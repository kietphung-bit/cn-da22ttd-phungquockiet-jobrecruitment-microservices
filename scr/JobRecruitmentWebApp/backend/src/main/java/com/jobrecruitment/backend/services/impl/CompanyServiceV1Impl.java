package com.jobrecruitment.backend.services.impl;

import com.jobrecruitment.backend.dtos.request.CompanyProfileRequest;
import com.jobrecruitment.backend.dtos.response.CompanyResponse;
import com.jobrecruitment.backend.entities.Company;
import com.jobrecruitment.backend.entities.User;
import com.jobrecruitment.backend.exceptions.ResourceNotFoundException;
import com.jobrecruitment.backend.mappers.CompanyMapper;
import com.jobrecruitment.backend.repositories.CompanyRepository;
import com.jobrecruitment.backend.repositories.UserRepository;
import com.jobrecruitment.backend.services.CompanyServiceV1;
import com.jobrecruitment.backend.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * CompanyServiceV1Impl - Service triển khai logic nghiệp vụ cho Quản lý Doanh nghiệp (Version 1).
 * 
 * Chức năng chính:
 * - Danh sách doanh nghiệp có phân trang và tìm kiếm theo tên (Public access)
 * - Xem chi tiết doanh nghiệp theo ID (Public access)
 * - Employer xem profile của doanh nghiệp mình (Employer only)
 * - Employer cập nhật thông tin doanh nghiệp (Employer only)
 * - Employer cập nhật logo (Employer only)
 * 
 * Tính năng nổi bật:
 * - Phân trang (Pagination): Hỗ trợ Pageable với sort, page, size
 * - Tìm kiếm: Theo tên doanh nghiệp (case-insensitive, partial match)
 * - Partial Update: Chỉ cập nhật các field được cung cấp
 * 
 * Dependencies:
 * - CompanyRepository: Truy vấn database với phân trang và tìm kiếm
 * - UserRepository: Xác định employer từ username (JWT)
 * - CompanyMapper: Chuyển đổi Entity ↔ DTO, partial update
 * 
 * Transaction Management:
 * - @Transactional: Rollback nếu có lỗi
 * - @Transactional(readOnly = true): Query optimization cho read operations
 * 
 * @author JobRecruitment Development Team
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompanyServiceV1Impl implements CompanyServiceV1 {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyMapper companyMapper;
    private final FileStorageService fileStorageService;

    /**
     * Lấy danh sách tất cả doanh nghiệp với phân trang và tìm kiếm.
     * 
     * Endpoint công khai - không yêu cầu authentication, cho phép ứng viên
     * tìm kiếm và xem danh sách các doanh nghiệp.
     * 
     * Quy trình xử lý:
     * 1. Kiểm tra tham số tìm kiếm 'name':
     *    - Nếu có: Tìm kiếm theo tên (case-insensitive, partial match)
     *    - Nếu không: Lấy tất cả doanh nghiệp
     * 2. Thực thi query với Pageable (hỗ trợ sort, page, size)
     * 3. Chuyển đổi Page<Company> sang Page<CompanyResponse>
     * 4. Trả về kết quả với thông tin phân trang
     * 
     * @param pageable Đối tượng phân trang (page, size, sort)
     * @param name Từ khóa tìm kiếm tên doanh nghiệp (optional, partial match)
     * @return Page<CompanyResponse> chứa danh sách doanh nghiệp và thông tin phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAllCompanies(Pageable pageable, String name) {
        log.info("Fetching companies with pagination - name filter: {}, page: {}, size: {}", 
                name, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Company> companyPage;
        
        if (name != null && !name.trim().isEmpty()) {
            // Tìm kiếm theo tên (không phân biệt hoa thường, khớp một phần)
            companyPage = companyRepository.findByCompanyNameContainingIgnoreCase(name, pageable);
            log.info("Found {} companies matching '{}'", companyPage.getTotalElements(), name);
        } else {
            // Lấy tất cả doanh nghiệp
            companyPage = companyRepository.findAll(pageable);
            log.info("Found {} total companies", companyPage.getTotalElements());
        }
        
        // Chuyển đổi entities sang DTOs
        return companyPage.map(companyMapper::toResponse);
    }

    /**
     * Lấy thông tin chi tiết doanh nghiệp theo ID.
     * 
     * Endpoint công khai - cho phép tất cả user xem thông tin doanh nghiệp.
     * 
     * Quy trình xử lý:
     * 1. Tìm kiếm Company entity theo companyId
     * 2. Nếu không tìm thấy: Throw ResourceNotFoundException
     * 3. Chuyển đổi Entity sang DTO (CompanyResponse)
     * 4. Trả về thông tin doanh nghiệp
     * 
     * @param companyId ID của doanh nghiệp cần xem
     * @return CompanyResponse chứa thông tin chi tiết doanh nghiệp
     * @throws ResourceNotFoundException Nếu không tìm thấy doanh nghiệp với ID được cung cấp
     */
    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Long companyId) {
        log.info("Fetching company with ID: {}", companyId);
        
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));
        
        return companyMapper.toResponse(company);
    }

    /**
     * Lấy thông tin profile doanh nghiệp của employer đang đăng nhập.
     * 
     * Employer-only endpoint - chỉ employer mới có thể xem profile doanh nghiệp của mình.
     * 
     * Quy trình xử lý:
     * 1. Tìm kiếm User entity theo username (từ JWT token)
     * 2. Nếu không tìm thấy User: Throw ResourceNotFoundException
     * 3. Tìm Company entity liên kết với User (One-to-One relationship)
     * 4. Nếu không tìm thấy Company: Throw ResourceNotFoundException
     * 5. Chuyển đổi Entity sang DTO và trả về
     * 
     * @param username Username của employer đang đăng nhập (lấy từ JWT)
     * @return CompanyResponse chứa thông tin profile doanh nghiệp
     * @throws ResourceNotFoundException Nếu không tìm thấy User hoặc Company profile
     */
    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getMyProfile(String username) {
        log.info("Fetching company profile for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Company company = companyRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found for user: " + username));
        
        return companyMapper.toResponse(company);
    }

    /**
     * Cập nhật thông tin profile doanh nghiệp.
     * 
     * Employer-only endpoint - chỉ employer mới có thể cập nhật profile doanh nghiệp của mình.
     * 
     * Quy trình xử lý:
     * 1. Tìm User entity theo username (từ JWT)
     * 2. Tìm Company entity liên kết với User
     * 3. Sử dụng CompanyMapper để cập nhật các field từ request:
     *    - CompanyName: Tên doanh nghiệp
     *    - CompanyDescription: Mô tả doanh nghiệp
     *    - CompanyAddress: Địa chỉ
     *    - CompanyWebsite: Website URL
     *    - CompanyEmail: Email liên hệ
     *    - CompanyStatus: Trạng thái (PENDING/ACTIVE/BLOCKED)
     * 4. Lưu Company entity vào database
     * 5. Chuyển đổi sang DTO và trả về
     * 
     * Lưu ý: Sử dụng CompanyMapper.updateEntityFromRequest để partial update
     * 
     * @param request CompanyProfileRequest chứa thông tin cần cập nhật
     * @param username Username của employer đang đăng nhập
     * @return CompanyResponse với thông tin đã cập nhật
     * @throws ResourceNotFoundException Nếu không tìm thấy User hoặc Company profile
     * @throws ValidationException Nếu dữ liệu không hợp lệ
     */
    @Override
    public CompanyResponse updateProfile(CompanyProfileRequest request, String username) {
        log.info("Updating company profile for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Company company = companyRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found for user: " + username));
        
        // Cập nhật các trường của doanh nghiệp sử dụng mapper
        companyMapper.updateEntityFromRequest(company, request);
        
        Company updatedCompany = companyRepository.save(company);
        log.info("Company profile updated successfully - CompanyID: {}", updatedCompany.getCompanyId());
        
        return companyMapper.toResponse(updatedCompany);
    }

    /**
     * Upload logo công ty (Employer-only)
     * 
     * Employer-only endpoint - chỉ employer mới có thể upload logo doanh nghiệp của mình.
     * 
     * Quy trình xử lý:
     * 1. Tìm User entity theo username (từ JWT)
     * 2. Tìm Company entity liên kết với User
     * 3. Upload file vào uploads/logos/ với FileStorageService
     *    - Validation: extension (jpg, jpeg, png, gif), size (max 10MB)
     *    - Uniqueness: UUID prefix + original filename
     * 4. Xóa logo cũ nếu tồn tại (cleanup old file)
     * 5. Cập nhật LogoURL field với URL mới trong database
     * 6. Lưu Company entity vào database
     * 7. Chuyển đổi sang DTO và trả về
     * 
     * Lưu ý: Đây là endpoint chuyên dụng cho việc upload logo (PATCH method)
     * 
     * @param file Logo image file (MultipartFile)
     * @param username Username của employer đang đăng nhập
     * @return CompanyResponse với thông tin đã cập nhật logo
     * @throws ResourceNotFoundException Nếu không tìm thấy User hoặc Company profile
     * @throws FileStorageException Nếu file không hợp lệ hoặc lỗi upload
     */
    @Override
    public CompanyResponse uploadLogo(MultipartFile file, String username) {
        log.info("Uploading company logo for user: {} - File: {}, Size: {} bytes", 
                username, file.getOriginalFilename(), file.getSize());
        
        // 1. Tìm User theo username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        // 2. Tìm profile Company theo User
        Company company = companyRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found for user: " + username));
        
        // 3. Upload file vào thư mục logos
        String relativePath = fileStorageService.storeFile(file, "logos");
        // Thêm tiền tố /uploads/ để có thể truy cập qua static resource mapping
        String newLogoUrl = "/uploads/" + relativePath;
        log.info("Logo file uploaded successfully: {}", newLogoUrl);
        
        // 4. Xóa logo cũ nếu tồn tại
        String oldLogoUrl = company.getLogoURL();
        if (oldLogoUrl != null && !oldLogoUrl.isEmpty()) {
            try {
                fileStorageService.deleteFile(oldLogoUrl);
                log.info("Old logo deleted: {}", oldLogoUrl);
            } catch (Exception e) {
                log.warn("Failed to delete old logo: {} - Error: {}", oldLogoUrl, e.getMessage());
                // Tiếp tục dù lỗi - logo mới đã được upload
            }
        }
        
        // 5. Cập nhật URL logo trong cơ sở dữ liệu
        company.setLogoURL(newLogoUrl);
        
        Company updatedCompany = companyRepository.save(company);
        log.info("Company logo updated successfully - CompanyID: {}, LogoURL: {}", 
                updatedCompany.getCompanyId(), newLogoUrl);
        
        return companyMapper.toResponse(updatedCompany);
    }
}
