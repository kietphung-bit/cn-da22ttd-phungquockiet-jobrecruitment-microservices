package com.jobrecruitment.backend.configs;

import com.jobrecruitment.backend.filters.JwtAuthenticationFilter;
import com.jobrecruitment.backend.filters.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * SecurityConfig - Cấu hình bảo mật Spring Security cho hệ thống
 * 
 * Phiên bản: Spring Security 6 - Lambda DSL (cú pháp hiện đại)
 * 
 * Tính năng chính:
 * 1. Xác thực JWT (JSON Web Token):
 *    - Stateless session (không lưu session trên server)
 *    - JWT token được gửi qua Header: Authorization: Bearer <token>
 *    - JwtAuthenticationFilter kiểm tra và giải mã token trước khi vào controller
 * 
 * 2. Phân quyền theo vai trò (RBAC - Role-Based Access Control):
 *    - ADM (Admin): Quản trị toàn hệ thống
 *    - DN (Doanh nghiệp): Nhà tuyển dụng, đăng tin, quản lý ứng viên
 *    - UV (Ứng viên): Tìm việc, ứng tuyển, quản lý CV
 * 
 * 3. CORS (Cross-Origin Resource Sharing):
 *    - Cho phép React frontend (localhost:3000, localhost:5173) gọi request
 *    - Cho phép Swagger UI (cùng origin) hoạt động
 *    - Cho phép Authorization header cho JWT
 * 
 * 4. Mã hoá mật khẩu:
 *    - BCrypt: Thuật toán mã hoá mạnh, không giải mã ngược
 *    - Salt tự động: Mỗi mật khẩu có salt khác nhau
 * 
 * 5. Method-level security:
 *    - @EnableMethodSecurity: Bật tính năng @PreAuthorize, @PostAuthorize
 *    - Kiểm tra quyền chi tiết tại từng method trong controller
 * 
 * 6. Rate Limiting:
 *    - RateLimitFilter: Giới hạn số lượng request để bảo vệ khỏi DDoS
 *    - Public endpoints: 10 requests/minute (theo IP)
 *    - Authenticated endpoints: 50 requests/minute (theo User ID)
 * 
 * Kiến trúc bảo mật:
 * Request -> RateLimitFilter -> CORS Filter -> JWT Filter -> SecurityFilterChain -> Controller
 *         -> Exception Handler (401/403/429)
 * 
 * Phụ thuộc:
 * - JwtAuthenticationFilter: Filter custom kiểm tra JWT token
 * - RateLimitFilter: Filter custom giới hạn request rate
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    
    /**
     * Bean PasswordEncoder - Mã hoá mật khẩu bằng BCrypt
     * 
     * Tham khảo: Section 4.1 - Password must be hashed using BCrypt
     * 
     * BCrypt là gì?
     * - Thuật toán mã hoá mật khẩu mạnh, không giải mã ngược được
     * - Tự động tạo salt (chuỗi ngẫu nhiên) cho mỗi mật khẩu
     * - Chậm (intentionally slow) để chống brute-force attack
     * 
     * Ví dụ:
     * - Input: "password123"
     * - Output: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     * 
     * Sử dụng:
     * - Khi đăng ký: passwordEncoder.encode(rawPassword) -> Lưu vào database
     * - Khi đăng nhập: passwordEncoder.matches(rawPassword, encodedPassword) -> Kiểm tra
     * 
     * @return BCryptPasswordEncoder - Encoder mã hoá mật khẩu
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Bean SecurityFilterChain - Cấu hình chuỗi bộ lọc bảo mật
     * 
     * Đây là phần CORỄ của Spring Security - Định nghĩa quy tắc bảo mật cho HTTP request
     * 
     * LUỒNG XẬC THỰC JWT (JWT Authentication Flow):
     * 1. Request vào hệ thống -> JwtAuthenticationFilter (bộ lọc tự viết)
     * 2. JwtAuthenticationFilter kiểm tra:
     *    - Có Authorization header không?
     *    - Header có format "Bearer <token>" không?
     *    - Token có hợp lệ không? (chữ ký, thời gian hết hạn)
     * 3. Nếu token hợp lệ:
     *    - Giải mã token để lấy username và role
     *    - Tạo Authentication object
     *    - Đặt vào SecurityContext (Spring Security biết user đã đăng nhập)
     * 4. Request tiếp tục vào SecurityFilterChain:
     *    - Kiểm tra quyền truy cập (authorization): .requestMatchers(...).permitAll() / .authenticated()
     * 5. Nếu pass kiểm tra: Request vào Controller
     *    - Controller kiểm tra @PreAuthorize("hasAuthority('ROLE_ADM')")
     * 6. Nếu không pass:
     *    - 401 Unauthorized: Chưa đăng nhập (không có token hoặc token không hợp lệ)
     *    - 403 Forbidden: Đã đăng nhập nhưng không đủ quyền (sai role)
     * 
     * CẤU TRÚC CẤU HÌNH:
     * - CSRF: Tắt (disable) vì dùng JWT stateless
     * - CORS: Bật để cho phép React frontend gọi request
     * - authorizeHttpRequests: Định nghĩa endpoint nào public, endpoint nào cần xác thực
     * - sessionManagement: Stateless (không lưu session)
     * - addFilterBefore: Thêm JwtAuthenticationFilter trước UsernamePasswordAuthenticationFilter
     * 
     * @param http HttpSecurity - Đối tượng cấu hình bảo mật
     * @return SecurityFilterChain - Chuỗi bộ lọc bảo mật đã cấu hình
     * @throws Exception Nếu có lỗi khi cấu hình
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Vô hiệu hóa CSRF cho API không trạng thái
            .csrf(csrf -> csrf.disable())
            
            // Kích hoạt CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Thiết lập quy tắc phân quyền
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (authentication) - Legacy and V1
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                
                // Public endpoints (static files - logos, CVs, uploaded files)
                .requestMatchers("/uploads/**").permitAll()
                
                // Public endpoints (health check, swagger, documentation)
                .requestMatchers(
                    "/api/health/**", 
                    "/actuator/**", 
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                
                // Public endpoints - Tất cả có thể xem việc làm (unauthenticated browsing)
                .requestMatchers(
                    "/api/jobs",                      // GET all jobs (legacy)
                    "/api/jobs/{jobId}",             // GET job by ID (legacy)
                    "/api/jobs/search",              // GET search jobs (legacy)
                    "/api/jobs/filter/salary",       // GET filter by salary (legacy)
                    "/api/jobs/category/{jcid}",     // GET jobs by category (legacy)
                    "/api/jobs/company/{companyId}"  // GET jobs by company (legacy)
                ).permitAll()
                
                // Public endpoints - RESTful API v1 (đọc dữ liệu việc làm, công ty, danh mục)
                .requestMatchers(
                    org.springframework.http.HttpMethod.GET,
                    "/api/v1/jobs/**",               // GET all jobs and job details (public job browsing)
                    "/api/v1/companies/**",          // GET all companies and company details (public company info)
                    "/api/v1/categories/**",         // GET all job categories (public category browsing)
                    "/api/v1/seeking-posts"          // GET seeking posts (public talent browsing - returns masked data for guests)
                ).permitAll()
                
                // Tất cả các yêu cầu khác yêu cầu xác thực
                // Controllers sẽ xử lý phân quyền chi tiết thông qua @PreAuthorize
                .anyRequest().authenticated()
            )
            
            // Quản lý phiên không trạng thái (JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Thêm bộ lọc giới hạn tốc độ ĐẦU TIÊN (trước bất kỳ xác thực nào)
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Thêm bộ lọc JWT trước UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * Bean CorsConfigurationSource - Cấu hình CORS cho frontend và Swagger UI
     * 
     * CORS là gì?
     * - Cross-Origin Resource Sharing: Cơ chế cho phép browser gọi request từ domain khác
     * - Mặc định browser chặn request cross-origin (Same-Origin Policy)
     * - Ví dụ: React (localhost:3000) gọi API đến Spring Boot (localhost:8080) -> Cần CORS
     * 
     * Tại sao cần CORS trong dự án này?
     * 1. React Frontend (localhost:3000) cần gọi API đến Backend (localhost:8080)
     * 2. Vite Dev Server (localhost:5173) cần gọi API khi dev
     * 3. Swagger UI (localhost:8080) cần gọi API để test (cùng origin nhưng vẫn cần CORS config)
     * 
     * CẤU HÌNH CHI TIẾT:
     * 
     * 1. AllowedOriginPatterns: Cho phép origin nào?
     *    - http://localhost:* -> Tất cả port trên localhost (3000, 5173, 8080...)
     *    - http://127.0.0.1:* -> IPv4 localhost
     *    - https://localhost:*, https://127.0.0.1:* -> HTTPS localhost
     * 
     * 2. AllowedMethods: Cho phép HTTP method nào?
     *    - GET, POST, PUT, DELETE, PATCH: CRUD operations
     *    - OPTIONS: QUAN TRỌNG - Preflight request (browser gọi trước request thật)
     *    - HEAD: Kiểm tra metadata
     * 
     * 3. AllowedHeaders: Cho phép header nào?
     *    - Authorization: QUAN TRỌNG - Chứa JWT token (Bearer <token>)
     *    - Content-Type: QUAN TRỌNG - Cho biết kiểu dữ liệu (application/json)
     *    - Accept: Yêu cầu response type
     *    - X-Requested-With, Origin, Access-Control-Request-Method/Headers: CORS headers
     * 
     * 4. AllowCredentials: Cho phép gửi credentials (cookies, authorization headers)?
     *    - true: QUAN TRỌNG - Phải bật để Authorization header hoạt động
     * 
     * 5. ExposedHeaders: Headers nào được frontend đọc?
     *    - Authorization: Cho phép frontend đọc token từ response
     *    - Content-Type, Access-Control-*: Metadata
     * 
     * 6. MaxAge: Cache preflight request bao lâu?
     *    - 3600L (1 giờ): Browser không cần gọi OPTIONS request nhiều lần
     * 
     * LUỒNG CORS REQUEST:
     * 1. Browser gọi OPTIONS preflight request (kiểm tra xem có được phép không)
     * 2. Server trả về CORS headers: Access-Control-Allow-Origin, Access-Control-Allow-Methods...
     * 3. Nếu OK: Browser gọi request thật (GET, POST...)
     * 4. Nếu không OK: Browser chặn request, báo lỗi CORS
     * 
     * @return CorsConfigurationSource - Nguồn cấu hình CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Cho phép nhiều origin: Swagger UI (cùng origin), React, Vite
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",     // Cho phép bất kỳ cổng localhost nào (Swagger UI, React, Vite)
            "http://127.0.0.1:*",     // IPv4 localhost
            "https://localhost:*",    // HTTPS localhost
            "https://127.0.0.1:*"     // HTTPS IPv4 localhost
        ));
        
        // Cho phép tất cả các phương thức HTTP phổ biến (QUAN TRỌNG cho Swagger UI)
        configuration.setAllowedMethods(Arrays.asList(
            "GET", 
            "POST", 
            "PUT", 
            "DELETE", 
            "PATCH", 
            "OPTIONS",  // QUAN TRỌNG: Yêu cầu cho preflight CORS
            "HEAD"
        ));
        
        // Cho phép tất cả các header phổ biến (QUAN TRỌNG cho JWT và Swagger UI)
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",      // QUAN TRỌNG: JWT Bearer token
            "Content-Type",       // QUAN TRỌNG: JSON request body
            "Accept",             // QUAN TRỌNG: Response type negotiation
            "X-Requested-With",   // Xác định AJAX request
            "Origin",             // CORS origin header
            "Access-Control-Request-Method",    // Phương thức preflight
            "Access-Control-Request-Headers"    // Header preflight
        ));
        
        // Cho phép gửi credentials (cookies, authorization headers)
        // QUAN TRỌNG: Phải bật để Authorization header hoạt động
        configuration.setAllowCredentials(true);
        
        // Xác định headers cho frontend/Swagger UI
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        // Thời gian cache preflight request (1 giờ)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    /**
     * Bean AuthenticationManager - Quản lý quá trình xác thực người dùng
     * 
     * AuthenticationManager là gì?
     * - Core component của Spring Security
     * - Chịu trách nhiệm xác thực (authenticate) người dùng
     * - Kiểm tra username/password có đúng không
     * 
     * Sử dụng trong dự án:
     * - AuthService.login(): Gọi authenticationManager.authenticate()
     * - Input: UsernamePasswordAuthenticationToken(username, password)
     * - Process:
     *   1. Tìm User trong database theo username (UserDetailsService)
     *   2. So sánh password: passwordEncoder.matches(rawPassword, encodedPassword)
     *   3. Nếu đúng: Trả về Authentication object (chứa User + Authorities)
     *   4. Nếu sai: Ném BadCredentialsException
     * - Output: Authentication object -> Dùng để tạo JWT token
     * 
     * Ví dụ:
     * <pre>
     * Authentication auth = authenticationManager.authenticate(
     *     new UsernamePasswordAuthenticationToken("user@example.com", "password123")
     * );
     * // Nếu thành công -> Tạo JWT token
     * String token = jwtUtil.generateToken(auth.getName());
     * </pre>
     * 
     * @param config AuthenticationConfiguration - Cấu hình xác thực từ Spring Security
     * @return AuthenticationManager - Quản lý xác thực
     * @throws Exception Nếu có lỗi khi lấy AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
            throws Exception {
        return config.getAuthenticationManager();
    }
}
