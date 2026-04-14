package com.loan.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 贷款核心表（统一贷款信息）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("loan_core")
public class LoanCore extends BaseEntity {

    private String loanNo;              // 贷款号，全局唯一
    private Long customerId;             // 客户ID
    private Long productId;             // 产品ID
    private String productType;         // 产品类型：REVOLVING/TERM
    private String loanStatus;          // 贷款状态
    private BigDecimal totalQuota;      // 总授信额度
    private BigDecimal usedQuota;       // 已用额度（循环贷用）
    private BigDecimal availableQuota;  // 可用额度
    private BigDecimal currentBalance;  // 当前余额
    private BigDecimal interestRate;    // 当前利率
    private Integer term;               // 期限（月）
    private LocalDate startDate;       // 生效日期
    private LocalDate expireDate;      // 到期日期
    private String bizFlowNo;          // 关联业务流水号
}
