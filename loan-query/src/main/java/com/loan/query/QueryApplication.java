package com.loan.query;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.loan.query", "com.loan.common"})
public class QueryApplication {
    public static void main(String[] args) {
        SpringApplication.run(QueryApplication.class, args);
    }
}
