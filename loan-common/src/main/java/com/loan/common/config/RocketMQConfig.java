package com.loan.common.config;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.core.MessageTemplate;

/**
 * RocketMQ配置
 */
@Configuration
public class RocketMQConfig {

    @Bean
    public MessageTemplate messageTemplate() {
        return new RocketMQTemplate();
    }
}
