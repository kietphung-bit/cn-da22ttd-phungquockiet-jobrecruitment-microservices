package com.jobrecruitment.backend.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * JWT Utility Component - Tiện ích xử lý JWT Token
 * 
 * Chức năng chính:
 * - Tạo JWT token khi người dùng đăng nhập thành công
 * - Xác thực và giải mã JWT token từ HTTP request
 * - Trích xuất thông tin user (username, role, userCode) từ token
 * - Kiểm tra thời hạn hiệu lực của token
 * 
 * Công nghệ sử dụng:
 * - JJWT 0.12.x+ (Modern JWT Library)
 * - HMAC-SHA256 signing algorithm (HS256)
 * - BASE64-encoded secret key (256-bit minimum)
 * - Builder pattern cho code dễ đọc và bảo trì
 * 
 * Cấu hình trong application.properties:
 * - app.jwt.secret: Secret key đã mã hóa BASE64 (256-bit)
 * - app.jwt.expiration: Thời gian sống của token (ms, mặc định 24h = 86400000)
 * 
 * Tích hợp với Spring Security:
 * - JwtAuthenticationFilter sử dụng class này để xác thực request
 * - SecurityConfig cấu hình JWT filter trong filter chain
 * 
 * @see JwtAuthenticationFilter
 * @see SecurityConfig
 */
@Component
public class JwtUtils {
    
    @Value("${app.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;
    
    @Value("${app.jwt.expiration:86400000}") // Default: 24 hours in milliseconds
    private Long jwtExpiration;
    
    /**
     * Lấy signing key để ký và xác thực JWT token.
     * 
     * Quy trình:
     * 1. Giải mã secret key từ BASE64 string (lưu trong application.properties)
     * 2. Tạo SecretKey object cho thuật toán HMAC-SHA256
     * 
     * @return SecretKey object dùng để ký và xác thực token
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Trích xuất username (email) từ JWT token.
     * Username được lưu trong claim "subject" của token.
     * 
     * @param token JWT token string (bỏ prefix "Bearer " nếu có)
     * @return Username (email) của người dùng
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Trích xuất thời gian hết hạn từ JWT token.
     * 
     * @param token JWT token string
     * @return Thời điểm token hết hiệu lực (Date object)
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Trích xuất thời điểm phát hành token (issuedAt).
     * Sử dụng để kiểm tra token có trước thời điểm logout all không.
     * 
     * @param token JWT token string
     * @return Thời điểm token được phát hành (Date object)
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }
    
    /**
     * Trích xuất một claim cụ thể từ JWT token.
     * Hàm generic cho phép trích xuất bất kỳ claim nào (username, role, userCode, exp, etc.)
     * 
     * @param <T> Kiểu dữ liệu của claim cần lấy
     * @param token JWT token string
     * @param claimsResolver Function để xử lý và trích xuất claim từ Claims object
     * @return Giá trị claim đã được trích xuất
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Trích xuất toàn bộ claims từ JWT token.
     * 
     * Quy trình xử lý (JJWT 0.12.x+ API):
     * 1. Tạo JWT parser với signing key
     * 2. Parse và verify token signature
     * 3. Trích xuất payload (claims)
     * 
     * @param token JWT token string
     * @return Claims object chứa toàn bộ thông tin trong token
     * @throws io.jsonwebtoken.JwtException Nếu token không hợp lệ, đã hết hạn, hoặc signature sai
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Kiểm tra token đã hết hạn chưa.
     * So sánh thời gian expiration trong token với thời gian hiện tại.
     * 
     * @param token JWT token string
     * @return true nếu token đã hết hạn, false nếu còn hiệu lực
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Xác thực JWT token với thông tin user từ database.
     * 
     * Các bước kiểm tra:
     * 1. Trích xuất username từ token
     * 2. So sánh username trong token với username từ UserDetails
     * 3. Kiểm tra token chưa hết hạn
     * 
     * Method này được gọi bởi JwtAuthenticationFilter để xác thực mỗi request.
     * 
     * @param token JWT token từ Authorization header
     * @param userDetails Thông tin user đã load từ database (qua UserDetailsService)
     * @return true nếu token hợp lệ, false nếu không hợp lệ
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    /**
     * Tạo JWT token đơn giản chỉ với username.
     * Không bao gồm custom claims (role, userCode).
     * 
     * @param username Username (email) để lưu vào token subject
     * @return JWT token string đã được ký
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }
    
    /**
     * Tạo JWT token với custom claims (role, userCode, userId, etc.).
     * 
     * Sử dụng khi muốn lưu thêm thông tin trong token để tránh query database.
     * Ví dụ: extraClaims.put("role", "DN"); extraClaims.put("userCode", "DN12345678");
     * 
     * @param extraClaims Map chứa các claim bổ sung (role, userCode, userId, etc.)
     * @param username Username (email) để lưu vào token subject
     * @return JWT token string đã được ký
     */
    public String generateToken(Map<String, Object> extraClaims, String username) {
        return createToken(extraClaims, username);
    }
    
    /**
     * Tạo JWT token với JJWT Builder API (Modern approach).
     * 
     * Token structure:
     * - Header: Algorithm (HS256), Type (JWT)
     * - Payload: Claims (subject, issuedAt, expiration, custom claims)
     * - Signature: HMAC-SHA256(header + payload, secretKey)
     * 
     * @param claims Map chứa các claims cần thêm vào token
     * @param username Username (email) làm subject của token
     * @return JWT token string (compact format: header.payload.signature)
     */
    private String createToken(Map<String, Object> claims, String username) {
        return Jwts
                .builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }
    
    /**
     * Trích xuất role code từ JWT token.
     * 
     * Role code có thể là:
     * - "ADM": Administrator (Quản trị viên)
     * - "DN": Doanh nghiệp (Employer)
     * - "UV": Ứng viên (Candidate)
     * 
     * @param token JWT token string
     * @return Role code hoặc null nếu không có claim "role" trong token
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    
    /**
     * Trích xuất user code từ JWT token.
     * 
     * UserCode được đồng bộ với CompanyCode hoặc CandidateCode (Section 4.5.C):
     * - Admin: AD00000001
     * - Company: DN + 8 số (ví dụ: DN12345678)
     * - Candidate: UV + 8 số (ví dụ: UV87654321)
     * 
     * @param token JWT token string
     * @return UserCode hoặc null nếu không có claim "userCode" trong token
     */
    public String extractUserCode(String token) {
        return extractClaim(token, claims -> claims.get("userCode", String.class));
    }
    
    /**
     * Trích xuất user ID từ JWT token.
     * 
     * @param token JWT token string
     * @return User ID (Primary Key) hoặc null nếu không có claim "userId" trong token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }
}
