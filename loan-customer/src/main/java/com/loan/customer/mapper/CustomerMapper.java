package com.loan.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.loan.common.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
