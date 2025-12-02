package de.dicecup.classlink;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI caseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dicecup School Management API")
                        .description("""
                                Dicecup is a modern school management platform designed to streamline
                                grading, user administration, and classroom workflows.
                                
                                This API provides secure, role-based access to all core functions, including:
                                - Authentication & Authorization (JWT-based)
                                - User & Role Management
                                - Class, Grade, and Invitation Management
                                - Reporting and Audit-Ready Data Operations
                                
                                All endpoints follow REST principles and require proper authentication
                                unless explicitly stated otherwise.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Dicecup Support")
                                .email("support@dicecup.com"))
                        .license(new License()
                                .name("MIT License")
                                .identifier("MIT"))
                        .termsOfService("https://dicecup.com/terms")
                );
    }
}
