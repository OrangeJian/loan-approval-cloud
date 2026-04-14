package com.loan.query.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.loan.common.dto.LoanQueryDTO;
import com.loan.common.dto.Result;
import com.loan.common.entity.LoanCore;
import com.loan.common.entity.LoanTransaction;
import com.loan.query.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/query")
public class QueryController {

    @Autowired
    private QueryService queryService;

    @GetMapping("/loan/{loanNo}")
    public Result<Map<String, Object>> getLoanDetail(@PathVariable String loanNo) {
        return queryService.getLoanDetail(loanNo);
    }

    @PostMapping("/loan/list")
    public Result<Page<LoanCore>> getLoanList(@RequestBody LoanQueryDTO queryDTO) {
        return queryService.getLoanList(queryDTO);
    }

    @GetMapping("/transaction/{loanId}")
    public Result<List<LoanTransaction>> getTransactionHistory(@PathVariable Long loanId) {
        return queryService.getTransactionHistory(loanId);
    }
}
