package com.loan.credit.controller;

import com.loan.common.dto.Result;
import com.loan.credit.service.CreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/credit")
public class CreditController {

    @Autowired
    private CreditService creditService;

    // 预留一些管理接口
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("OK");
    }
}
