package com.loan.customer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.loan.common.dto.Result;
import com.loan.common.entity.Customer;
import com.loan.customer.mapper.CustomerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CUSTOMER_CACHE_KEY = "customer:";

    public Result<Page<Customer>> list(int pageNum, int pageSize, String keyword) {
        Page<Customer> pageInfo = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Customer::getName, keyword)
                    .or()
                    .like(Customer::getPhone, keyword)
                    .or()
                    .like(Customer::getIdCard, keyword);
        }
        wrapper.orderByDesc(Customer::getCreatedTime);
        customerMapper.selectPage(pageInfo, wrapper);
        return Result.success(pageInfo);
    }

    public Result<Customer> getById(Long id) {
        String cacheKey = CUSTOMER_CACHE_KEY + id;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return Result.success(new com.fasterxml.jackson.databind.ObjectMapper().readValue(cached, Customer.class));
            } catch (Exception e) {
                // ignore
            }
        }

        Customer customer = customerMapper.selectById(id);
        if (customer != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey, new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(customer));
            } catch (Exception e) {
                // ignore
            }
        }
        return Result.success(customer);
    }

    public Result<Customer> getByCustomerNo(String customerNo) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getCustomerNo, customerNo);
        return Result.success(customerMapper.selectOne(wrapper));
    }

    public Result<Void> create(Customer customer) {
        customer.setStatus("1");
        customer.setCustomerNo("C" + System.currentTimeMillis());
        customerMapper.insert(customer);
        return Result.success();
    }

    public Result<Void> update(Customer customer) {
        customerMapper.updateById(customer);
        redisTemplate.delete(CUSTOMER_CACHE_KEY + customer.getId());
        return Result.success();
    }

    public Result<Void> updateRiskLevel(Long id, String riskLevel) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setRiskLevel(riskLevel);
        customerMapper.updateById(customer);
        redisTemplate.delete(CUSTOMER_CACHE_KEY + id);
        return Result.success();
    }
}
