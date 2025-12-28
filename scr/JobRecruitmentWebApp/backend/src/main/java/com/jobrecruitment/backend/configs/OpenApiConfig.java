package com.jobrecruitment.backend.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

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
                        .description("""
                        # API documentation for the Job Recruitment Platform.
                        This platform allows employers to post job listings and candidates to apply for jobs.
                        
                        RESTful API for Job Recruitment Platform providing:
                        - **User Management:** Registration, Authentication (JWT), Profile Management
                        - **Job Listings:** Create, Read, Update, Delete job postings
                        - **Applications:** Apply for jobs, View application status
                        - **Admin Features:** User role management, Job listing approvals
                        - **Authentication:** JWT-based authentication for secure access

                        ## Authentication
                        This API uses JWT (JSON Web Tokens) for authentication. To access protected:
                        1. Obtain a JWT token by logging in via the `/api/auth/login` endpoint.
                        2. Copy the `token` from the response.
                        3. In Swagger UI, click on the "Authorize" button and enter the token in the format: `Bearer <your_token_here>`.
                        4. After authorization, you can access the protected endpoints.

                        ## Authorization Rules
                        - **Public Endpoints:** Some endpoints are publicly accessible without authentication (e.g., job listings).
                        - **Protected Endpoints:** Endpoints that require authentication are marked with a lock icon in Swagger UI.
                        - **Role-Based Access:** Certain actions may require specific user roles (e.g., admin privileges).
                                - Employers can create and manage job listings.
                                - Candidates can apply for jobs and manage their applications.
                                - Admins can manage users and oversee platform activities.
                        ## API Conventions
                        - All endpoints follow RESTful conventions.
                        - Use standard HTTP methods: GET, POST, PUT, DELETE.
                        - All responses follow standard JSON format:
                          ```json
                          {
                              "status": "success",
                              "data": { ... },
                              "message": "Descriptive message"
                          }
                          ```
                        - Request and response bodies are in JSON format.
                        - Auto-generated codes: Company (DN), Job (VL), Candidate (UV), Application (DX)
                                        """)
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
                                .description("""
                                Enter your JWT token in the format: `Bearer <your_token_here>`

                                To obtain a token:
                                1. Use the `/api/auth/login` endpoint with valid credentials.
                                2. Copy the `token` from the response.
                                3. Click the "Authorize" button in Swagger UI and enter the token.
                                4. Access protected endpoints as needed.
                                                """)))
                
                .addServersItem(new Server()
                        .url("http://localhost:5000")
                        .description("Local Development Server"))
                .addServersItem(new Server()
                        .url("https://api.jobrecruitment.com")
                        .description("Production Server"));
    }
}
