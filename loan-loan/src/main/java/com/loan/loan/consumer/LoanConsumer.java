package com.loan.loan.consumer;

import com.loan.common.constant.RocketMQTopic;
import com.loan.common.mq.LoanApplyMessage;
import com.loan.loan.service.LoanService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用信消息消费者
 */
@Component
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true", matchIfMissing = true)
@RocketMQMessageListener(
        topic = RocketMQTopic.LOAN_LOAN,
        selectorExpression = RocketMQTopic.Tag.APPLY,
        consumerGroup = "loan-loan-consumer-group"
)
public class LoanConsumer implements RocketMQListener<LoanApplyMessage> {

    @Autowired
    private LoanService loanService;

    @Override
    public void onMessage(LoanApplyMessage message) {
        try {
            loanService.processLoanApply(message);
        } catch (Exception e) {
            throw new RuntimeException("用信申请处理失败", e);
        }
    }
}
