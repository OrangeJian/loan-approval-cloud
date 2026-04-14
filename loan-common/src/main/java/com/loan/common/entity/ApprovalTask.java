package com.loan.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 审批任务表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval_task")
public class ApprovalTask extends BaseEntity {

    private String taskNo;           // 任务编号
    private Long loanId;            // 关联贷款ID
    private String flowCode;         // 流程代码
    private String currentStage;     // 当前阶段
    private String taskStatus;       // 任务状态
    private Long assigneeId;         // 处理人ID
    private String assigneeName;     // 处理人姓名
    private LocalDateTime claimTime; // 领取时间
    private LocalDateTime completeTime; // 完成时间
    private String approvalResult;   // 审批结果
    private String comment;          // 审批意见
}
