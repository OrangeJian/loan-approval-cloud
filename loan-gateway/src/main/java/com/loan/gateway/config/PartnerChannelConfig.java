package com.loan.gateway.config;

import lombok.Data;

/**
 * 单个合作伙伴渠道配置
 */
@Data
public class PartnerChannelConfig {
    /** 渠道代码（由 YAML key 注入后设置） */
    private String channelCode;
    private String channelName;
    private String secretKey;
    private int rateLimitPerMinute = 100;
    private String ipWhitelist;
    private String status = "enabled";
    private String notifyUrl;
}
