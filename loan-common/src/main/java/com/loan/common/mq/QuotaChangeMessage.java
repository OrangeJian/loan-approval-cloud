package com.loan.common.mq;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 额度变更消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuotaChangeMessage extends LoanMessage {

    private Long loanId;           // 贷款ID
    private String changeType;      // 变更类型
    private BigDecimal changeAmount;// 变更金额
    private BigDecimal beforeQuota; // 变更前额度
    private BigDecimal afterQuota; // 变更后额度

    public QuotaChangeMessage() {
        this.setMessageType("QUOTA_CHANGE");
    }
}
