package com.loan.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 产品表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("loan_product")
public class Product extends BaseEntity {

    private String productCode;     // 产品代码
    private String productName;     // 产品名称
    private String productType;     // 产品类型：REVOLVING/TERM
    private BigDecimal minAmount;   // 最低金额
    private BigDecimal maxAmount;   // 最高金额
    private Integer minTerm;        // 最低期限（月）
    private Integer maxTerm;        // 最高期限（月）
    private BigDecimal minInterestRate;  // 最低利率
    private BigDecimal maxInterestRate;  // 最高利率
    private String amountStrategy;  // 额度策略JSON
    private String interestStrategy; // 利率策略JSON
    private String approvalStrategy; // 审批策略JSON
    private String repaymentType;   // 还款方式
    private String quotaModel;      // 额度模型
    private String status;          // 状态
}
