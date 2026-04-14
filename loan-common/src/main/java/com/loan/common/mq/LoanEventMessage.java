package com.loan.common.mq;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通用事件消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoanEventMessage extends LoanMessage {

    private String eventType;      // 事件类型
    private String eventSource;    // 事件来源

    public LoanEventMessage() {
        this.setMessageType("LOAN_EVENT");
    }
}
