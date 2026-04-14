package com.loan.loan.controller;

import com.loan.common.dto.Result;
import com.loan.loan.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/loan")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping("/confirm")
    public Result<Void> confirmLoan(@RequestParam Long loanId, @RequestParam BigDecimal amount,
                                    @RequestParam String bizFlowNo) {
        loanService.confirmLoan(loanId, amount, bizFlowNo);
        return Result.success();
    }

    @PostMapping("/repay")
    public Result<Void> repay(@RequestBody Map<String, Object> request) {
        loanService.processRepay(request);
        return Result.success();
    }

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("OK");
    }
}
