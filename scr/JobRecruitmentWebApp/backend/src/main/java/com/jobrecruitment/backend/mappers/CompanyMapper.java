package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.request.CompanyProfileRequest;
import com.jobrecruitment.backend.dtos.response.CompanyResponse;
import com.jobrecruitment.backend.entities.Company;

/**
 * CompanyMapper - Mapper cho Company entity
 * 
 * Mô tả:
 * - Chuyển đổi giữa Company entity và Company DTO
 * - 2 chiều: Entity -> Response DTO, Request DTO -> Entity (update)
 * - Flatten: Chỉ lấy userId từ User (không map toàn bộ User object)
 * 
 * Chiến lược mapping:
 * - toResponse: Map tất cả field từ Company, lấy userId từ nested User
 * - updateEntityFromRequest: Chỉ update các field khác null (partial update)
 */
@Component
public class CompanyMapper {
    
    /**
     * Chuyển đổi từ Company entity sang CompanyResponse DTO
     * 
     * Chiến lược:
     * - Map tất cả field của Company (bao gồm status, logo, website)
     * - Flatten nested User: Chỉ lấy userId (không map toàn bộ UserResponse)
     * - Null-safe: Kiểm tra user != null trước khi lấy userId
     * 
     * @param company Company entity
     * @return CompanyResponse DTO (null nếu input null)
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
     * Cập nhật Company entity từ CompanyProfileRequest DTO
     * 
     * Sử dụng:
     * - API PUT /api/v1/companies/profile (update hồ sơ công ty)
     * 
     * Chiến lược:
     * - Partial update: Chỉ cập nhật các field khác null trong request
     * - Preserve: Field nào null trong request thì giữ nguyên giá trị cũ
     * - Không update: companyId, user, companyCode, companyStatus, createdAt (immutable)
     * 
     * @param company Entity cần cập nhật (modified in-place)
     * @param request DTO chứa dữ liệu mới
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
