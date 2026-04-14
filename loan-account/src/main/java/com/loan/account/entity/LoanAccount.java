package com.loan.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("loan_account")
public class LoanAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long loanId;           // 关联贷款ID
    private String loanNo;         // 贷款号
    private Long customerId;        // 客户ID
    private String productType;    // 产品类型
    private BigDecimal loanAmount; // 借款金额
    private BigDecimal interestRate; // 利率
    private BigDecimal totalInterest; // 总利息
    private BigDecimal totalAmount; // 总金额
    private BigDecimal repaidPrincipal; // 已还本金
    private BigDecimal repaidInterest; // 已还利息
    private BigDecimal repaidAmount; // 已还金额
    private LocalDate loanDate;   // 放款日期
    private LocalDate expireDate;  // 到期日期
    private Integer term;         // 期限
    private String repaymentType; // 还款方式
    private String status;        // 账户状态
    private java.time.LocalDateTime createdTime;
    private java.time.LocalDateTime updatedTime;
}
