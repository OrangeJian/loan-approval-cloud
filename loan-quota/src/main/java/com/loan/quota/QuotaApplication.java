package com.loan.quota;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.loan.quota.mapper")
@ComponentScan(basePackages = {"com.loan.quota", "com.loan.common"})
public class QuotaApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuotaApplication.class, args);
    }
}
