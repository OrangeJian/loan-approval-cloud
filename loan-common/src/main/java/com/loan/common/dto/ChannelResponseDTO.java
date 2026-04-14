package com.loan.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 渠道响应DTO
 */
@Data
public class ChannelResponseDTO implements Serializable {

    private String channelCode;     // 渠道代码
    private String bizFlowNo;       // 业务流水号
    private String code;           // 响应码
    private String message;         // 响应消息
    private String data;            // 业务数据JSON
    private String sign;            // 签名
    private String timestamp;       // 时间戳
}
