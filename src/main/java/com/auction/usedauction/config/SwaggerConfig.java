package com.auction.usedauction.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private final String jwt = "JWT";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(components())
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(jwt)); // 헤더에 토큰 포함
    }

    private Info apiInfo() {
        return new Info()
                .title("중고경매플랫폼")
                .description("창의융합종합설계2 - 중고경매플랫폼 API")
                .version("1.0");

    }


    private Components components() {
        return new Components().addSecuritySchemes(jwt, new SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
        );
    }
}
