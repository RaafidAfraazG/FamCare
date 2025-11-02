package com.famcare.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI famCareAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FamCare API")
                        .description("Family Mental Health Tracking System API Documentation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your.email@college.edu")));
    }
}