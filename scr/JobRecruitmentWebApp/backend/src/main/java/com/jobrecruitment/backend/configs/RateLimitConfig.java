package com.jobrecruitment.backend.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * RateLimitConfig - Cấu hình Rate Limiting cho hệ thống
 * 
 * Mục đích:
 * - Bảo vệ API khỏi lạm dụng, spam, và tấn công DDoS
 * - Giới hạn số lượng request trong một khoảng thời gian
 * - Phân biệt giới hạn cho Public endpoints và Authenticated endpoints
 * 
 * Chiến lược:
 * 1. Public Endpoints (Low Trust):
 *    - Giới hạn theo IP Address
 *    - Mặc định: 10 requests/minute
 *    - Use case: Đăng nhập, đăng ký, xem Job (chưa authenticate)
 * 
 * 2. Authenticated Endpoints (High Trust):
 *    - Giới hạn theo User ID (từ JWT token)
 *    - Mặc định: 50 requests/minute
 *    - Use case: Nộp đơn, cập nhật profile, đăng tin tuyển dụng
 * 
 * Token Bucket Algorithm:
 * - Capacity: Số token tối đa trong bucket (giới hạn burst traffic)
 * - Refill Tokens: Số token được nạp lại sau mỗi khoảng thời gian
 * - Refill Period: Thời gian nạp lại token (minutes)
 * 
 * Ví dụ (Public):
 * - Capacity = 10, Refill = 10 tokens/1 minute
 * - User gọi 10 requests liên tiếp: OK (bucket rỗng)
 * - User gọi request thứ 11: 429 Too Many Requests
 * - Sau 1 phút: Bucket được nạp lại 10 tokens
 * 
 * Configuration trong application.properties:
 * rate.limit.public.capacity=10
 * rate.limit.public.refill.tokens=10
 * rate.limit.public.refill.minutes=1
 * rate.limit.authenticated.capacity=50
 * rate.limit.authenticated.refill.tokens=50
 * rate.limit.authenticated.refill.minutes=1
 * 
 * @see RateLimitFilter
 */
@Configuration
@ConfigurationProperties(prefix = "rate.limit")
@Getter
@Setter
public class RateLimitConfig {
    
    /**
     * Cấu hình cho Public Endpoints (Low Trust)
     */
    private PublicLimit publicLimit = new PublicLimit();
    
    /**
     * Cấu hình cho Authenticated Endpoints (High Trust)
     */
    private AuthenticatedLimit authenticated = new AuthenticatedLimit();
    
    /**
     * Cấu hình Rate Limit cho Public Endpoints
     */
    @Getter
    @Setter
    public static class PublicLimit {
        /**
         * Số token tối đa trong bucket (giới hạn burst traffic)
         * Mặc định: 50 requests
         */
        private long capacity = 50;
        
        /**
         * Cấu hình nạp lại token
         */
        private Refill refill = new Refill();
    }
    
    /**
     * Cấu hình Rate Limit cho Authenticated Endpoints
     */
    @Getter
    @Setter
    public static class AuthenticatedLimit {
        /**
         * Số token tối đa trong bucket
         * Mặc định: 100 requests
         */
        private long capacity = 100;
        
        /**
         * Cấu hình nạp lại token
         */
        private Refill refill = new Refill();
    }
    
    /**
     * Cấu hình nạp lại token
     */
    @Getter
    @Setter
    public static class Refill {
        /**
         * Số token được nạp lại
         * Mặc định: 50 (Public), 100 (Authenticated)
         */
        private long tokens = 50;
        
        /**
         * Thời gian nạp lại token (phút)
         * Mặc định: 1 minute
         */
        private long minutes = 1;
    }
}
