package com.jobrecruitment.backend.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication Response DTO
 * Returned after successful login/register
 * Section 4.1 - Authentication & Authorization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token; // JWT token
    @Builder.Default
    private String tokenType = "Bearer";
    private String userCode;
    private String username;
    private String roleCode;
    private String roleName;
    private String message; // Custom message for registration/login response
}
