package com.loan.facade.service;

import com.loan.common.constant.RocketMQTopic;
import com.loan.common.dto.CreditApplyDTO;
import com.loan.common.dto.LoanApplyDTO;
import com.loan.common.dto.Result;
import com.loan.common.mq.CreditApplyMessage;
import com.loan.common.mq.LoanApplyMessage;
import com.loan.common.util.RedisLockUtil;
import com.loan.common.util.IdGenerator;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

/**
 * 前置服务 - 业务编排
 */
@Service
public class FacadeService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 授信申请
     */
    public Result<Void> creditApply(Map<String, Object> request) {
        String bizFlowNo = (String) request.get("bizFlowNo");

        // 幂等性检查
        if (checkDuplicate(bizFlowNo)) {
            return Result.error("重复请求");
        }

        // 构建授信申请DTO
        CreditApplyDTO dto = new CreditApplyDTO();
        dto.setChannelCode((String) request.get("channelCode"));
        dto.setBizFlowNo(bizFlowNo);
        dto.setCustomerId(getLong(request.get("customerId")));
        dto.setProductId(getLong(request.get("productId")));
        dto.setApplyAmount(getBigDecimal(request.get("applyAmount")));
        dto.setApplyTerm(getInteger(request.get("applyTerm")));
        dto.setApplyPurpose((String) request.get("applyPurpose"));

        // 发送授信申请消息
        CreditApplyMessage message = new CreditApplyMessage(dto);
        rocketMQTemplate.convertAndSend(RocketMQTopic.LOAN_CREDIT + ":" + RocketMQTopic.Tag.APPLY, message);

        // 标记已处理
        markProcessed(bizFlowNo);

        return Result.success();
    }

    /**
     * 用信申请
     */
    public Result<Void> loanApply(Map<String, Object> request) {
        String bizFlowNo = (String) request.get("bizFlowNo");

        if (checkDuplicate(bizFlowNo)) {
            return Result.error("重复请求");
        }

        LoanApplyDTO dto = new LoanApplyDTO();
        dto.setChannelCode((String) request.get("channelCode"));
        dto.setBizFlowNo(bizFlowNo);
        dto.setLoanId(getLong(request.get("loanId")));
        dto.setLoanNo((String) request.get("loanNo"));
        dto.setCustomerId(getLong(request.get("customerId")));
        dto.setApplyAmount(getBigDecimal(request.get("applyAmount")));
        dto.setApplyTerm(getInteger(request.get("applyTerm")));
        dto.setApplyPurpose((String) request.get("applyPurpose"));

        LoanApplyMessage message = new LoanApplyMessage(dto);
        rocketMQTemplate.convertAndSend(RocketMQTopic.LOAN_LOAN + ":" + RocketMQTopic.Tag.APPLY, message);

        markProcessed(bizFlowNo);

        return Result.success();
    }

    /**
     * 还款
     */
    public Result<Void> repay(Map<String, Object> request) {
        String bizFlowNo = (String) request.get("bizFlowNo");

        if (checkDuplicate(bizFlowNo)) {
            return Result.error("重复请求");
        }

        // 发送还款消息到LOAN_LOAN topic
        rocketMQTemplate.convertAndSend(RocketMQTopic.LOAN_LOAN + ":repay", request);

        markProcessed(bizFlowNo);

        return Result.success();
    }

    /**
     * 查询
     */
    public Result<Object> query(Map<String, Object> request) {
        // 查询请求可以转发到loan-query服务
        return Result.success(request);
    }

    private boolean checkDuplicate(String bizFlowNo) {
        String key = "processed:" + bizFlowNo;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    private void markProcessed(String bizFlowNo) {
        String key = "processed:" + bizFlowNo;
        redisTemplate.opsForValue().set(key, "1");
        redisTemplate.expire(key, Duration.ofHours(24)); // 24小时过期
    }

    private Long getLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String && !((String) value).isEmpty()) {
            return Long.parseLong((String) value);
        }
        return null;
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Double) return BigDecimal.valueOf((Double) value);
        if (value instanceof String && !((String) value).isEmpty()) {
            return new BigDecimal((String) value);
        }
        return null;
    }

    private Integer getInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof String && !((String) value).isEmpty()) {
            return Integer.parseInt((String) value);
        }
        return null;
    }
}
