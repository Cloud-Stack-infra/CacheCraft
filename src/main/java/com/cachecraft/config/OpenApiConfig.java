package com.cachecraft.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cacheCraftOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CacheCraft API")
                        .description("Adaptive caching system using Caffeine and Redis")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CacheCraft Team")
                                .email("support@cachecraft.com")));
    }
}