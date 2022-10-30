package com.syf.category;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CategoryCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(CategoryCenterApplication.class, args);
    }
}
