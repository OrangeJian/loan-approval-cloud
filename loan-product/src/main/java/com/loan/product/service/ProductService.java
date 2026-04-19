package com.loan.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.loan.common.dto.Result;
import com.loan.common.entity.Product;
import com.loan.product.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    private static final String PRODUCT_CACHE_KEY = "product:";

    public Result<Page<Product>> list(int pageNum, int pageSize, String keyword, String productType) {
        Page<Product> pageInfo = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getProductName, keyword)
                    .or()
                    .like(Product::getProductCode, keyword);
        }
        if (productType != null && !productType.isEmpty()) {
            wrapper.eq(Product::getProductType, productType);
        }
        wrapper.orderByDesc(Product::getCreatedTime);
        productMapper.selectPage(pageInfo, wrapper);
        return Result.success(pageInfo);
    }

    public Result<List<Product>> listAll(String productType) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, "1");
        if (productType != null && !productType.isEmpty()) {
            wrapper.eq(Product::getProductType, productType);
        }
        return Result.success(productMapper.selectList(wrapper));
    }

    public Result<Product> getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.error("产品不存在");
        }
        return Result.success(product);
    }

    public Result<Product> getByProductCode(String productCode) {
        String cacheKey = PRODUCT_CACHE_KEY + productCode;
        if (redisTemplate != null) {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Product product = objectMapper.readValue(cached, Product.class);
                    return Result.success(product);
                } catch (Exception e) {
                    redisTemplate.delete(cacheKey);
                }
            }
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getProductCode, productCode);
        Product product = productMapper.selectOne(wrapper);
        if (product != null && redisTemplate != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(product));
            } catch (Exception e) {
            }
        }
        return Result.success(product);
    }

    public Result<Void> create(Product product) {
        product.setStatus("1");
        productMapper.insert(product);
        return Result.success();
    }

    public Result<Void> update(Product product) {
        productMapper.updateById(product);
        if (redisTemplate != null) {
            redisTemplate.delete(PRODUCT_CACHE_KEY + product.getProductCode());
        }
        return Result.success();
    }

    public Result<Void> delete(Long id) {
        Product product = productMapper.selectById(id);
        if (product != null && redisTemplate != null) {
            redisTemplate.delete(PRODUCT_CACHE_KEY + product.getProductCode());
        }
        productMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 计算利率（根据策略）
     */
    public BigDecimal calculateInterestRate(String productCode, BigDecimal amount, Integer term, String riskLevel) {
        Result<Product> productResult = getByProductCode(productCode);
        if (!productResult.isSuccess() || productResult.getData() == null) {
            return null;
        }
        Product product = productResult.getData();

        // 简化利率计算：基础利率 + 风险等级调整
        BigDecimal baseRate = product.getMinInterestRate();
        if (riskLevel != null) {
            switch (riskLevel) {
                case "HIGH":
                    baseRate = baseRate.add(new BigDecimal("0.005"));
                    break;
                case "MEDIUM":
                    baseRate = baseRate.add(new BigDecimal("0.002"));
                    break;
                case "LOW":
                    baseRate = baseRate.subtract(new BigDecimal("0.001"));
                    break;
            }
        }
        return baseRate;
    }
}
