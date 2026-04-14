package com.loan.common.enums;

/**
 * 贷款状态
 */
public enum LoanStatus {
    CREDIT_PENDING("授信待审批"),
    CREDIT_APPROVED("授信已通过"),
    CREDIT_REJECTED("授信已拒绝"),
    LOAN_PENDING("用信待审批"),
    LOAN_APPROVED("用信已通过"),
    LOAN_REJECTED("用信已拒绝"),
    ACTIVE("生效中"),
    CLEARED("已结清"),
    OVERDUE("逾期"),
    FROZEN("冻结");

    private final String desc;

    LoanStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
