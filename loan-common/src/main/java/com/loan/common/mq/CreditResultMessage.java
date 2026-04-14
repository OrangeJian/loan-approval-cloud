package com.loan.common.mq;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 授信结果消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreditResultMessage extends LoanMessage {

    private Long loanId;           // 贷款ID
    private String loanNo;         // 贷款号
    private String approvalResult;  // 审批结果
    private BigDecimal approvedQuota; // 审批额度
    private BigDecimal interestRate; // 审批利率
    private String comment;         // 审批意见

    public CreditResultMessage() {
        this.setMessageType("CREDIT_RESULT");
    }
}
