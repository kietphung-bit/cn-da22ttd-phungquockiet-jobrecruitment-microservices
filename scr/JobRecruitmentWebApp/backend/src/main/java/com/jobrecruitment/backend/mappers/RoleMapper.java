package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.response.RoleResponse;
import com.jobrecruitment.backend.entities.Role;

/**
 * RoleMapper - Mapper cho Role entity
 * 
 * Mô tả:
 * - Chuyển đổi giữa Role entity và RoleResponse DTO
 * - Đơn giản: Chỉ map 3 field (roleId, roleCode, roleName)
 * - Không map users: Tránh infinite loop và over-fetching
 * 
 * Chiến lược mapping:
 * - Entity -> DTO: Map tất cả field trừ Role
 * - Null-safe: Kiểm tra null trước khi map
 */
@Component
public class RoleMapper {
    
    /**
     * Chuyển đổi từ Role entity sang RoleResponse DTO
     * 
     * Chiến lược:
     * - Map các field cơ bản: roleId, roleCode, roleName
     * - Không map danh sách users (tránh circular reference)
     * 
     * @param role Role entity
     * @return RoleResponse DTO (null nếu input null)
     */
    public RoleResponse toResponse(Role role) {
        if (role == null) {
            return null;
        }
        
        return new RoleResponse(
            role.getRoleId(),
            role.getRoleCode(),
            role.getRoleName()
        );
    }
}
