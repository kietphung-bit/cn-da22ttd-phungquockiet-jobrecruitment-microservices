package com.jobrecruitment.backend.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AuthResponse - DTO trả về thông tin xác thực
 * 
 * Mô tả:
 * - Trả về sau khi đăng nhập/đăng ký thành công
 * - Chứa JWT token để xác thực các request tiếp theo
 * - Chứa thông tin cơ bản của User (userCode, username, role)
 * 
 * Sử dụng:
 * - API POST /api/v1/auth/login
 * - API POST /api/v1/auth/candidate/register
 * - API POST /api/v1/auth/company/register
 * 
 * Tham khảo: Section 4.1 - Authentication & Authorization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    /**
     * JWT token
     * - Dùng để xác thực các API request tiếp theo
     * - Thêm vào header: Authorization: Bearer <token>
     * - Thời gian sống: 24h (Section 4.1)
     */
    private String token; // JWT token
    
    /**
     * Loại token
     * - Mặc định: "Bearer"
     * - Theo chuẩn OAuth 2.0
     */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * Mã người dùng
     * - Format: "AD"/"DN"/"UV" + 8 chữ số
     * - Ví dụ: "UV12345678", "DN87654321"
     */
    private String userCode;
    
    /**
     * Tên đăng nhập (Email)
     * - Email đăng ký của User
     */
    private String username;
    
    /**
     * Mã vai trò
     * - "ADM": Quản trị viên
     * - "DN": Nhà tuyển dụng (Employer)
     * - "UV": Ứng viên (Candidate)
     */
    private String roleCode;
    
    /**
     * Tên vai trò (Tiếng Việt)
     * - Ví dụ: "Quản trị viên", "Nhà tuyển dụng", "Ứng viên"
     */
    private String roleName;
    
    /**
     * Tên ứng viên (nếu role = UV)
     * - Null nếu không phải ứng viên
     */
    private String candidateName;
    
    /**
     * Mã ứng viên (nếu role = UV)
     * - Null nếu không phải ứng viên
     */
    private String candidateCode;
    
    /**
     * Tên công ty (nếu role = DN)
     * - Null nếu không phải nhà tuyển dụng
     */
    private String companyName;
    
    /**
     * Mã công ty (nếu role = DN)
     * - Null nếu không phải nhà tuyển dụng
     */
    private String companyCode;
    
    /**
     * Thông báo tùy chỉnh
     * - Ví dụ: "Đăng nhập thành công", "Đăng ký thành công"
     */
    private String message; // Custom message for registration/login response
}
