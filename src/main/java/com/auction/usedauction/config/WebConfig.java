package com.auction.usedauction.config;

import com.auction.usedauction.util.AuthConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    public static final String ALLOWED_METHOD_NAMES = "GET,HEAD,POST,PUT,DELETE,TRACE,OPTIONS,PATCH";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods(ALLOWED_METHOD_NAMES.split(","))
                .allowedOrigins("https://bkkang1.github.io")
                .maxAge(3000)
                .exposedHeaders(AuthConstants.AUTH_HEADER)
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}