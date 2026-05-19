package com.loan.product.dto;

import com.loan.product.dto.strategy.AmountStrategy;
import com.loan.product.dto.strategy.ApprovalStrategy;
import com.loan.product.dto.strategy.InterestStrategy;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新产品请求 DTO
 */
@Data
public class ProductUpdateDTO {

    @NotNull(message = "产品ID不能为空")
    private Long id;

    private String productCode;

    private String productName;

    private String productType;

    @DecimalMin(value = "0.01", message = "最低金额必须大于0")
    private BigDecimal minAmount;

    @DecimalMin(value = "0.01", message = "最高金额必须大于0")
    private BigDecimal maxAmount;

    @Min(value = 1, message = "最低期限至少为1个月")
    private Integer minTerm;

    @Min(value = 1, message = "最高期限至少为1个月")
    private Integer maxTerm;

    @DecimalMin(value = "0", message = "最低利率不能为负")
    private BigDecimal minInterestRate;

    @DecimalMin(value = "0", message = "最高利率不能为负")
    private BigDecimal maxInterestRate;

    private AmountStrategy amountStrategy;

    private InterestStrategy interestStrategy;

    private ApprovalStrategy approvalStrategy;

    private String repaymentType;

    private String quotaModel;

    private String status;
}
