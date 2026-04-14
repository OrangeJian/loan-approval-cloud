package com.loan.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("repayment_plan")
public class RepaymentPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long accountId;        // 账户ID
    private Long loanId;           // 贷款ID
    private Integer period;        // 期数
    private BigDecimal principal; // 本金
    private BigDecimal interest;   // 利息
    private BigDecimal amount;    // 还款金额
    private BigDecimal repaidPrincipal; // 已还本金
    private BigDecimal repaidInterest;  // 已还利息
    private BigDecimal repaidAmount;   // 已还金额
    private LocalDate dueDate;    // 应还日期
    private LocalDate repaidDate; // 实际还款日期
    private String status;        // 状态：FUTURE/CURRENT/OVERDUE/CLEARED
    private java.time.LocalDateTime createdTime;
    private java.time.LocalDateTime updatedTime;
}
