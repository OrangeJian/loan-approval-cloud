package com.loan.product.dto.strategy;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 审批策略配置
 */
@Data
public class ApprovalStrategy {
    private Integer autoApproveScore;       // 自动审批最低评分
    private BigDecimal needManualAmount;    // 需要人工审批的金额阈值
    private Integer maxAutoApproveTerm;     // 自动审批最大期限（月）
    private Boolean needManualRisk;         // 是否必须人工风控
}
