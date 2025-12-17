package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.dtos.request.JobRequest;
import com.jobrecruitment.backend.dtos.response.JobResponse;
import com.jobrecruitment.backend.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * JobServiceV1 - Service interface cho quản lý tin tuyển dụng
 * 
 * Chức năng:
 * - Employer: Đăng tin, chỉnh sửa, xóa, đóng/mở tin tuyển dụng
 * - Candidate/Public: Xem, tìm kiếm tin tuyển dụng (phân trang + filter)
 * - Admin: Quản lý tất cả tin tuyển dụng
 * 
 * Business Rules:
 * - Employer chỉ quản lý được Job của mình
 * - Job có 3 trạng thái: OPEN (mở), CLOSED (đóng), HIDDEN (ẩn)
 * - Xóa Job = soft delete (set JobStatus = HIDDEN)
 * 
 * Features:
 * - Pagination: Spring Data JPA Pageable
 * - Dynamic filtering: JPA Specifications (title, location, category, salary range, status...)
 * - Code generation: JobCode = JOB + 8 số
 * 
 * @see JobServiceV1Impl
 */
public interface JobServiceV1 {
    
    /**
     * Get all jobs with pagination and dynamic filtering
     * 
     * @param pageable Pagination parameters (page, size, sort)
     * @param jobTitle Filter by job title (partial match)
     * @param jobStatus Filter by job status
     * @param jobLocation Filter by location (partial match)
     * @param companyId Filter by company ID
     * @param jcId Filter by category ID
     * @param minSalary Minimum salary filter
     * @param maxSalary Maximum salary filter
     * @return Page of JobResponse DTOs
     */
    Page<JobResponse> getAllJobs(
            Pageable pageable,
            String jobTitle,
            JobStatus jobStatus,
            String jobLocation,
            Long companyId,
            Integer jcId,
            Double minSalary,
            Double maxSalary
    );
    
    /**
     * Get job by ID
     * 
     * @param jobId Job ID
     * @return JobResponse DTO
     */
    JobResponse getJobById(Long jobId);
    
    /**
     * Create new job posting (Employer only)
     * 
     * @param request Job creation request
     * @param username Authenticated employer username
     * @return Created JobResponse DTO
     */
    JobResponse createJob(JobRequest request, String username);
    
    /**
     * Update existing job (Employer only - own jobs)
     * 
     * @param jobId Job ID
     * @param request Job update request
     * @param username Authenticated employer username
     * @return Updated JobResponse DTO
     */
    JobResponse updateJob(Long jobId, JobRequest request, String username);
    
    /**
     * Update job status (Employer only - own jobs)
     * 
     * @param jobId Job ID
     * @param newStatus New job status
     * @param username Authenticated employer username
     * @return Updated JobResponse DTO
     */
    JobResponse updateJobStatus(Long jobId, JobStatus newStatus, String username);
    
    /**
     * Delete job (Soft delete - change to HIDDEN)
     * 
     * @param jobId Job ID
     * @param username Authenticated employer username
     */
    void deleteJob(Long jobId, String username);
    
    /**
     * Get jobs posted by authenticated employer with pagination
     * 
     * @param pageable Pagination parameters
     * @param username Authenticated employer username
     * @return Page of JobResponse DTOs
     */
    Page<JobResponse> getMyJobs(Pageable pageable, String username);
}
