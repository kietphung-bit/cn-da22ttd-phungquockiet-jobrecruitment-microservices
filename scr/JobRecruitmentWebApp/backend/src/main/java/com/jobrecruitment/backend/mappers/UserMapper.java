package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.response.UserResponse;
import com.jobrecruitment.backend.entities.User;
import com.jobrecruitment.backend.repositories.CompanyRepository;

import lombok.RequiredArgsConstructor;

/**
 * UserMapper - Mapper cho User entity
 * 
 * Mô tả:
 * - Chuyển đổi giữa User entity và UserResponse DTO
 * - Bảo mật: Loại bỏ password khi trả về response
 * - Nested mapping: Sử dụng RoleMapper cho Role
 * - Company info: Lấy thông tin Company nếu user là Employer (DN)
 * 
 * Chiến lược mapping:
 * - Entity -> DTO: Chỉ map các field công khai, bỏ password và locked
 * - Null-safe: Kiểm tra null trước khi map
 * - Company data: Fetch từ CompanyRepository khi roleCode = DN
 */
@Component
@RequiredArgsConstructor
public class UserMapper {
    
    /**
     * RoleMapper: Để map nested Role object
     */
    private final RoleMapper roleMapper;
    
    /**
     * CompanyRepository: Để lấy thông tin Company cho Employer
     */
    private final CompanyRepository companyRepository;
    
    /**
     * Chuyển đổi từ User entity sang UserResponse DTO
     * 
     * Chiến lược:
     * - Loại bỏ password (ẩn thông tin nhạy cảm)
     * - Bao gồm locked status (admin cần để quản lý)
     * - Map nested Role qua RoleMapper
     * - Lấy Company info nếu user là Employer (roleCode = DN)
     * 
     * @param user User entity
     * @return UserResponse DTO (null nếu input null)
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        
        UserResponse response = new UserResponse(
            user.getUserId(),
            user.getUserCode(),
            user.getUsername(),
            roleMapper.toResponse(user.getRole()),
            user.getLocked(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            null, // companyId - will be set below if DN
            null, // companyName - will be set below if DN
            null  // companyStatus - will be set below if DN
        );
        
        // If user is Employer (DN), fetch company information
        if ("DN".equals(user.getRole().getRoleCode())) {
            companyRepository.findByUserUserId(user.getUserId())
                .ifPresent(company -> {
                    response.setCompanyId(company.getCompanyId());
                    response.setCompanyName(company.getCompanyName());
                    response.setCompanyStatus(company.getCompanyStatus().name());
                });
        }
        
        return response;
    }
}
