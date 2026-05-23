package com.loan.gateway.dto;

import lombok.Data;

/**
 * 贷款申请响应 DTO
 */
@Data
public class LoanApplyResponseDTO {
    private String bizFlowNo;
    private String status;
    private String message;
}
