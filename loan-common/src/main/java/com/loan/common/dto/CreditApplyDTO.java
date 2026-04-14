package com.loan.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 授信申请DTO
 */
@Data
public class CreditApplyDTO implements Serializable {

    private String channelCode;      // 渠道代码
    private String bizFlowNo;        // 业务流水号
    private Long customerId;         // 客户ID
    private Long productId;         // 产品ID
    private BigDecimal applyAmount;  // 申请金额
    private Integer applyTerm;       // 申请期限
    private String applyPurpose;     // 申请用途
    private String extData;         // 扩展数据JSON
}
