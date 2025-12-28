package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.Application;
import com.jobrecruitment.backend.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ApplicationRepository - Repository interface cho Application entity
 * 
 * Mô tả:
 * - Cung cấp các phương thức truy vấn database cho Application
 * - JpaSpecificationExecutor: Hỗ trợ dynamic queries
 * - Complex queries: Tìm theo candidate (qua CV), check duplicate, đếm theo thời gian
 * 
 * Tham khảo: Section 4.8 - Application Module
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {
    
    /**
     * Tìm kiếm Application dựa trên applicationCode
     * - Query: WHERE applicationCode = :applicationCode
     * @param applicationCode Mã đơn (DX + 8 số)
     * @return Optional<Application> - Có thể empty nếu không tìm thấy
     */
    Optional<Application> findByApplicationCode(String applicationCode);
    
    /**
     * Kiểm tra applicationCode đã tồn tại chưa
     * - Query: SELECT COUNT(*) > 0 WHERE applicationCode = :applicationCode
     * @param applicationCode Mã cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     */
    boolean existsByApplicationCode(String applicationCode);
    
    /**
     * Tìm kiếm tất cả Application của một Job
     * - Sử dụng trong: Employer xem đơn ứng tuyển tin của mình
     * - Query: WHERE job.jobId = :jobId
     * @param jobId ID tin tuyển dụng
     * @return List<Application> - Danh sách đơn ứng tuyển
     */
    List<Application> findByJobJobId(Long jobId);
    
    /**
     * Tìm kiếm Application của Job theo status
     * - Sử dụng trong: Employer filter đơn theo trạng thái
     * - Query: WHERE job.jobId = :jobId AND applicationStatus = :status
     * @param jobId ID tin tuyển dụng
     * @param status Trạng thái (PENDING/APPROVED/REJECTED)
     * @return List<Application> - Danh sách đơn theo trạng thái
     */
    List<Application> findByJobJobIdAndApplicationStatus(Long jobId, ApplicationStatus status);
    
    /**
     * Tìm kiếm tất cả Application của một Candidate
     * - Sử dụng trong: Candidate xem lịch sử ứng tuyển
     * - JPQL: JOIN qua CV để lấy candidateId
     * - Complex: Application -> CV -> Candidate
     * @param candidateId ID ứng viên
     * @return List<Application> - Danh sách đơn của ứng viên
     */
    @Query("SELECT a FROM Application a WHERE a.cv.candidate.candidateId = :candidateId")
    List<Application> findByCandidateId(@Param("candidateId") Long candidateId);
    
    /**
     * Kiểm tra Candidate đã ứng tuyển Job chưa
     * - Sử dụng trong: Validation (prevent duplicate application)
     * - JPQL: JOIN qua CV để check candidateId
     * @param jobId ID tin tuyển dụng
     * @param candidateId ID ứng viên
     * @return true nếu đã ứng tuyển, false nếu chưa
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM Application a WHERE a.job.jobId = :jobId AND a.cv.candidate.candidateId = :candidateId")
    boolean existsByJobIdAndCandidateId(@Param("jobId") Long jobId, @Param("candidateId") Long candidateId);
    
    /**
     * Tìm kiếm Application theo Job và CV
     * - Sử dụng trong: Check specific application
     * - Query: WHERE job.jobId = :jobId AND cv.cvId = :cvId
     * @param jobId ID tin tuyển dụng
     * @param cvId ID CV
     * @return Optional<Application> - Có thể empty
     */
    Optional<Application> findByJobJobIdAndCvCvId(Long jobId, Long cvId);
    
    /**
     * Đếm số lượng Application theo status
     * - Sử dụng trong: Admin Dashboard statistics
     * - Query: SELECT COUNT(*) WHERE applicationStatus = :status
     * @param status Trạng thái
     * @return Số lượng đơn
     */
    long countByApplicationStatus(ApplicationStatus status);
    
    /**
     * Đếm đơn nộp sau một thời điểm
     * - Sử dụng trong: Statistics (đơn hôm nay)
     * - JPQL: WHERE applyTime >= :startDate
     * @param startDate Thời điểm bắt đầu
     * @return Số lượng đơn
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.applyTime >= :startDate")
    long countByApplyTimeAfter(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Đếm đơn nộp trong khoảng thời gian
     * - Sử dụng trong: Statistics (đơn tháng này, tuần này)
     * - JPQL: WHERE applyTime BETWEEN startDate AND endDate
     * @param startDate Thời điểm bắt đầu
     * @param endDate Thời điểm kết thúc
     * @return Số lượng đơn
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.applyTime BETWEEN :startDate AND :endDate")
    long countByApplyTimeBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Tìm Application của Company với JOIN FETCH (tránh N+1 query)
     * - Eager load: CV, Candidate, Job
     * - Sử dụng trong: Employer xem đơn (getApplicationsForMyJobs)
     * - Performance: 1 query thay vì N+1 queries
     * @param companyId ID công ty
     * @return List<Application> với relationships đã load sẵn
     */
    @Query("SELECT DISTINCT a FROM Application a " +
           "JOIN FETCH a.cv cv " +
           "JOIN FETCH cv.candidate c " +
           "JOIN FETCH a.job j " +
           "WHERE j.company.companyId = :companyId")
    List<Application> findByCompanyIdWithDetails(@Param("companyId") Long companyId);
    
    /**
     * Tìm Application của Company có filter Job và Status với JOIN FETCH
     * - Eager load: CV, Candidate, Job
     * - Supports filtering by jobId và status
     * - NULL-safe: jobId và status có thể null (lấy tất cả)
     * @param companyId ID công ty (required)
     * @param jobId ID tin (optional - null = tất cả)
     * @param status Trạng thái (optional - null = tất cả)
     * @return List<Application> với filters applied
     */
    @Query("SELECT DISTINCT a FROM Application a " +
           "JOIN FETCH a.cv cv " +
           "JOIN FETCH cv.candidate c " +
           "JOIN FETCH a.job j " +
           "WHERE j.company.companyId = :companyId " +
           "AND (:jobId IS NULL OR j.jobId = :jobId) " +
           "AND (:status IS NULL OR a.applicationStatus = :status)")
    List<Application> findByCompanyIdWithDetailsFiltered(
        @Param("companyId") Long companyId,
        @Param("jobId") Long jobId,
        @Param("status") ApplicationStatus status
    );
}
