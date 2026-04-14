package com.loan.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 审批DTO
 */
@Data
public class ApprovalDTO implements Serializable {

    private String taskNo;           // 任务编号
    private Long loanId;            // 贷款ID
    private String approvalResult;   // 审批结果：PASS/REJECT
    private String comment;          // 审批意见
    private Long approverId;         // 审批人ID
    private String approverName;     // 审批人姓名
}
