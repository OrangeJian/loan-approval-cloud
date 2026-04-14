package com.loan.pay.controller;

import com.loan.common.dto.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay")
public class PayController {

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("OK");
    }
}
