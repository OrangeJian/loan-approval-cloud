package com.loan.common.enums;

/**
 * 还款方式
 */
public enum RepaymentType {
    REVOLVING_STYLE("随借随还"),
    EQUAL_PRINCIPAL_INTEREST("等额本息"),
    EQUAL_PRINCIPAL("等额本金"),
    BULLET_REPAYMENT("一次性还本付息"),
    MONTHLY_INTEREST_BULLET_PRINCIPAL("按月付息到期还本");

    private final String desc;

    RepaymentType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
