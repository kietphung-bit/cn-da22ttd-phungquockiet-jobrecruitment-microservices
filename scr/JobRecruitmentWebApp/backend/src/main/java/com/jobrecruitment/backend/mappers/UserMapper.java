package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.response.UserResponse;
import com.jobrecruitment.backend.entities.User;

import lombok.RequiredArgsConstructor;

/**
 * User Mapper
 * Converts User Entity <-> DTO
 * Excludes password for security
 */
@Component
@RequiredArgsConstructor
public class UserMapper {
    
    private final RoleMapper roleMapper;
    
    /**
     * Convert User entity to UserResponse DTO
     * Excludes password for security
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
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
