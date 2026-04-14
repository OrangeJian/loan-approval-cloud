package com.loan.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 额度变更DTO
 */
@Data
public class QuotaChangeDTO implements Serializable {

    private Long loanId;            // 贷款ID
    private String changeType;      // 变更类型
    private BigDecimal changeAmount;// 变更金额
    private String triggerSource;   // 触发来源
    private String bizFlowNo;       // 业务流水号
    private String remark;          // 备注
}
