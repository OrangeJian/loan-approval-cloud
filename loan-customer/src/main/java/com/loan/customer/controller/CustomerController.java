package com.loan.customer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.loan.common.dto.Result;
import com.loan.common.entity.Customer;
import com.loan.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/list")
    public Result<Page<Customer>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return customerService.list(pageNum, pageSize, keyword);
    }

    @GetMapping("/{id}")
    public Result<Customer> getById(@PathVariable Long id) {
        return customerService.getById(id);
    }

    @GetMapping("/no/{customerNo}")
    public Result<Customer> getByCustomerNo(@PathVariable String customerNo) {
        return customerService.getByCustomerNo(customerNo);
    }

    @PostMapping
    public Result<Void> create(@RequestBody Customer customer) {
        return customerService.create(customer);
    }

    @PutMapping
    public Result<Void> update(@RequestBody Customer customer) {
        return customerService.update(customer);
    }

    @PutMapping("/{id}/risk-level")
    public Result<Void> updateRiskLevel(@PathVariable Long id, @RequestParam String riskLevel) {
        return customerService.updateRiskLevel(id, riskLevel);
    }
}
