package com.loan.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 交易流水表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("loan_transaction")
public class LoanTransaction extends BaseEntity {

    private Long loanId;              // 关联贷款ID
    private String transType;         // 交易类型
    private BigDecimal transAmount;   // 交易金额
    private String transStatus;       // 交易状态
    private String bizFlowNo;         // 业务流水号
    private String requestSource;     // 请求来源
    private String approvalResult;    // 审批结果
    private String approvalComment;   // 审批意见
    private Long approverId;          // 审批人ID
    private String relatedEventId;    // 关联RocketMQ消息ID
    private String beforeStatus;      // 变更前状态
    private String afterStatus;       // 变更后状态
    private String extData;           // 扩展信息JSON
}
