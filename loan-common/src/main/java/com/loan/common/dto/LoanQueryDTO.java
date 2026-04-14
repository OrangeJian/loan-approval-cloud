package com.loan.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 贷款查询DTO
 */
@Data
public class LoanQueryDTO implements Serializable {

    private String loanNo;         // 贷款号
    private Long customerId;        // 客户ID
    private String loanStatus;      // 贷款状态
    private String productType;      // 产品类型
    private String startDate;       // 开始日期
    private String endDate;         // 结束日期
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
