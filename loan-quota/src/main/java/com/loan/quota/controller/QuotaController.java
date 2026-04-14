package com.loan.quota.controller;

import com.loan.common.dto.QuotaChangeDTO;
import com.loan.common.dto.Result;
import com.loan.common.entity.LoanCore;
import com.loan.quota.service.QuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/quota")
public class QuotaController {

    @Autowired
    private QuotaService quotaService;

    @GetMapping("/{loanId}")
    public Result<LoanCore> getByLoanId(@PathVariable Long loanId) {
        return quotaService.getByLoanId(loanId);
    }

    @GetMapping("/loan-no/{loanNo}")
    public Result<LoanCore> getByLoanNo(@PathVariable String loanNo) {
        return quotaService.getByLoanNo(loanNo);
    }

    @PostMapping("/create")
    public Result<LoanCore> createQuota(@RequestBody QuotaChangeDTO dto) {
        return quotaService.createQuota(
                dto.getLoanId(),
                null,
                dto.getChangeType(),
                dto.getChangeAmount(),
                null,
                null,
                dto.getBizFlowNo()
        );
    }

    @PostMapping("/freeze")
    public Result<Void> freezeQuota(@RequestBody QuotaChangeDTO dto) {
        return quotaService.freezeQuota(dto.getLoanId(), dto.getChangeAmount(),
                dto.getTriggerSource(), dto.getBizFlowNo());
    }

    @PostMapping("/use")
    public Result<Void> useQuota(@RequestBody QuotaChangeDTO dto) {
        return quotaService.useQuota(dto.getLoanId(), dto.getChangeAmount(),
                dto.getTriggerSource(), dto.getBizFlowNo());
    }

    @PostMapping("/release")
    public Result<Void> releaseQuota(@RequestBody QuotaChangeDTO dto) {
        return quotaService.releaseQuota(dto.getLoanId(), dto.getChangeAmount(),
                dto.getTriggerSource(), dto.getBizFlowNo());
    }

    @PostMapping("/reduce")
    public Result<Void> reduceQuota(@RequestParam Long loanId, @RequestParam BigDecimal newTotalQuota,
                                     @RequestParam String triggerSource, @RequestParam String bizFlowNo) {
        return quotaService.reduceQuota(loanId, newTotalQuota, triggerSource, bizFlowNo);
    }
}
