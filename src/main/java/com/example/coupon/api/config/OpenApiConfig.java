package com.example.coupon.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Coupon Service API")
                        .description("REST service for managing discount coupons with country-based geo restrictions and per-user limits.")
                        .version("1.0.0"));
    }
}
