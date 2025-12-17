package com.jobrecruitment.backend.configs;

import com.jobrecruitment.backend.filters.JwtAuthenticationFilter;
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
 * Kiến trúc bảo mật:
 * Request -> CORS Filter -> JWT Filter -> SecurityFilterChain -> Controller
 *         -> Exception Handler (401/403)
 * 
 * Phụ thuộc:
 * - JwtAuthenticationFilter: Filter custom kiểm tra JWT token
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
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
            // Disable CSRF for stateless API
            .csrf(csrf -> csrf.disable())
            
            // Enable CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (authentication) - Legacy and V1
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                
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
                
                // Public endpoints - Anyone can view jobs (unauthenticated browsing)
                .requestMatchers(
                    "/api/jobs",                      // GET all jobs (legacy)
                    "/api/jobs/{jobId}",             // GET job by ID (legacy)
                    "/api/jobs/search",              // GET search jobs (legacy)
                    "/api/jobs/filter/salary",       // GET filter by salary (legacy)
                    "/api/jobs/category/{jcid}",     // GET jobs by category (legacy)
                    "/api/jobs/company/{companyId}"  // GET jobs by company (legacy)
                ).permitAll()
                
                // Public endpoints - RESTful API v1 (read operations)
                .requestMatchers(
                    org.springframework.http.HttpMethod.GET,
                    "/api/v1/jobs",                  // GET all jobs (paginated & filtered)
                    "/api/v1/jobs/{jobId}",          // GET job by ID
                    "/api/v1/applications",          // GET all applications (paginated & filtered)
                    "/api/v1/applications/{applicationId}",  // GET application by ID
                    "/api/v1/companies",             // GET all companies (paginated & filtered)
                    "/api/v1/companies/{companyId}", // GET company by ID
                    "/api/v1/candidates/{candidateId}" // GET candidate by ID
                ).permitAll()
                
                // Public endpoints - Job categories (read-only)
                .requestMatchers(
                    org.springframework.http.HttpMethod.GET,
                    "/api/job-categories",           // GET all categories
                    "/api/job-categories/{jcId}"     // GET category by ID
                ).permitAll()
                
                // Admin-only endpoints (Role: ADM)
                // Fine-grained control via @PreAuthorize in controllers
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADM")
                
                // Employer-only endpoints (Role: DN - Doanh nghiệp)
                // Fine-grained control via @PreAuthorize in controllers
                .requestMatchers("/api/employer/**").hasAuthority("ROLE_DN")
                
                // Candidate-only endpoints (Role: UV - Ứng viên)
                // Fine-grained control via @PreAuthorize in controllers
                .requestMatchers("/api/candidate/**").hasAuthority("ROLE_UV")
                
                // All other requests require authentication (but role is determined by controller)
                .anyRequest().authenticated()
            )
            
            // Stateless session management (JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Add JWT filter before UsernamePasswordAuthenticationFilter
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
        
        // Allow multiple origins: Swagger UI (same origin), React, Vite
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",     // Allows any localhost port (Swagger UI, React, Vite)
            "http://127.0.0.1:*",     // IPv4 localhost
            "https://localhost:*",    // HTTPS localhost
            "https://127.0.0.1:*"     // HTTPS IPv4 localhost
        ));
        
        // Allow all common HTTP methods (CRITICAL for Swagger UI)
        configuration.setAllowedMethods(Arrays.asList(
            "GET", 
            "POST", 
            "PUT", 
            "DELETE", 
            "PATCH", 
            "OPTIONS",  // CRITICAL: Required for CORS preflight requests
            "HEAD"
        ));
        
        // Allow all common headers (CRITICAL for JWT and Swagger UI)
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",      // CRITICAL: JWT Bearer token
            "Content-Type",       // CRITICAL: JSON request body
            "Accept",             // CRITICAL: Response type negotiation
            "X-Requested-With",   // AJAX identifier
            "Origin",             // CORS origin header
            "Access-Control-Request-Method",    // Preflight method
            "Access-Control-Request-Headers"    // Preflight headers
        ));
        
        // Allow credentials (cookies, authorization headers)
        // CRITICAL: Must be true for Authorization header to work
        configuration.setAllowCredentials(true);
        
        // Expose headers to frontend/Swagger UI
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        // Max age for preflight requests (1 hour)
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
