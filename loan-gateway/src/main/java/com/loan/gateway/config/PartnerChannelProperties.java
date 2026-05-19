package com.loan.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 合作伙伴渠道配置 - 从 application.yml 加载
 */
@Data
@Component
@ConfigurationProperties(prefix = "partner.channels")
public class PartnerChannelProperties {
    private Map<String, PartnerChannelConfig> channels;
}
