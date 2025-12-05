package com.jobrecruitment.backend.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    
    private Integer roleId;
    private String roleCode;
    private String roleName;
}
