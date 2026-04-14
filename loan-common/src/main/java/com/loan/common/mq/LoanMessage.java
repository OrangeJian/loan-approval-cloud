package com.loan.common.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * 贷款消息基类
 */
@Data
public class LoanMessage implements Serializable {

    private String messageId;       // 消息ID
    private String bizFlowNo;       // 业务流水号
    private String messageType;     // 消息类型
    private Long loanId;           // 贷款ID
    private Long customerId;        // 客户ID
    private String productType;     // 产品类型
    private Object data;           // 消息数据
    private Long timestamp;        // 时间戳
    private Integer retryCount;    // 重试次数

    public LoanMessage() {
        this.messageId = java.util.UUID.randomUUID().toString().replace("-", "");
        this.timestamp = System.currentTimeMillis();
        this.retryCount = 0;
    }
}
