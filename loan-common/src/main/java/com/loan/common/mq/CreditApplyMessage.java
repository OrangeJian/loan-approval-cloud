package com.loan.common.mq;

import com.loan.common.dto.CreditApplyDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 授信申请消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreditApplyMessage extends LoanMessage {

    private CreditApplyDTO creditApplyDTO;

    public CreditApplyMessage(CreditApplyDTO dto) {
        super();
        this.setMessageType("CREDIT_APPLY");
        this.setData(dto);
        if (dto != null) {
            this.setBizFlowNo(dto.getBizFlowNo());
            this.setCustomerId(dto.getCustomerId());
        }
    }
}
