package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CompanyRepository - Repository interface cho Company entity
 * 
 * Mô tả:
 * - Cung cấp các phương thức truy vấn database cho Company
 * - One-to-One với User: Tìm theo userId
 * - Advanced: Tìm kiếm theo tên (pagination), đếm theo status
 * 
 * Tham khảo: Section 4.2 - Employer Module
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    /**
     * Tìm kiếm Company dựa trên companyCode
     * - Sử dụng trong: Profile retrieval, Job management
     * - Query: WHERE companyCode = :companyCode
     * @param companyCode Mã công ty (DN + 8 số)
     * @return Optional<Company> - Có thể empty nếu không tìm thấy
     */
    Optional<Company> findByCompanyCode(String companyCode);
    
    /**
     * Tìm kiếm Company dựa trên userId
     * - Sử dụng trong: Profile retrieval sau khi authenticate
     * - Query: JOIN với User WHERE user.userId = :userId
     * - One-to-One relationship
     * @param userId ID của User liên kết
     * @return Optional<Company> - Có thể empty nếu không tìm thấy
     */
    Optional<Company> findByUserUserId(Long userId);
    
    /**
     * Kiểm tra companyCode đã tồn tại chưa
     * - Sử dụng trong: Code generation validation
     * - Query: SELECT COUNT(*) > 0 WHERE companyCode = :companyCode
     * @param companyCode Mã cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     */
    boolean existsByCompanyCode(String companyCode);
    
    /**
     * Kiểm tra email đã tồn tại chưa
     * - Sử dụng trong: Registration/Profile update validation
     * - Query: SELECT COUNT(*) > 0 WHERE companyEmail = :companyEmail
     * @param companyEmail Email cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     */
    boolean existsByCompanyEmail(String companyEmail);
    
    /**
     * Đếm số lượng Company theo status
     * - Sử dụng trong: Admin Dashboard statistics
     * - Query: SELECT COUNT(*) WHERE companyStatus = :companyStatus
     * @param companyStatus Trạng thái (PENDING/ACTIVE/BLOCKED)
     * @return Số lượng công ty có trạng thái đó
     */
    long countByCompanyStatus(com.jobrecruitment.backend.enums.CompanyStatus companyStatus);
    
    /**
     * Tìm kiếm Company theo tên (ignore case, LIKE %keyword%)
     * - Sử dụng trong: Admin search companies
     * - Query: WHERE LOWER(companyName) LIKE LOWER('%' || :name || '%')
     * - Pagination: Pageable (page, size, sort)
     * @param name Từ khóa tìm kiếm
     * @param pageable Tham số phân trang
     * @return Page<Company> - Kết quả phân trang
     */
    Page<Company> findByCompanyNameContainingIgnoreCase(String name, Pageable pageable);
}
