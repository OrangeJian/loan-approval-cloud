package com.loan.pay.consumer;

import com.loan.common.constant.RocketMQTopic;
import com.loan.pay.service.PayService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true", matchIfMissing = true)
@RocketMQMessageListener(
        topic = RocketMQTopic.LOAN_PAY,
        selectorExpression = "*",
        consumerGroup = "loan-pay-consumer-group"
)
public class PayConsumer implements RocketMQListener<Map<String, Object>> {

    @Autowired
    private PayService payService;

    @Override
    public void onMessage(Map<String, Object> message) {
        try {
            payService.processPayRequest(message);
        } catch (Exception e) {
            throw new RuntimeException("支付处理失败", e);
        }
    }
}
