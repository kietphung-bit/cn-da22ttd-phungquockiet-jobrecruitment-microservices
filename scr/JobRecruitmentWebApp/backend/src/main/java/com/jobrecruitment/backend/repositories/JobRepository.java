package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.Job;
import com.jobrecruitment.backend.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JobRepository - Repository interface cho Job entity
 * 
 * Mô tả:
 * - Cung cấp các phương thức truy vấn database cho Job
 * - JpaSpecificationExecutor: Hỗ trợ dynamic queries (filter, sort)
 * - Advanced: Tìm kiếm theo nhiều tiêu chí (keyword, location, salary, category)
 * 
 * Tham khảo: Section 4.4 - Job Module
 */
@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    
    /**
     * Tìm kiếm Job dựa trên jobCode
     * - Query: WHERE jobCode = :jobCode
     * @param jobCode Mã tin tuyển dụng (VL + 8 số)
     * @return Optional<Job> - Có thể empty nếu không tìm thấy
     */
    Optional<Job> findByJobCode(String jobCode);
    
    /**
     * Kiểm tra jobCode đã tồn tại chưa
     * - Query: SELECT COUNT(*) > 0 WHERE jobCode = :jobCode
     * @param jobCode Mã cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     */
    boolean existsByJobCode(String jobCode);
    
    /**
     * Tìm kiếm tất cả Job của một Company
     * - Sử dụng trong: Employer xem tin của mình
     * - Query: JOIN với Company WHERE company.companyId = :companyId
     * @param companyId ID công ty
     * @return List<Job> - Danh sách tin tuyển dụng
     */
    List<Job> findByCompanyCompanyId(Long companyId);
    
    /**
     * Tìm kiếm Job của Company theo status
     * - Sử dụng trong: Employer filter theo trạng thái
     * - Query: WHERE company.companyId = :companyId AND jobStatus = :jobStatus
     * @param companyId ID công ty
     * @param jobStatus Trạng thái (PENDING/WAIT/ACTIVE/CLOSED/HIDDEN)
     * @return List<Job> - Danh sách tin tuyển dụng
     */
    List<Job> findByCompanyCompanyIdAndJobStatus(Long companyId, JobStatus jobStatus);
    
    /**
     * Tìm kiếm Job theo status
     * - Sử dụng trong: Candidate xem tin ACTIVE, Admin quản lý
     * - Query: WHERE jobStatus = :jobStatus
     * @param jobStatus Trạng thái
     * @return List<Job> - Danh sách tin tuyển dụng
     */
    List<Job> findByJobStatus(JobStatus jobStatus);
    
    /**
     * Tìm kiếm Job theo ngành nghề
     * - Sử dụng trong: Filter theo JobCategory
     * - Query: WHERE jobCategory.jcId = :jcId
     * @param jcId ID ngành nghề
     * @return List<Job> - Danh sách tin tuyển dụng
     */
    List<Job> findByJobCategoryJcId(Integer jcId);
    
    /**
     * Tìm kiếm Job theo keyword (title hoặc location)
     * - Sử dụng trong: Basic search
     * - JPQL: LIKE %keyword% trong jobTitle hoặc jobLocation
     * @param keyword Từ khóa tìm kiếm
     * @return List<Job> - Danh sách tin khớp
     */
    @Query("SELECT j FROM Job j WHERE " +
           "LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.jobLocation) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Job> searchJobs(@Param("keyword") String keyword);
    
    /**
     * Tìm kiếm Job theo khoảng lương
     * - Sử dụng trong: Filter theo mức lương
     * - JPQL: BETWEEN minSalary AND maxSalary
     * @param minSalary Lương tối thiểu
     * @param maxSalary Lương tối đa
     * @return List<Job> - Danh sách tin trong khoảng lương
     */
    @Query("SELECT j FROM Job j WHERE j.jobSalary BETWEEN :minSalary AND :maxSalary")
    List<Job> findBySalaryRange(@Param("minSalary") Double minSalary, @Param("maxSalary") Double maxSalary);
    
    /**
     * Đếm số lượng Job theo status
     * - Sử dụng trong: Admin Dashboard statistics
     * - Query: SELECT COUNT(*) WHERE jobStatus = :jobStatus
     * @param jobStatus Trạng thái
     * @return Số lượng tin tuyển dụng
     */
    long countByJobStatus(JobStatus jobStatus);
    
    /**
     * Tìm kiếm nâng cao với nhiều filter
     * - Sử dụng trong: Candidate tìm việc với nhiều tiêu chí
     * - JPQL: LEFT JOIN FETCH company và jobCategory (để tránh N+1 query)
     * - Filter: keyword (title/location), location, category, salary range, status
     * - Tất cả filter đều optional (có thể null)
     * @param keyword Từ khóa tìm trong tiêu đề/địa điểm
     * @param location Địa điểm cụ thể
     * @param jcId ID ngành nghề
     * @param minSalary Lương tối thiểu
     * @param maxSalary Lương tối đa
     * @param status Trạng thái tin
     * @return List<Job> - Danh sách tin khớp với tất cả filter
     */
    @Query("SELECT DISTINCT j FROM Job j LEFT JOIN FETCH j.company LEFT JOIN FETCH j.jobCategory WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.jobLocation) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:location IS NULL OR :location = '' OR LOWER(j.jobLocation) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:jcId IS NULL OR j.jobCategory.jcId = :jcId) AND " +
           "(:minSalary IS NULL OR j.jobSalary >= :minSalary) AND " +
           "(:maxSalary IS NULL OR j.jobSalary <= :maxSalary) AND " +
           "(:status IS NULL OR j.jobStatus = :status)")
    List<Job> advancedSearch(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("jcId") Integer jcId,
            @Param("minSalary") Double minSalary,
            @Param("maxSalary") Double maxSalary,
            @Param("status") JobStatus status
    );
}
