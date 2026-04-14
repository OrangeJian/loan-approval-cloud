package com.loan.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 渠道请求DTO
 */
@Data
public class ChannelRequestDTO implements Serializable {

    private String channelCode;     // 渠道代码
    private String bizFlowNo;       // 业务流水号
    private String bizType;         // 业务类型
    private String sign;            // 签名
    private String timestamp;       // 时间戳
    private String version;         // 版本号
    private String data;            // 业务数据JSON
}
