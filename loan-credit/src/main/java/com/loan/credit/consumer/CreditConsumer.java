package com.loan.credit.consumer;

import com.loan.common.constant.RocketMQTopic;
import com.loan.common.mq.CreditApplyMessage;
import com.loan.credit.service.CreditService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 授信消息消费者
 */
@Component
@RocketMQMessageListener(
        topic = RocketMQTopic.LOAN_CREDIT,
        selectorExpression = RocketMQTopic.Tag.APPLY,
        consumerGroup = "loan-credit-consumer-group"
)
public class CreditConsumer implements RocketMQListener<CreditApplyMessage> {

    @Autowired
    private CreditService creditService;

    @Override
    public void onMessage(CreditApplyMessage message) {
        try {
            creditService.processCreditApply(message);
        } catch (Exception e) {
            throw new RuntimeException("授信申请处理失败", e);
        }
    }
}
