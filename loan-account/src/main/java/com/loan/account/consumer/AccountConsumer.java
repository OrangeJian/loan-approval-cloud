package com.loan.account.consumer;

import com.loan.common.constant.RocketMQTopic;
import com.loan.account.service.AccountService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 账务消息消费者
 */
@Component
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true", matchIfMissing = true)
@RocketMQMessageListener(
        topic = RocketMQTopic.LOAN_ACCOUNT,
        selectorExpression = "*",
        consumerGroup = "loan-account-consumer-group"
)
public class AccountConsumer implements RocketMQListener<Map<String, Object>> {

    @Autowired
    private AccountService accountService;

    @Override
    public void onMessage(Map<String, Object> message) {
        try {
            accountService.createAccount(message);
        } catch (Exception e) {
            // 处理创建账务的消息
            try {
                accountService.processRepay(message);
            } catch (Exception ex) {
                throw new RuntimeException("账务处理失败", ex);
            }
        }
    }
}
