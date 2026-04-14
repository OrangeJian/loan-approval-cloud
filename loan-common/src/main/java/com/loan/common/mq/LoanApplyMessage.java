package com.loan.common.mq;

import com.loan.common.dto.LoanApplyDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用信申请消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoanApplyMessage extends LoanMessage {

    private LoanApplyDTO loanApplyDTO;

    public LoanApplyMessage(LoanApplyDTO dto) {
        super();
        this.setMessageType("LOAN_APPLY");
        this.setData(dto);
        if (dto != null) {
            this.setBizFlowNo(dto.getBizFlowNo());
            this.setLoanId(dto.getLoanId());
            this.setCustomerId(dto.getCustomerId());
        }
    }
}
