package com.loan.common.enums;

/**
 * 审批阶段
 */
public enum ApprovalStage {
    AUTO_RISK("自动风控"),
    MANUAL_RISK("人工风控"),
    MANUAL_AMOUNT("人工额度"),
    FINAL_APPROVE("最终审批");

    private final String desc;

    ApprovalStage(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
