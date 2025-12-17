package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.dtos.request.CompanyProfileRequest;
import com.jobrecruitment.backend.dtos.response.CompanyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * CompanyServiceV1 - Service interface cho quản lý hồ sơ công ty
 * 
 * Chức năng:
 * - Employer: Xem/cập nhật hồ sơ công ty, upload logo
 * - Public/Candidate: Xem danh sách công ty, tìm kiếm theo tên
 * - Admin: Quản lý tất cả công ty
 * 
 * Business Rules:
 * - Mỗi User chỉ có 1 Company profile (One-to-One)
 * - Employer chỉ cập nhật được hồ sơ của mình
 * 
 * Features:
 * - Pagination: Spring Data JPA Pageable
 * - Search: Tìm kiếm theo tên (ignore case, LIKE %keyword%)
 * 
 * @see CompanyServiceV1Impl
 */
public interface CompanyServiceV1 {
    
    /**
     * Lấy danh sách tất cả công ty (Public, phân trang + tìm kiếm)
     * 
     * Sử dụng:
     * - API GET /api/v1/companies
     * - Candidate tìm kiếm công ty để xem các Job
     * 
     * @param pageable Pagination parameters (page, size, sort)
     * @param name Tìm kiếm theo tên công ty (optional, LIKE %keyword%)
     * @return Page<CompanyResponse>
     */
    Page<CompanyResponse> getAllCompanies(Pageable pageable, String name);
    
    /**
     * Lấy hồ sơ Company theo ID (Public)
     * 
     * Sử dụng:
     * - API GET /api/v1/companies/{companyId}
     * - Candidate xem thông tin công ty trước khi ứng tuyển
     * 
     * @param companyId Company ID
     * @return CompanyResponse
     * @throws ResourceNotFoundException nếu không tìm thấy Company
     */
    CompanyResponse getCompanyById(Long companyId);
    
    /**
     * Lấy hồ sơ của Company đang authenticate (Employer only)
     * 
     * Sử dụng:
     * - API GET /api/v1/companies/profile
     * 
     * @param username Username của Employer đang authenticate
     * @return CompanyResponse
     * @throws ResourceNotFoundException nếu không tìm thấy Company
     */
    CompanyResponse getMyProfile(String username);
    
    /**
     * Cập nhật hồ sơ Company (Employer only - own profile)
     * 
     * Sử dụng:
     * - API PUT /api/v1/companies/profile
     * 
     * Business Logic:
     * - Partial update: Chỉ cập nhật field khác null
     * - Validate: Email không trùng
     * 
     * @param request CompanyProfileRequest (companyName, description, address, website, email...)
     * @param username Username của Employer đang authenticate
     * @return CompanyResponse (đã cập nhật)
     * @throws ValidationException nếu email trùng
     */
    CompanyResponse updateProfile(CompanyProfileRequest request, String username);
    
    /**
     * Cập nhật logo công ty (Employer only)
     * 
     * Sử dụng:
     * - API PATCH /api/v1/companies/logo
     * - Upload logo qua file upload service, sau đó gọi API này với logoUrl
     * 
     * @param logoUrl URL logo mới (ví dụ: https://cdn.example.com/logos/company123.png)
     * @param username Username của Employer đang authenticate
     * @return CompanyResponse (đã cập nhật logoURL)
     */
    CompanyResponse updateLogo(String logoUrl, String username);
}
