package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.response.RoleResponse;
import com.jobrecruitment.backend.entities.Role;

/**
 * Role Mapper
 * Converts Role Entity <-> DTO
 */
@Component
public class RoleMapper {
    
    /**
     * Convert Role entity to RoleResponse DTO
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
