package com.loan.product.controller;

import com.loan.common.constant.ErrorCode;
import com.loan.common.dto.Result;
import com.loan.product.dto.ProductCreateDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.loan.product.dto.ProductQueryDTO;
import com.loan.product.dto.ProductUpdateDTO;
import com.loan.product.dto.ProductVO;
import com.loan.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public Result<Page<ProductVO>> list(@Valid ProductQueryDTO queryDTO) {
        return productService.list(queryDTO);
    }

    @GetMapping("/all")
    public Result<List<ProductVO>> listAll(@RequestParam(required = false) String productType) {
        return productService.listAll(productType);
    }

    @GetMapping("/{id}")
    public Result<ProductVO> getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @GetMapping("/code/{productCode}")
    public Result<ProductVO> getByProductCode(@PathVariable String productCode) {
        return productService.getByProductCode(productCode);
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid ProductCreateDTO dto) {
        return productService.create(dto);
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Valid ProductUpdateDTO dto) {
        return productService.update(dto);
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.error(Integer.parseInt(ErrorCode.PARAM_ERROR), message);
    }
}
