package com.jobrecruitment.backend.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RoleResponse - DTO trả về thông tin vai trò
 * 
 * Mô tả:
 * - Trả về thông tin vai trò của người dùng
 * - Chỉ chứa 3 trường cơ bản: roleId, roleCode, roleName
 * - Không chứa danh sách users (tránh infinite loop)
 * 
 * Sử dụng:
 * - API GET /api/v1/roles
 * - Nested trong UserResponse, AuthResponse
 * 
 * Tham khảo: Section 3.2 - Role Table
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    
    /**
     * ID vai trò
     * - Primary key
     */
    private Integer roleId;
    
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
}
