package com.loan.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 额度变更记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("loan_quota_change")
public class LoanQuotaChange extends BaseEntity {

    private Long loanId;               // 关联贷款ID
    private String changeType;         // 变更类型
    private BigDecimal changeAmount;   // 变更金额
    private BigDecimal beforeQuota;   // 变更前额度
    private BigDecimal afterQuota;     // 变更后额度
    private String triggerSource;      // 触发来源服务
    private String bizFlowNo;         // 业务流水号
    private String remark;            // 备注
}
