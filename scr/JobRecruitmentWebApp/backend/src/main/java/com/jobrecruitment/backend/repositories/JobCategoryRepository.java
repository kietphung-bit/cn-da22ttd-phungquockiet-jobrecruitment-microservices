package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JobCategoryRepository - Repository interface cho JobCategory entity
 * 
 * Mô tả:
 * - Cung cấp các phương thức truy vấn database cho JobCategory
 * - Master data: Admin quản lý danh mục ngành nghề
 * - Unique constraint: jcName không trùng lặp
 * 
 * Tham khảo: Section 4.6 - JobCategory Module
 */
@Repository
public interface JobCategoryRepository extends JpaRepository<JobCategory, Integer> {
    
    /**
     * Tìm kiếm JobCategory dựa trên tên
     * - Sử dụng trong: Validation (prevent duplicate name)
     * - Query: WHERE jcName = :jcName
     * @param jcName Tên ngành nghề
     * @return Optional<JobCategory> - Có thể empty nếu không tìm thấy
     */
    Optional<JobCategory> findByJcName(String jcName);
    
    /**
     * Kiểm tra tên ngành nghề đã tồn tại chưa
     * - Sử dụng trong: Create/Update validation
     * - Query: SELECT COUNT(*) > 0 WHERE jcName = :jcName
     * @param jcName Tên cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     */
    boolean existsByJcName(String jcName);
}
