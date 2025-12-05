package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.dtos.request.CompanyProfileRequest;
import com.jobrecruitment.backend.dtos.response.CompanyResponse;

public interface CompanyService {
    
    /**
     * Get company profile by ID (Public access)
     */
    CompanyResponse getCompanyById(Long companyId);
    
    /**
     * Get authenticated company's profile
     */
    CompanyResponse getMyProfile(String username);
    
    /**
     * Update company profile (Employer only - own profile)
     */
    CompanyResponse updateProfile(CompanyProfileRequest request, String username);
}
