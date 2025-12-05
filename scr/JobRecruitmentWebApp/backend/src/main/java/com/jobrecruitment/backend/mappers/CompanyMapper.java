package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.request.CompanyProfileRequest;
import com.jobrecruitment.backend.dtos.response.CompanyResponse;
import com.jobrecruitment.backend.entities.Company;

/**
 * Company Mapper
 * Converts Company Entity <-> DTO
 */
@Component
public class CompanyMapper {
    
    /**
     * Convert Company entity to CompanyResponse DTO
     */
    public CompanyResponse toResponse(Company company) {
        if (company == null) {
            return null;
        }
        
        return new CompanyResponse(
            company.getCompanyId(),
            company.getUser() != null ? company.getUser().getUserId() : null,
            company.getCompanyCode(),
            company.getCompanyName(),
            company.getCompanyDescription(),
            company.getCompanyAddress(),
            company.getCompanyWebsite(),
            company.getCompanyEmail(),
            company.getLogoURL(),
            company.getCompanyStatus(),
            company.getCreatedAt(),
            company.getUpdatedAt()
        );
    }
    
    /**
     * Update Company entity from CompanyProfileRequest
     * Used for profile updates
     */
    public void updateEntityFromRequest(Company company, CompanyProfileRequest request) {
        if (request.getCompanyName() != null) {
            company.setCompanyName(request.getCompanyName());
        }
        if (request.getCompanyDescription() != null) {
            company.setCompanyDescription(request.getCompanyDescription());
        }
        if (request.getCompanyAddress() != null) {
            company.setCompanyAddress(request.getCompanyAddress());
        }
        if (request.getCompanyWebsite() != null) {
            company.setCompanyWebsite(request.getCompanyWebsite());
        }
        if (request.getCompanyEmail() != null) {
            company.setCompanyEmail(request.getCompanyEmail());
        }
        if (request.getLogoURL() != null) {
            company.setLogoURL(request.getLogoURL());
        }
    }
}
