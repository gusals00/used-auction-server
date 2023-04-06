package com.auction.usedauction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://bkkang1.github.io",
                        "https://usedauction.shop",
                        "http://localhost:3001",
                        "https://localhost:3001",
                        "https://localhost:8080",
                        "http://localhost:8080",
                        "https://127.0.0.1:3001")
//                .allowedOrigins("*")
                .allowCredentials(true);
    }
}