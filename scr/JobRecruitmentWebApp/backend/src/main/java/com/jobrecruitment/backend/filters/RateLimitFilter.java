package com.jobrecruitment.backend.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobrecruitment.backend.configs.RateLimitConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RateLimitFilter - Bộ lọc Rate Limiting sử dụng Bucket4j
 * 
 * Mục đích:
 * - Bảo vệ API khỏi lạm dụng, spam, và tấng công DDoS
 * - Giới hạn số lượng request theo IP (Public) hoặc User ID (Authenticated)
 * 
 * Cơ chế hoạt động:
 * 1. Request vào hệ thống
 * 2. Kiểm tra request đã authenticate chưa (có JWT token không?)
 * 3a. Nếu chưa authenticate (Public):
 *     - Lấy IP address làm key
 *     - Áp dụng Public Rate Limit (10 requests/minute)
 * 3b. Nếu đã authenticate:
 *     - Lấy User ID từ JWT token làm key
 *     - Áp dụng Authenticated Rate Limit (50 requests/minute)
 * 4. Kiểm tra bucket của key:
 *     - Nếu còn token: Cho phép request, trừ 1 token
 *     - Nếu hết token: Trả về 429 Too Many Requests
 * 
 * Token Bucket Algorithm (Bucket4j):
 * - Mỗi key (IP hoặc User ID) có 1 bucket riêng
 * - Bucket chứa số lượng token giới hạn (capacity)
 * - Mỗi request tiêu tốn 1 token
 * - Token được nạp lại theo thời gian (refill rate)
 * 
 * Ví dụ:
 * - User UV12345678 (authenticated):
 *   - Bucket capacity: 50 tokens
 *   - Refill: 50 tokens/minute
 *   - Gọi 50 requests liên tiếp: OK
 *   - Request thứ 51: 429 Too Many Requests
 *   - Sau 1 phút: Bucket được nạp lại 50 tokens
 * 
 * - Anonymous user từ IP 192.168.1.1 (public):
 *   - Bucket capacity: 10 tokens
 *   - Refill: 10 tokens/minute
 *   - Gọi 10 requests liên tiếp: OK
 *   - Request thứ 11: 429 Too Many Requests
 * 
 * HTTP Response khi vượt giới hạn:
 * {
 *   "status": 429,
 *   "message": "Too Many Requests. Please try again later.",
 *   "limit": "10 requests per minute",
 *   "retryAfter": 60
 * }
 * 
 * Lưu ý:
 * - Filter này chạy TRƯỚC SecurityFilterChain (đăng ký trong SecurityConfig)
 * - Bucket được lưu trong memory (ConcurrentHashMap) - production nên dùng Redis
 * - IP address có thể bị spoof - production nên kết hợp với Cloudflare/AWS WAF
 * 
 * @see RateLimitConfig
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper;
    
    /**
     * Lưu trữ bucket cho mỗi key (IP hoặc User ID)
     * Key: IP address hoặc User ID
     * Value: Bucket object chứa token
     * 
     * Lưu ý: Production nên dùng Redis hoặc Hazelcast để:
     * - Shared state giữa nhiều server instances
     * - Persistence khi server restart
     * - TTL tự động xóa bucket cũ
     */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    /**
     * Xử lý mỗi HTTP request
     * 
     * Logic:
     * 1. Lấy key (IP hoặc User ID)
     * 2. Lấy hoặc tạo bucket cho key
     * 3. Kiểm tra bucket còn token không (tryConsume)
     * 4. Nếu còn: Cho phép request đi tiếp
     * 5. Nếu hết: Trả về 429 Too Many Requests
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Filter chain để request đi tiếp
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Bước 1: Xác định key và bucket type
        String key;
        final boolean isAuthenticated;
        
        // Kiểm tra request đã authenticate chưa
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
            && !"anonymousUser".equals(authentication.getPrincipal())) {
            // User đã authenticate: Sử dụng User ID làm key
            key = "user:" + authentication.getName();
            isAuthenticated = true;
        } else {
            // User chưa authenticate: Sử dụng IP address làm key
            key = "ip:" + getClientIP(request);
            isAuthenticated = false;
        }
        
        // Bước 2: Lấy hoặc tạo bucket cho key
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(isAuthenticated));
        
        // Bước 3: Kiểm tra bucket có còn token không
        if (bucket.tryConsume(1)) {
            // Còn token: Cho phép request đi tiếp
            log.debug("Rate limit passed for key: {}", key);
            filterChain.doFilter(request, response);
        } else {
            // Hết token: Trả về 429 Too Many Requests
            log.warn("Rate limit exceeded for key: {}", key);
            sendRateLimitExceededResponse(response, isAuthenticated);
        }
    }
    
    /**
     * Tạo bucket mới với cấu hình tương ứng
     * 
     * @param isAuthenticated true nếu user đã authenticate, false nếu public
     * @return Bucket object với bandwidth đã cấu hình
     */
    private Bucket createBucket(boolean isAuthenticated) {
        Bandwidth bandwidth;
        
        if (isAuthenticated) {
            // Authenticated user: High trust, more requests allowed
            long capacity = rateLimitConfig.getAuthenticated().getCapacity();
            long tokens = rateLimitConfig.getAuthenticated().getRefill().getTokens();
            long minutes = rateLimitConfig.getAuthenticated().getRefill().getMinutes();
            
            bandwidth = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(tokens, Duration.ofMinutes(minutes))
                .build();
            
            log.debug("Created authenticated bucket: capacity={}, refill={} tokens/{} minutes", 
                     capacity, tokens, minutes);
        } else {
            // Public/Anonymous: Low trust, fewer requests allowed
            long capacity = rateLimitConfig.getPublicLimit().getCapacity();
            long tokens = rateLimitConfig.getPublicLimit().getRefill().getTokens();
            long minutes = rateLimitConfig.getPublicLimit().getRefill().getMinutes();
            
            bandwidth = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(tokens, Duration.ofMinutes(minutes))
                .build();
            
            log.debug("Created public bucket: capacity={}, refill={} tokens/{} minutes", 
                     capacity, tokens, minutes);
        }
        
        return Bucket.builder()
            .addLimit(bandwidth)
            .build();
    }
    
    /**
     * Lấy IP address của client
     * 
     * Xử lý trường hợp request đi qua proxy/load balancer:
     * - Ưu tiên: X-Forwarded-For header (real client IP)
     * - Fallback: X-Real-IP header
     * - Fallback: getRemoteAddr() (proxy IP)
     * 
     * Lưu ý: Production nên validate X-Forwarded-For để tránh IP spoofing
     * 
     * @param request HTTP request
     * @return IP address của client
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For có thể chứa nhiều IP (client, proxy1, proxy2...)
            // Lấy IP đầu tiên (real client IP)
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Gửi response 429 Too Many Requests
     * 
     * Response format:
     * {
     *   "status": 429,
     *   "message": "Too Many Requests. Please try again later.",
     *   "limit": "10 requests per minute",
     *   "retryAfter": 60
     * }
     * 
     * @param response HTTP response
     * @param isAuthenticated true nếu user đã authenticate
     */
    private void sendRateLimitExceededResponse(HttpServletResponse response, boolean isAuthenticated) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // Xác định limit và retry after
        String limit;
        long retryAfter;
        if (isAuthenticated) {
            long capacity = rateLimitConfig.getAuthenticated().getCapacity();
            long minutes = rateLimitConfig.getAuthenticated().getRefill().getMinutes();
            limit = capacity + " requests per " + minutes + " minute" + (minutes > 1 ? "s" : "");
            retryAfter = minutes * 60; // seconds
        } else {
            long capacity = rateLimitConfig.getPublicLimit().getCapacity();
            long minutes = rateLimitConfig.getPublicLimit().getRefill().getMinutes();
            limit = capacity + " requests per " + minutes + " minute" + (minutes > 1 ? "s" : "");
            retryAfter = minutes * 60; // seconds
        }
        
        // Thêm Retry-After header (RFC 6585)
        response.setHeader("Retry-After", String.valueOf(retryAfter));
        
        // Tạo response body
        Map<String, Object> errorResponse = Map.of(
            "status", HttpStatus.TOO_MANY_REQUESTS.value(),
            "message", "Too Many Requests. Please try again later.",
            "limit", limit,
            "retryAfter", retryAfter
        );
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
