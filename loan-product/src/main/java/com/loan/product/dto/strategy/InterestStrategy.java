package com.loan.product.dto.strategy;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 利率策略配置
 */
@Data
public class InterestStrategy {
    private BigDecimal baseRate;            // 基础利率
    private Boolean riskAdjust;             // 是否启用风险调整
    private Boolean termAdjust;             // 是否启用期限调整
    private Boolean amountAdjust;           // 是否启用金额调整
    private BigDecimal riskPremiumHigh;     // 高风险溢价
    private BigDecimal riskPremiumMedium;   // 中风险溢价
    private BigDecimal riskDiscountLow;     // 低风险折扣
}
