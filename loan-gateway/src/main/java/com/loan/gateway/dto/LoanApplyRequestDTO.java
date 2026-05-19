package com.loan.gateway.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 贷款申请请求 DTO - 嵌套在 ChannelRequestDTO.data 中
 */
@Data
public class LoanApplyRequestDTO {

    @NotBlank(message = "客户编号不能为空")
    private String customerId;

    @NotBlank(message = "产品代码不能为空")
    private String productCode;

    @NotNull(message = "申请金额不能为空")
    @DecimalMin(value = "0.01", message = "申请金额必须大于0")
    private BigDecimal applyAmount;

    @NotNull(message = "申请期限不能为空")
    @Min(value = 1, message = "申请期限至少为1个月")
    private Integer applyTerm;

    @NotBlank(message = "还款方式不能为空")
    private String repaymentType;

    private String applyPurpose;

    private String notifyUrl;

    private String extData;
}
