package com.jobrecruitment.backend.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger Configuration - Cấu hình OpenAPI cho tài liệu API
 * 
 * Tính năng chính:
 * - Xác thực JWT Bearer Token trong Swagger UI
 * - Tài liệu API với metadata
 * - Yêu cầu bảo mật toàn cục cho các endpoint được bảo vệ
 * 
 * Truy cập Swagger UI: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Job Recruitment Platform API")
                        .description("Comprehensive RESTful API for Job Recruitment System connecting Employers and Candidates")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Development Team")
                                .email("dev@jobrecruitment.com")
                                .url("https://jobrecruitment.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                // Không có yêu cầu bảo mật toàn cục
                // Bảo mật được áp dụng cho từng endpoint thông qua chú thích @SecurityRequirement
                // Điều này cho phép các endpoint công khai được kiểm thử mà không cần xác thực trong Swagger UI
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token obtained from /api/auth/login endpoint")));
    }
}
