package com.loan.credit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.loan.credit.mapper")
@ComponentScan(basePackages = {"com.loan.credit", "com.loan.common"})
public class CreditApplication {
    public static void main(String[] args) {
        SpringApplication.run(CreditApplication.class, args);
    }
}
