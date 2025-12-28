package com.jobrecruitment.backend.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig - Cấu hình chung cho ứng dụng web
 * 
 * Chức năng:
 * - Cấu hình ObjectMapper cho JSON serialization/deserialization
 * - Hỗ trợ Java 8 DateTime API (LocalDate, LocalDateTime)
 * - Định dạng JSON response chuẩn
 * - Cấu hình Resource Handler để serve static files (uploads)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Bean ObjectMapper - Chuyển đổi giữa Java objects và JSON
     * 
     * Cấu hình:
     * - JavaTimeModule: Hỗ trợ LocalDate, LocalDateTime, ZonedDateTime
     * - WRITE_DATES_AS_TIMESTAMPS: false - Xuất date dạng ISO-8601 string thay vì timestamp
     * 
     * Ví dụ output:
     * - LocalDate: "2025-01-20"
     * - LocalDateTime: "2025-01-20T14:30:00"
     * 
     * @return ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Đăng ký module hỗ trợ Java 8 DateTime
        mapper.registerModule(new JavaTimeModule());
        
        // Xuất date dạng string ISO-8601 thay vì timestamp số
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper;
    }

    /**
     * Cấu hình Resource Handler để serve static files
     * 
     * Mapping:
     * - URL Pattern: /uploads/**
     * - Physical Location: file:./uploads/
     * 
     * Ví dụ:
     * - Request: http://localhost:8080/uploads/logos/uuid-logo.png
     * - File Path: ./uploads/logos/uuid-logo.png
     * 
     * Production Note:
     * - Development: Serve files trực tiếp từ Spring Boot
     * - Production: Nên dùng Nginx hoặc CDN để serve static files
     * 
     * @param registry ResourceHandlerRegistry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình serve files từ thư mục uploads
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(3600); // Cache 1 hour (3600 seconds)
    }
}
