package com.loan.common.enums;

/**
 * 产品类型
 */
public enum ProductType {
    REVOLVING("循环贷"),
    TERM("一次性贷款");

    private final String desc;

    ProductType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
