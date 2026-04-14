package com.loan.common.enums;

/**
 * 交易类型
 */
public enum TransactionType {
    CREDIT_APPLY("授信申请"),
    CREDIT_APPROVE("授信审批"),
    CREDIT_REJECT("授信拒绝"),
    LOAN_APPLY("用信申请"),
    LOAN_APPROVE("用信用审批"),
    LOAN_REJECT("用信拒绝"),
    REPAY("还款"),
    OVERDUE("逾期"),
    QUOTA_ADJUST("额度调整"),
    INTEREST_ADJUST("利率调整");

    private final String desc;

    TransactionType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
