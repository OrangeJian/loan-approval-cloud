package com.loan.quota;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.loan.quota.mapper")
public class QuotaApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuotaApplication.class, args);
    }
}
