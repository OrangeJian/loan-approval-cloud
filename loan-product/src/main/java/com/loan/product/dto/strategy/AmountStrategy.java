package com.loan.product.dto.strategy;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 额度策略配置
 */
@Data
public class AmountStrategy {
    private BigDecimal minQuota;            // 最低授信额度
    private BigDecimal maxQuota;            // 最高授信额度
    private String quotaModel;              // 额度模型：SCORE_MODEL / INCOME_MODEL
    private BigDecimal scoreWeight;         // 评分权重系数
    private BigDecimal incomeWeight;        // 收入权重系数
}
