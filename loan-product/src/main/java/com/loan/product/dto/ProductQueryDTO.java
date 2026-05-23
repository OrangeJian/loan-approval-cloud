package com.loan.product.dto;

import lombok.Data;

/**
 * 产品分页查询 DTO
 */
@Data
public class ProductQueryDTO {

    private int pageNum = 1;

    private int pageSize = 10;

    private String keyword;

    private String productType;

    private String status;
}
