package com.loan.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.loan.common.dto.Result;
import com.loan.common.entity.Product;
import com.loan.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String productType) {
        return productService.list(pageNum, pageSize, keyword, productType);
    }

    @GetMapping("/all")
    public Result<List<Product>> listAll(@RequestParam(name = "productType", required = false) String productType) {
        return productService.listAll(productType);
    }

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @GetMapping("/code/{productCode}")
    public Result<Product> getByProductCode(@PathVariable String productCode) {
        return productService.getByProductCode(productCode);
    }

    @PostMapping
    public Result<Void> create(@RequestBody Product product) {
        return productService.create(product);
    }

    @PutMapping
    public Result<Void> update(@RequestBody Product product) {
        return productService.update(product);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return productService.delete(id);
    }

    @GetMapping("/calculate-rate")
    public Result<BigDecimal> calculateRate(
            @RequestParam String productCode,
            @RequestParam BigDecimal amount,
            @RequestParam Integer term,
            @RequestParam(required = false) String riskLevel) {
        BigDecimal rate = productService.calculateInterestRate(productCode, amount, term, riskLevel);
        return Result.success(rate);
    }
}
