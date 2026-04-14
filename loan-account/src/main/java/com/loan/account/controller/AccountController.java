package com.loan.account.controller;

import com.loan.account.entity.LoanAccount;
import com.loan.account.entity.RepaymentPlan;
import com.loan.account.mapper.LoanAccountMapper;
import com.loan.account.mapper.RepaymentPlanMapper;
import com.loan.common.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private LoanAccountMapper accountMapper;

    @Autowired
    private RepaymentPlanMapper planMapper;

    @GetMapping("/loan/{loanId}")
    public Result<LoanAccount> getByLoanId(@PathVariable Long loanId) {
        LoanAccount account = accountMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LoanAccount>()
                        .eq(LoanAccount::getLoanId, loanId)
        );
        return Result.success(account);
    }

    @GetMapping("/{id}/plans")
    public Result<List<RepaymentPlan>> getRepaymentPlans(@PathVariable Long id) {
        List<RepaymentPlan> plans = planMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RepaymentPlan>()
                        .eq(RepaymentPlan::getAccountId, id)
                        .orderByAsc(RepaymentPlan::getPeriod)
        );
        return Result.success(plans);
    }

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("OK");
    }
}
