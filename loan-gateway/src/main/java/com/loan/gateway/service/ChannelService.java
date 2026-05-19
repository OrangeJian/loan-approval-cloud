package com.loan.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loan.common.dto.ChannelRequestDTO;
import com.loan.common.dto.ChannelResponseDTO;
import com.loan.gateway.config.PartnerChannelConfig;
import com.loan.gateway.config.PartnerChannelProperties;
import com.loan.gateway.dto.LoanApplyRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

/**
 * 渠道服务 - 处理渠道接入协议转换、鉴权等
 */
@Service
public class ChannelService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PartnerChannelProperties channelProperties;

    @Autowired
    private Validator validator;

    private static final String RATE_LIMIT_PREFIX = "rate:limit:";
    private static final String CHANNEL_CACHE_PREFIX = "channel:config:";
    private static final int DEFAULT_MAX_REQUEST_PER_MINUTE = 100;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * 加载渠道配置（Redis 缓存 + YAML 回源）
     */
    public PartnerChannelConfig loadChannelConfig(String channelCode) {
        // 1. 尝试 Redis 缓存
        String cacheKey = CHANNEL_CACHE_PREFIX + channelCode;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, PartnerChannelConfig.class);
            } catch (Exception e) {
                redisTemplate.delete(cacheKey);
            }
        }

        // 2. 从 YAML 配置加载
        Map<String, PartnerChannelConfig> channels = channelProperties.getChannels();
        if (channels == null) {
            return null;
        }
        PartnerChannelConfig config = channels.get(channelCode);
        if (config == null || !"enabled".equals(config.getStatus())) {
            return null;
        }
        config.setChannelCode(channelCode);

        // 3. 写入 Redis 缓存（30 分钟 TTL）
        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(config), Duration.ofMinutes(30));
        } catch (Exception e) {
            // ignore cache write failure
        }
        return config;
    }

    /**
     * HMAC-SHA256 签名验证
     * 签名公式：hex(HMAC-SHA256(secretKey, channelCode + bizFlowNo + timestamp + data))
     */
    public boolean verifySign(ChannelRequestDTO request) {
        PartnerChannelConfig channel = loadChannelConfig(request.getChannelCode());
        if (channel == null) {
            return false;
        }

        String expectedSign = hmacSha256(
                channel.getSecretKey(),
                request.getChannelCode() +
                        (request.getBizFlowNo() != null ? request.getBizFlowNo() : "") +
                        (request.getTimestamp() != null ? request.getTimestamp() : "") +
                        (request.getData() != null ? request.getData() : "")
        );

        return MessageDigest.isEqual(expectedSign.getBytes(), request.getSign().getBytes());
    }

    /**
     * 生成响应签名
     * 签名公式：hex(HMAC-SHA256(secretKey, channelCode + bizFlowNo + code + data))
     */
    public String signResponse(PartnerChannelConfig channel, String bizFlowNo, String code, String data) {
        return hmacSha256(
                channel.getSecretKey(),
                channel.getChannelCode() +
                        (bizFlowNo != null ? bizFlowNo : "") +
                        (code != null ? code : "") +
                        (data != null ? data : "")
        );
    }

    private String hmacSha256(String secret, String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] bytes = mac.doFinal(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 签名失败", e);
        }
    }

    /**
     * 频率控制（支持渠道自定义阈值）
     */
    public boolean checkRateLimit(String channelCode, String bizFlowNo) {
        PartnerChannelConfig channel = loadChannelConfig(channelCode);
        int limit = (channel != null) ? channel.getRateLimitPerMinute() : DEFAULT_MAX_REQUEST_PER_MINUTE;

        String key = RATE_LIMIT_PREFIX + channelCode + ":" + System.currentTimeMillis() / 60000;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count > limit) {
            return false;
        }
        redisTemplate.expire(key, Duration.ofSeconds(60L));
        return true;
    }

    /**
     * 解析贷款申请业务数据
     */
    public LoanApplyRequestDTO parseLoanApplyRequest(ChannelRequestDTO request) {
        if (request.getData() == null || request.getData().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(request.getData(), LoanApplyRequestDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 校验 LoanApplyRequestDTO 字段
     */
    public String validateLoanApplyRequest(LoanApplyRequestDTO dto) {
        Set<ConstraintViolation<LoanApplyRequestDTO>> violations = validator.validate(dto);
        if (violations.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation<LoanApplyRequestDTO> v : violations) {
            if (sb.length() > 0) sb.append("; ");
            sb.append(v.getPropertyPath()).append(": ").append(v.getMessage());
        }
        return sb.toString();
    }

    /**
     * 构建带签名的响应
     */
    public ChannelResponseDTO buildSignedResponse(PartnerChannelConfig channel, String bizFlowNo,
                                                  String code, String message, Object data) {
        ChannelResponseDTO response = buildResponse(channel.getChannelCode(), bizFlowNo, code, message, data);
        String dataStr = response.getData();
        response.setSign(signResponse(channel, bizFlowNo, code, dataStr));
        return response;
    }

    /**
     * 构建响应
     */
    public ChannelResponseDTO buildResponse(String channelCode, String bizFlowNo, String code, String message, Object data) {
        ChannelResponseDTO response = new ChannelResponseDTO();
        response.setChannelCode(channelCode);
        response.setBizFlowNo(bizFlowNo);
        response.setCode(code);
        response.setMessage(message);
        response.setTimestamp(String.valueOf(System.currentTimeMillis()));
        if (data != null) {
            try {
                response.setData(objectMapper.writeValueAsString(data));
            } catch (Exception e) {
                response.setData(data.toString());
            }
        }
        return response;
    }

    /**
     * 解析请求数据
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseRequestData(ChannelRequestDTO request) {
        if (request.getData() == null || request.getData().isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(request.getData(), Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
}
