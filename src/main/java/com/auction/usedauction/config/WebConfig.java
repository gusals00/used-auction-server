package com.auction.usedauction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    public static final String ALLOWED_METHOD_NAMES = "GET,HEAD,POST,PUT,DELETE,TRACE,OPTIONS,PATCH";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
//                .allowedMethods(ALLOWED_METHOD_NAMES.split(","));
//                .allowedOrigins("https://bkkang1.github.io",
////                        "https://usedauction.shop",
//                        "http://localhost:3001",
//                        "https://localhost:3001",
//                        "http://112.217.167.202:3001",
//                        "https://112.217.167.202:3001",
//                        "https://localhost:8080",
//                        "http://localhost:8080",
//                        "https://127.0.0.1:3001",
//
//                        "http://106.101.0.62:3001",
//                        "https://106.101.0.62:3001",
//                        "https://106.101.0.62",
//
//                        "http://172.24.64.1:3001",
//                        "https://172.24.64.1:3001",
//                        "https://172.24.64.1",
//
//                        "http://192.168.186.159",
//                        "http://192.168.186.159:3001",
//                        "https://192.168.186.159:3001",
//                        "https://192.168.186.159"

//                        )
                .allowedOrigins("*")

                .allowCredentials(true);
    }
}