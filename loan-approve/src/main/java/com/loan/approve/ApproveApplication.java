package com.loan.approve;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.loan.approve.mapper")
@ComponentScan(basePackages = {"com.loan.approve", "com.loan.common"})
public class ApproveApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApproveApplication.class, args);
    }
}
