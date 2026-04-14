package com.loan.common.enums;

/**
 * 审批结果
 */
public enum ApprovalResult {
    PENDING("待审批"),
    PASS("通过"),
    REJECT("拒绝"),
    AUTO_PASS("自动通过"),
    AUTO_REJECT("自动拒绝");

    private final String desc;

    ApprovalResult(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
