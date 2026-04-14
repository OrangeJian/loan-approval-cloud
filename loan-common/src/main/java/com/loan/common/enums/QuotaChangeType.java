package com.loan.common.enums;

/**
 * 额度变更类型
 */
public enum QuotaChangeType {
    GRANT("授信"),
    REDUCE("额度调减"),
    FREEZE("冻结"),
    UNFREEZE("解冻"),
    USE("使用"),
    RELEASE("释放"),
    ADJUST("调额");

    private final String desc;

    QuotaChangeType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
