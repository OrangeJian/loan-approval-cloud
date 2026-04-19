package com.loan.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loan.common.dto.ChannelRequestDTO;
import com.loan.common.dto.ChannelResponseDTO;
import com.loan.common.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;

/**
 * 渠道服务 - 处理渠道接入协议转换、鉴权等
 */
@Service
public class ChannelService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String RATE_LIMIT_PREFIX = "rate:limit:";
    private static final int MAX_REQUEST_PER_MINUTE = 100;

    /**
     * 验证渠道签名
     */
    public boolean verifySign(ChannelRequestDTO request) {
        // 简化签名验证，实际应该使用RSA等非对称加密
        return request.getSign() != null && !request.getSign().isEmpty();
    }

    /**
     * 频率控制
     */
    public boolean checkRateLimit(String channelCode, String bizFlowNo) {
        String key = RATE_LIMIT_PREFIX + channelCode + ":" + System.currentTimeMillis() / 60000;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count > MAX_REQUEST_PER_MINUTE) {
            return false;
        }
        redisTemplate.expire(key, Duration.ofSeconds(60L));
        return true;
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