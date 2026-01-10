package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.response.UserResponse;
import com.jobrecruitment.backend.entities.User;

import lombok.RequiredArgsConstructor;

/**
 * UserMapper - Mapper cho User entity
 * 
 * Mô tả:
 * - Chuyển đổi giữa User entity và UserResponse DTO
 * - Bảo mật: Loại bỏ password khi trả về response
 * - Nested mapping: Sử dụng RoleMapper cho Role
 * 
 * Chiến lược mapping:
 * - Entity -> DTO: Chỉ map các field công khai, bỏ password và locked
 * - Null-safe: Kiểm tra null trước khi map
 */
@Component
@RequiredArgsConstructor
public class UserMapper {
    
    /**
     * RoleMapper: Để map nested Role object
     */
    private final RoleMapper roleMapper;
    
    /**
     * Chuyển đổi từ User entity sang UserResponse DTO
     * 
     * Chiến lược:
     * - Loại bỏ password (ẩn thông tin nhạy cảm)
     * - Bao gồm locked status (admin cần để quản lý)
     * - Map nested Role qua RoleMapper
     * 
     * @param user User entity
     * @return UserResponse DTO (null nếu input null)
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserResponse(
            user.getUserId(),
            user.getUserCode(),
            user.getUsername(),
            roleMapper.toResponse(user.getRole()),
            user.getLocked(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
