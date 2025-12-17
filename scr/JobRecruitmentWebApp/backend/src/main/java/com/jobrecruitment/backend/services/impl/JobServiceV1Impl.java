package com.jobrecruitment.backend.services.impl;

import com.jobrecruitment.backend.dtos.request.JobRequest;
import com.jobrecruitment.backend.dtos.response.JobResponse;
import com.jobrecruitment.backend.entities.Company;
import com.jobrecruitment.backend.entities.Job;
import com.jobrecruitment.backend.entities.JobCategory;
import com.jobrecruitment.backend.entities.User;
import com.jobrecruitment.backend.enums.JobStatus;
import com.jobrecruitment.backend.exceptions.ResourceNotFoundException;
import com.jobrecruitment.backend.exceptions.ValidationException;
import com.jobrecruitment.backend.mappers.JobMapper;
import com.jobrecruitment.backend.repositories.CompanyRepository;
import com.jobrecruitment.backend.repositories.JobCategoryRepository;
import com.jobrecruitment.backend.repositories.JobRepository;
import com.jobrecruitment.backend.repositories.UserRepository;
import com.jobrecruitment.backend.services.JobServiceV1;
import com.jobrecruitment.backend.specifications.JobSpecification;
import com.jobrecruitment.backend.utils.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

/**
 * JobServiceV1Impl - Service triển khai logic nghiệp vụ quản lý Job (Tin tuyển dụng)
 * 
 * Chức năng chính:
 * - Hiển thị danh sách job với phân trang và lọc động (Pagination & Dynamic Filtering)
 * - Tạo job mới cho Employer (CRUD - Create)
 * - Cập nhật thông tin job (CRUD - Update)
 * - Thay đổi trạng thái job (Ẩn/Đóng job)
 * - Lấy chi tiết job theo ID
 * - Tìm kiếm job theo công ty
 * 
 * Đặc điểm kỹ thuật:
 * - JPA Specifications: Lọc động với nhiều tiêu chí tìm kiếm
 * - Spring Data Pageable: Phân trang cho hiệu suất tốt
 * - Transaction Management: Đảm bảo tính toàn vẹn dữ liệu
 * - JobStatus Logic: Tự động set WAIT hoặc PENDING dựa trên StartDate
 * - Validation: Kiểm tra StartDate <= EndDate (RBNT rule)
 * 
 * Business Rules được áp dụng:
 * - RBNT (Section 4.6.C.7): StartDate <= EndDate, không apply vào job đã hết hạn
 * - RBGTN (Section 4.6.B.4): JobSalary phải > 0
 * - RBSL (Section 4.6.B.5): MaxCandidates phải >= 0
 * - Job Status Logic: WAIT nếu StartDate > Today, ngược lại PENDING
 * 
 * @see JobServiceV1
 * @see JobControllerV1
 * @see JobSpecification
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JobServiceV1Impl implements JobServiceV1 {
    
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;
    private final CodeGenerator codeGenerator;
    
    /**
     * Lấy danh sách tất cả job với phân trang và lọc động.
     * 
     * Quy trình xử lý:
     * 1. Xây dựng JPA Specification dựa trên các tiêu chí lọc
     * 2. Thực thi query với Specification và Pageable
     * 3. Chuyển đổi Job entity sang JobResponse DTO
     * 4. Trả về Page<JobResponse> để hỗ trợ phân trang ở frontend
     * 
     * Các tiêu chí lọc được hỗ trợ:
     * - jobTitle: Tìm kiếm theo tên job (LIKE %...%)
     * - jobStatus: Lọc theo trạng thái (PENDING, WAIT, ACTIVE, CLOSED, HIDDEN)
     * - jobLocation: Tìm theo địa điểm làm việc
     * - companyId: Lọc job của công ty cụ thể
     * - jcId: Lọc theo danh mục job (IT, Marketing, Sales...)
     * - minSalary/maxSalary: Lọc theo khoảng lương
     * 
     * @param pageable Đối tượng phân trang (page, size, sort)
     * @param jobTitle Từ khóa tìm kiếm tiêu đề job (optional)
     * @param jobStatus Trạng thái job cần lọc (optional)
     * @param jobLocation Địa điểm làm việc (optional)
     * @param companyId ID công ty (optional)
     * @param jcId ID danh mục job (optional)
     * @param minSalary Mức lương tối thiểu (optional)
     * @param maxSalary Mức lương tối đa (optional)
     * @return Page<JobResponse> chứa danh sách job và thông tin phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> getAllJobs(
            Pageable pageable,
            String jobTitle,
            JobStatus jobStatus,
            String jobLocation,
            Long companyId,
            Integer jcId,
            Double minSalary,
            Double maxSalary
    ) {
        log.info("Fetching jobs with filters - title: {}, status: {}, location: {}, companyId: {}, categoryId: {}, salary: {}-{}",
                jobTitle, jobStatus, jobLocation, companyId, jcId, minSalary, maxSalary);
        
        // Build dynamic specification using filter criteria
        Specification<Job> spec = JobSpecification.withFilters(
                jobTitle, jobStatus, jobLocation, companyId, jcId, minSalary, maxSalary
        );
        
        // Execute query with specification and pagination
        Page<Job> jobPage = jobRepository.findAll(spec, pageable);
        
        log.info("Found {} jobs (page {} of {})", 
                jobPage.getTotalElements(), 
                jobPage.getNumber() + 1, 
                jobPage.getTotalPages());
        
        // Map entities to DTOs
        return jobPage.map(jobMapper::toResponse);
    }
    
    /**
     * Lấy thông tin chi tiết job theo ID.
     * 
     * Quy trình xử lý:
     * 1. Tìm Job entity trong database theo jobId
     * 2. Nếu không tìm thấy, throw ResourceNotFoundException
     * 3. Chuyển đổi Job entity sang JobResponse DTO
     * 4. Trả về JobResponse với đầy đủ thông tin job
     * 
     * Thông tin trả về bao gồm:
     * - Thông tin cơ bản: jobTitle, jobDescription, jobRequirement
     * - Thông tin công ty: companyName, companyEmail, logoURL
     * - Thông tin danh mục: jcName, jcDescription
     * - Điều kiện: jobSalary, jobLocation, maxCandidates
     * - Thời gian: startDate, endDate
     * - Trạng thái: jobStatus (PENDING, WAIT, ACTIVE, CLOSED, HIDDEN)
     * 
     * @param jobId ID của job cần lấy thông tin
     * @return JobResponse chứa thông tin chi tiết job
     * @throws ResourceNotFoundException Nếu không tìm thấy job với ID này
     */
    @Override
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long jobId) {
        log.info("Fetching job with ID: {}", jobId);
        
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));
        
        return jobMapper.toResponse(job);
    }
    
    /**
     * Tạo job mới (Chỉ dành cho Employer - Role DN).
     * 
     * Quy trình thực hiện (Transaction - Rollback nếu lỗi):
     * 1. Validate JobRequest:
     *    - StartDate <= EndDate (RBNT rule)
     *    - JobSalary > 0 (RBGTN rule)
     *    - MaxCandidates >= 0 (RBSL rule)
     * 2. Tìm User entity từ username (email của employer)
     * 3. Tìm Company profile liên kết với User
     * 4. Tìm JobCategory theo jcId
     * 5. Tạo JobCode unique: "VL" + 8 số ngẫu nhiên
     * 6. Tạo Job entity với:
     *    - Company, JobCategory, JobCode
     *    - Thông tin job từ request
     *    - JobStatus:
     *      + WAIT nếu startDate > Today (chưa đến ngày mở)
     *      + PENDING nếu startDate <= Today (chờ admin duyệt)
     * 7. Lưu Job vào database
     * 8. Chuyển đổi sang JobResponse và trả về
     * 
     * @param request JobRequest chứa thông tin job cần tạo
     * @param username Email của employer (từ JWT token)
     * @return JobResponse chứa thông tin job vừa tạo
     * @throws ValidationException Nếu dữ liệu không hợp lệ (StartDate > EndDate, Salary <= 0)
     * @throws ResourceNotFoundException Nếu không tìm thấy User, Company hoặc JobCategory
     */
    @Override
    public JobResponse createJob(JobRequest request, String username) {
        log.info("Creating job for employer: {}", username);
        
        // Validate request
        validateJobRequest(request);
        
        // Get employer
        User employer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        // Get company
        Company company = companyRepository.findByUserUserId(employer.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found for employer: " + username));
        
        // Get job category
        JobCategory category = jobCategoryRepository.findById(request.getJcId())
                .orElseThrow(() -> new ResourceNotFoundException("Job category not found with ID: " + request.getJcId()));
        
        // Generate unique JobCode
        String jobCode = codeGenerator.generateJobCode(code -> jobRepository.existsByJobCode(code));
        
        // Create job entity
        Job job = new Job();
        job.setCompany(company);
        job.setJobCategory(category);
        job.setJobCode(jobCode);
        job.setJobTitle(request.getJobTitle());
        job.setJobDescription(request.getJobDescription());
        job.setJobRequirement(request.getJobRequirement());
        job.setJobSalary(request.getJobSalary());
        job.setJobLocation(request.getJobLocation());
        job.setStartDate(request.getStartDate());
        job.setEndDate(request.getEndDate());
        job.setMaxCandidates(request.getMaxCandidates());
        
        // Set job status: WAIT if startDate is in the future, otherwise PENDING
        if (request.getStartDate().isAfter(LocalDate.now())) {
            job.setJobStatus(JobStatus.WAIT);
        } else {
            job.setJobStatus(JobStatus.PENDING);
        }
        
        Job savedJob = jobRepository.save(job);
        log.info("Job created successfully with ID: {}", savedJob.getJobId());
        
        return jobMapper.toResponse(savedJob);
    }
    
    /**
     * Cập nhật thông tin job (Chỉ Employer được sửa job của mình).
     * 
     * Quy trình thực hiện:
     * 1. Validate JobRequest (StartDate <= EndDate, Salary > 0, MaxCandidates >= 0)
     * 2. Tìm Job theo jobId, throw exception nếu không tồn tại
     * 3. Xác minh quyền sở hữu: Job phải thuộc company của employer
     * 4. Nếu jcId thay đổi, tìm JobCategory mới
     * 5. Cập nhật các field của Job entity từ JobRequest
     * 6. Lưu Job đã cập nhật vào database
     * 7. Trả về JobResponse
     * 
     * Lưu ý:
     * - Không thể thay đổi JobCode (unique, immutable)
     * - Không thể thay đổi Company (ownership không đổi)
     * - JobStatus không được cập nhật ở đây (dùng updateJobStatus)
     * 
     * @param jobId ID của job cần cập nhật
     * @param request JobRequest chứa thông tin mới
     * @param username Email của employer (từ JWT token)
     * @return JobResponse chứa thông tin job đã cập nhật
     * @throws ResourceNotFoundException Nếu không tìm thấy job hoặc category
     * @throws ValidationException Nếu dữ liệu không hợp lệ hoặc không phải job của employer
     */
    @Override
    public JobResponse updateJob(Long jobId, JobRequest request, String username) {
        log.info("Updating job {} by employer: {}", jobId, username);
        
        // Validate request
        validateJobRequest(request);
        
        // Get job and verify ownership
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));
        
        verifyJobOwnership(job, username);
        
        // Get job category if changed
        if (!job.getJobCategory().getJcId().equals(request.getJcId())) {
            JobCategory category = jobCategoryRepository.findById(request.getJcId())
                    .orElseThrow(() -> new ResourceNotFoundException("Job category not found with ID: " + request.getJcId()));
            job.setJobCategory(category);
        }
        
        // Update job fields using mapper
        jobMapper.updateEntityFromRequest(job, request);
        
        Job updatedJob = jobRepository.save(job);
        log.info("Job {} updated successfully", jobId);
        
        return jobMapper.toResponse(updatedJob);
    }
    
    /**
     * Thay đổi trạng thái job (Chỉ Employer được thay đổi trạng thái job của mình).
     * 
     * Các trạng thái hợp lệ (JobStatus enum):
     * - PENDING: Chờ admin duyệt (mặc định khi tạo job)
     * - WAIT: Chưa đến ngày mở (startDate > Today)
     * - ACTIVE: Đang hoạt động, nhận application
     * - CLOSED: Đóng tuyển dụng, không nhận application mới
     * - HIDDEN: Tạm ẩn, không hiển thị trên danh sách công khai
     * 
     * Quy trình thực hiện:
     * 1. Tìm Job theo jobId
     * 2. Xác minh quyền sở hữu (job phải thuộc company của employer)
     * 3. Cập nhật JobStatus sang giá trị mới
     * 4. Lưu vào database và trả về JobResponse
     * 
     * Use case:
     * - Employer muốn ẩn job tạm thời: ACTIVE -> HIDDEN
     * - Employer muốn đóng tuyển dụng: ACTIVE -> CLOSED
     * - Employer muốn mở lại job: HIDDEN/CLOSED -> ACTIVE
     * 
     * @param jobId ID của job cần thay đổi trạng thái
     * @param newStatus Trạng thái mới (PENDING, WAIT, ACTIVE, CLOSED, HIDDEN)
     * @param username Email của employer (từ JWT token)
     * @return JobResponse chứa thông tin job với trạng thái mới
     * @throws ResourceNotFoundException Nếu không tìm thấy job
     * @throws ValidationException Nếu job không thuộc employer
     */
    @Override
    public JobResponse updateJobStatus(Long jobId, JobStatus newStatus, String username) {
        log.info("Updating status of job {} to {} by employer: {}", jobId, newStatus, username);
        
        // Get job and verify ownership
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));
        
        verifyJobOwnership(job, username);
        
        // Update status
        job.setJobStatus(newStatus);
        Job updatedJob = jobRepository.save(job);
        
        log.info("Job {} status updated to {}", jobId, newStatus);
        return jobMapper.toResponse(updatedJob);
    }
    
    /**
     * Delete job (Soft delete - change to HIDDEN)
     */
    @Override
    public void deleteJob(Long jobId, String username) {
        log.info("Deleting job {} by employer: {}", jobId, username);
        
        // Get job and verify ownership
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));
        
        verifyJobOwnership(job, username);
        
        // Soft delete - change status to HIDDEN
        job.setJobStatus(JobStatus.HIDDEN);
        jobRepository.save(job);
        
        log.info("Job {} soft deleted (status set to HIDDEN)", jobId);
    }
    
    /**
     * Get jobs posted by authenticated employer with pagination
     */
    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> getMyJobs(Pageable pageable, String username) {
        log.info("Fetching jobs for employer: {}", username);
        
        // Get employer
        User employer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        // Get company
        Company company = companyRepository.findByUserUserId(employer.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found for employer: " + username));
        
        // Build specification to filter by company
        Specification<Job> spec = JobSpecification.hasCompanyId(company.getCompanyId());
        
        // Execute query with pagination
        Page<Job> jobPage = jobRepository.findAll(spec, pageable);
        
        log.info("Found {} jobs for employer (page {} of {})",
                jobPage.getTotalElements(),
                jobPage.getNumber() + 1,
                jobPage.getTotalPages());
        
        // Map to DTOs
        return jobPage.map(jobMapper::toResponse);
    }
    
    /**
     * Validate job request
     */
    private void validateJobRequest(JobRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException("Start date must be before end date");
            }
        }
        
        if (request.getJobSalary() != null && request.getJobSalary() < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        
        if (request.getMaxCandidates() != null && request.getMaxCandidates() < 0) {
            throw new IllegalArgumentException("Number of recruits must not be negative");
        }
    }
    
    /**
     * Verify job ownership by employer
     */
    private void verifyJobOwnership(Job job, String username) {
        User employer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Company company = companyRepository.findByUserUserId(employer.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found for employer: " + username));
        
        if (!Objects.equals(job.getCompany().getCompanyId(), company.getCompanyId())) {
            throw new ValidationException("You can only modify your own job postings");
        }
    }
}
