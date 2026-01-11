package com.jobrecruitment.backend.dtos.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserResponse - DTO trả về thông tin User
 * 
 * Mô tả:
 * - Loại bỏ thông tin nhạy cảm (password)
 * - Chỉ trả về thông tin công khai của User
 * - Bao gồm locked status cho admin quản lý
 * - Nested object: RoleResponse (để hiển thị thông tin vai trò)
 * 
 * Sử dụng:
 * - API GET /api/v1/users/{id}
 * - API GET /api/v1/admin/users (Admin user management)
 * - Trả về trong các response khác (CandidateResponse, CompanyResponse)
 * 
 * Tham khảo: Section 3.1 - User Table
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    /**
     * ID người dùng
     * - Primary key tự động tăng
     */
    private Long userId;
    
    /**
     * Mã người dùng
     * - Unique identifier
     * - Format: "AD"/"DN"/"UV" + 8 chữ số
     * - Đồng bộ với CompanyCode/CandidateCode
     */
    private String userCode;
    
    /**
     * Tên đăng nhập (Email)
     * - Email đăng ký của User
     */
    private String username; // Email
    
    /**
     * Thông tin vai trò
     * - Nested object: RoleResponse
     * - Chứa roleCode, roleName
     */
    private RoleResponse role;
    
    /**
     * Trạng thái khóa tài khoản
     * - true: Tài khoản bị khóa (không thể đăng nhập)
     * - false: Tài khoản hoạt động bình thường
     * - Admin cần thông tin này để quản lý user
     */
    private Boolean locked;
    
    /**
     * Thời gian tạo tài khoản
     */
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật gần nhất
     */
    private LocalDateTime updatedAt;
    
    // ========== Company-specific fields (for role DN) ==========
    
    /**
     * ID công ty (chỉ có khi roleCode = DN)
     * - Null nếu không phải Employer
     */
    private Long companyId;
    
    /**
     * Tên công ty (chỉ có khi roleCode = DN)
     */
    private String companyName;
    
    /**
     * Trạng thái công ty (chỉ có khi roleCode = DN)
     * - PENDING: Chờ xét duyệt
     * - ACTIVE: Đang hoạt động
     * - BLOCKED: Bị khóa
     */
    private String companyStatus;
}
