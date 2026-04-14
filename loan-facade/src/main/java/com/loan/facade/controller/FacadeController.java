package com.loan.facade.controller;

import com.loan.common.dto.Result;
import com.loan.facade.service.FacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 前置服务控制器
 */
@RestController
@RequestMapping("/facade")
public class FacadeController {

    @Autowired
    private FacadeService facadeService;

    @PostMapping("/credit/apply")
    public Result<Void> creditApply(@RequestBody Map<String, Object> request) {
        return facadeService.creditApply(request);
    }

    @PostMapping("/loan/apply")
    public Result<Void> loanApply(@RequestBody Map<String, Object> request) {
        return facadeService.loanApply(request);
    }

    @PostMapping("/repay")
    public Result<Void> repay(@RequestBody Map<String, Object> request) {
        return facadeService.repay(request);
    }

    @PostMapping("/query")
    public Result<Object> query(@RequestBody Map<String, Object> request) {
        return facadeService.query(request);
    }
}
