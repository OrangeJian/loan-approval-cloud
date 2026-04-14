package com.loan.pay.service;

import com.loan.common.constant.RocketMQTopic;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * 支付服务
 */
@Service
public class PayService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 处理放款请求
     */
    public void processPayRequest(Map<String, Object> message) {
        String bizFlowNo = (String) message.get("bizFlowNo");
        Long loanId = getLong(message.get("loanId"));
        Long customerId = getLong(message.get("customerId"));
        BigDecimal amount = getBigDecimal(message.get("amount"));

        // 生成支付流水号
        String payFlowNo = "PAY" + System.currentTimeMillis();

        // 模拟调用支付渠道
        boolean paySuccess = doPay(customerId, amount);

        if (paySuccess) {
            // 发送支付成功消息
            sendPayCallback(loanId, payFlowNo, bizFlowNo, amount, "SUCCESS");
        } else {
            // 发送支付失败消息
            sendPayCallback(loanId, payFlowNo, bizFlowNo, amount, "FAILED");
        }
    }

    /**
     * 模拟支付
     */
    private boolean doPay(Long customerId, BigDecimal amount) {
        // 实际应该调用第三方支付渠道
        // 这里简化处理，假设支付一定成功
        return true;
    }

    /**
     * 发送支付回调
     */
    private void sendPayCallback(Long loanId, String payFlowNo, String bizFlowNo,
                                BigDecimal amount, String status) {
        try {
            Map<String, Object> callbackMessage = Map.of(
                    "loanId", loanId,
                    "payFlowNo", payFlowNo,
                    "bizFlowNo", bizFlowNo,
                    "amount", amount,
                    "status", status
            );
            rocketMQTemplate.convertAndSend(RocketMQTopic.LOAN_PAY + ":" + RocketMQTopic.Tag.CALLBACK, callbackMessage);
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 处理退款
     */
    public void processRefund(Map<String, Object> message) {
        // 实际应该调用支付渠道退款接口
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
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Double) return BigDecimal.valueOf((Double) value);
        if (value instanceof String && !((String) value).isEmpty()) {
            return new BigDecimal((String) value);
        }
        return BigDecimal.ZERO;
    }
}
