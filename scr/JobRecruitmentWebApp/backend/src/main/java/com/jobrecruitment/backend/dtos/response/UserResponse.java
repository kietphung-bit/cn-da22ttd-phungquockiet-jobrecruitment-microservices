package com.jobrecruitment.backend.dtos.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserResponse - DTO trả về thông tin User
 * 
 * Mô tả:
 * - Loại bỏ thông tin nhạy cảm (password, locked)
 * - Chỉ trả về thông tin công khai của User
 * - Nested object: RoleResponse (để hiển thị thông tin vai trò)
 * 
 * Sử dụng:
 * - API GET /api/v1/users/{id}
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
     * Thời gian tạo tài khoản
     */
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật gần nhất
     */
    private LocalDateTime updatedAt;
}
