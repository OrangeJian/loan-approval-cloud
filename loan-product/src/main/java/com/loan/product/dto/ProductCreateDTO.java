package com.loan.product.dto;

import com.loan.product.dto.strategy.AmountStrategy;
import com.loan.product.dto.strategy.ApprovalStrategy;
import com.loan.product.dto.strategy.InterestStrategy;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建产品请求 DTO
 */
@Data
public class ProductCreateDTO {

    @NotBlank(message = "产品代码不能为空")
    private String productCode;

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    @NotNull(message = "产品类型不能为空")
    private String productType;

    @NotNull(message = "最低金额不能为空")
    @DecimalMin(value = "0.01", message = "最低金额必须大于0")
    private BigDecimal minAmount;

    @NotNull(message = "最高金额不能为空")
    @DecimalMin(value = "0.01", message = "最高金额必须大于0")
    private BigDecimal maxAmount;

    @NotNull(message = "最低期限不能为空")
    @Min(value = 1, message = "最低期限至少为1个月")
    private Integer minTerm;

    @NotNull(message = "最高期限不能为空")
    @Min(value = 1, message = "最高期限至少为1个月")
    private Integer maxTerm;

    @NotNull(message = "最低利率不能为空")
    @DecimalMin(value = "0", message = "最低利率不能为负")
    private BigDecimal minInterestRate;

    @NotNull(message = "最高利率不能为空")
    @DecimalMin(value = "0", message = "最高利率不能为负")
    private BigDecimal maxInterestRate;

    private AmountStrategy amountStrategy;

    private InterestStrategy interestStrategy;

    private ApprovalStrategy approvalStrategy;

    @NotBlank(message = "还款方式不能为空")
    private String repaymentType;

    private String quotaModel;
}
