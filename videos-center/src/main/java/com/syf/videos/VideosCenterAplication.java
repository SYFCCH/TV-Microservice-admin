package com.syf.videos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class VideosCenterAplication {
    public static void main(String[] args) {
        SpringApplication.run(VideosCenterAplication.class,args);
    }
}
