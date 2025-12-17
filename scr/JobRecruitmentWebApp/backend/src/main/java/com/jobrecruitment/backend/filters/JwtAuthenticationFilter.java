package com.jobrecruitment.backend.filters;

import com.jobrecruitment.backend.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * 
 * Much đích:
 * - Kiểm tra JWT token trong header của request
 * - Trích xuất JWT từ header Authorization (schema Bearer)
 * - Xác thực token và thiết lập Spring Security Context
 * 
 * Luồng xử lý:
 * 1. Trích xuất token từ header "Authorization: Bearer {token}"
 * 2. Xác thực chữ ký và thời hạn token
 * 3. Tải UserDetails từ database
 * 4. Tạo đối tượng Authentication
 * 5. Thiết lập authentication trong SecurityContextHolder
 * 
 * Mở rộng OncePerRequestFilter để đảm bảo bộ lọc chạy một lần mỗi yêu cầu.
 * 
 * @see JwtUtils cho logic xác thực token
 * @see CustomUserDetailsService cho việc tải thông tin người dùng
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    /**
     * Logic lọc - chạy trên mỗi yêu cầu HTTP
     * 
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param filterChain FilterChain
     * @throws ServletException nếu lỗi servlet xảy ra
     * @throws IOException nếu lỗi I/O xảy ra
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Trích xuất header Authorization
        final String authHeader = request.getHeader("Authorization");
        
        // Kiểm tra nếu header tồn tại và bắt đầu bằng "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // Trích xuất token JWT (loại bỏ tiền tố "Bearer ")
            final String jwt = authHeader.substring(7);
            
            // Trích xuất tên người dùng từ token
            final String username = jwtUtils.extractUsername(jwt);
            
            // Kiểm tra nếu tên người dùng tồn tại và chưa có authentication trong context
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Tải thông tin người dùng từ database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                // Xác thực token
                if (jwtUtils.validateToken(jwt, userDetails)) {
                    
                    // Tạo đối tượng authentication
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    // Thiết lập thông tin bổ sung (địa chỉ IP, session ID, v.v.)
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // Thiết lập authentication trong SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            
        } catch (Exception e) {
            // Ghi log lỗi và tiếp tục chuỗi bộ lọc
            // Các token không hợp lệ sẽ bị bỏ qua, dẫn đến các yêu cầu không được xác thực
            logger.error("Cannot set user authentication: {}", e);
        }
        
        // Tiếp tục chuỗi bộ lọc
        filterChain.doFilter(request, response);
    }
    
    /**
     * Bỏ qua xử lý JWT cho các endpoint Swagger/OpenAPI
     * Điều này ngăn bộ lọc chạy trên các đường dẫn tài liệu
     * 
     * @param request HttpServletRequest
     * @return true nếu bộ lọc KHÔNG nên xử lý yêu cầu này
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") || 
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/webjars");
    }
}
