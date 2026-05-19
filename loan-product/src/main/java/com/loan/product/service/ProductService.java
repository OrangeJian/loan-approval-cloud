package com.loan.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.loan.common.constant.ErrorCode;
import com.loan.common.dto.Result;
import com.loan.common.entity.Product;
import com.loan.common.util.JsonUtil;
import com.loan.product.dto.ProductCreateDTO;
import com.loan.product.dto.ProductQueryDTO;
import com.loan.product.dto.ProductUpdateDTO;
import com.loan.product.dto.ProductVO;
import com.loan.product.dto.strategy.AmountStrategy;
import com.loan.product.dto.strategy.ApprovalStrategy;
import com.loan.product.dto.strategy.InterestStrategy;
import com.loan.product.mapper.ProductMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    private static final String PRODUCT_CACHE_KEY = "product:";

    public Result<Page<ProductVO>> list(ProductQueryDTO queryDTO) {
        Page<Product> pageInfo = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.like(Product::getProductName, queryDTO.getKeyword())
                    .or()
                    .like(Product::getProductCode, queryDTO.getKeyword());
        }
        if (queryDTO.getProductType() != null && !queryDTO.getProductType().isEmpty()) {
            wrapper.eq(Product::getProductType, queryDTO.getProductType());
        }
        wrapper.orderByDesc(Product::getCreatedTime);
        productMapper.selectPage(pageInfo, wrapper);

        Page<ProductVO> voPage = new Page<>(pageInfo.getCurrent(), pageInfo.getSize(), pageInfo.getTotal());
        voPage.setRecords(pageInfo.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return Result.success(voPage);
    }

    public Result<List<ProductVO>> listAll(String productType) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, "1");
        if (productType != null && !productType.isEmpty()) {
            wrapper.eq(Product::getProductType, productType);
        }
        List<ProductVO> voList = productMapper.selectList(wrapper)
                .stream().map(this::toVO).collect(Collectors.toList());
        return Result.success(voList);
    }

    public Result<ProductVO> getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.error(Integer.parseInt(ErrorCode.PRODUCT_NOT_FOUND), "产品不存在");
        }
        return Result.success(toVO(product));
    }

    public Result<ProductVO> getByProductCode(String productCode) {
        String cacheKey = PRODUCT_CACHE_KEY + productCode;
        if (redisTemplate != null) {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                try {
                    Product product = JsonUtil.fromJson(cached, Product.class);
                    return Result.success(toVO(product));
                } catch (Exception e) {
                    redisTemplate.delete(cacheKey);
                }
            }
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getProductCode, productCode);
        Product product = productMapper.selectOne(wrapper);
        if (product == null) {
            return Result.error(Integer.parseInt(ErrorCode.PRODUCT_NOT_FOUND), "产品不存在");
        }

        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey, JsonUtil.toJson(product));
            } catch (Exception e) {
                // ignore cache write failure
            }
        }
        return Result.success(toVO(product));
    }

    public Result<Void> create(ProductCreateDTO dto) {
        Product product = toEntity(dto);
        product.setStatus("1");
        productMapper.insert(product);
        return Result.success();
    }

    public Result<Void> update(ProductUpdateDTO dto) {
        Product product = toEntity(dto);
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
        Result<ProductVO> productResult = getByProductCode(productCode);
        if (!productResult.isSuccess() || productResult.getData() == null) {
            return null;
        }
        ProductVO product = productResult.getData();

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

    // === 转换方法 ===

    private ProductVO toVO(Product entity) {
        if (entity == null) return null;
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(entity, vo);
        if (entity.getAmountStrategy() != null) {
            vo.setAmountStrategy(JsonUtil.fromJson(entity.getAmountStrategy(), AmountStrategy.class));
        }
        if (entity.getInterestStrategy() != null) {
            vo.setInterestStrategy(JsonUtil.fromJson(entity.getInterestStrategy(), InterestStrategy.class));
        }
        if (entity.getApprovalStrategy() != null) {
            vo.setApprovalStrategy(JsonUtil.fromJson(entity.getApprovalStrategy(), ApprovalStrategy.class));
        }
        return vo;
    }

    private Product toEntity(ProductCreateDTO dto) {
        if (dto == null) return null;
        Product entity = new Product();
        BeanUtils.copyProperties(dto, entity);
        if (dto.getAmountStrategy() != null) {
            entity.setAmountStrategy(JsonUtil.toJson(dto.getAmountStrategy()));
        }
        if (dto.getInterestStrategy() != null) {
            entity.setInterestStrategy(JsonUtil.toJson(dto.getInterestStrategy()));
        }
        if (dto.getApprovalStrategy() != null) {
            entity.setApprovalStrategy(JsonUtil.toJson(dto.getApprovalStrategy()));
        }
        return entity;
    }

    private Product toEntity(ProductUpdateDTO dto) {
        if (dto == null) return null;
        Product entity = new Product();
        BeanUtils.copyProperties(dto, entity);
        if (dto.getAmountStrategy() != null) {
            entity.setAmountStrategy(JsonUtil.toJson(dto.getAmountStrategy()));
        }
        if (dto.getInterestStrategy() != null) {
            entity.setInterestStrategy(JsonUtil.toJson(dto.getInterestStrategy()));
        }
        if (dto.getApprovalStrategy() != null) {
            entity.setApprovalStrategy(JsonUtil.toJson(dto.getApprovalStrategy()));
        }
        return entity;
    }
}
