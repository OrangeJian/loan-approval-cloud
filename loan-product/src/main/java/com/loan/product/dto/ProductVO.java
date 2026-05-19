package com.loan.product.dto;

import com.loan.product.dto.strategy.AmountStrategy;
import com.loan.product.dto.strategy.ApprovalStrategy;
import com.loan.product.dto.strategy.InterestStrategy;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品响应 VO
 */
@Data
public class ProductVO {

    private Long id;
    private String productCode;
    private String productName;
    private String productType;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer minTerm;
    private Integer maxTerm;
    private BigDecimal minInterestRate;
    private BigDecimal maxInterestRate;
    private AmountStrategy amountStrategy;
    private InterestStrategy interestStrategy;
    private ApprovalStrategy approvalStrategy;
    private String repaymentType;
    private String quotaModel;
    private String status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
