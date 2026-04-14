package com.loan.gateway.feign;

import com.loan.common.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "loan-facade", path = "/facade")
public interface FacadeFeignClient {

    @PostMapping("/credit/apply")
    Result<Void> creditApply(@RequestBody Map<String, Object> request);

    @PostMapping("/loan/apply")
    Result<Void> loanApply(@RequestBody Map<String, Object> request);

    @PostMapping("/repay")
    Result<Void> repay(@RequestBody Map<String, Object> request);

    @PostMapping("/query")
    Result<Object> query(@RequestBody Map<String, Object> request);
}
