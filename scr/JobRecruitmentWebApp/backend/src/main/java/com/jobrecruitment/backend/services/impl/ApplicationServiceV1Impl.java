package com.jobrecruitment.backend.services.impl;

import com.jobrecruitment.backend.dtos.request.ApplicationRequest;
import com.jobrecruitment.backend.dtos.response.ApplicationResponse;
import com.jobrecruitment.backend.entities.Application;
import com.jobrecruitment.backend.entities.CV;
import com.jobrecruitment.backend.entities.Candidate;
import com.jobrecruitment.backend.entities.Company;
import com.jobrecruitment.backend.entities.Job;
import com.jobrecruitment.backend.entities.User;
import com.jobrecruitment.backend.enums.ApplicationStatus;
import com.jobrecruitment.backend.enums.CVStatus;
import com.jobrecruitment.backend.enums.JobStatus;
import com.jobrecruitment.backend.exceptions.ResourceNotFoundException;
import com.jobrecruitment.backend.exceptions.ValidationException;
import com.jobrecruitment.backend.mappers.ApplicationMapper;
import com.jobrecruitment.backend.repositories.ApplicationRepository;
import com.jobrecruitment.backend.repositories.CVRepository;
import com.jobrecruitment.backend.repositories.CandidateRepository;
import com.jobrecruitment.backend.repositories.CompanyRepository;
import com.jobrecruitment.backend.repositories.JobRepository;
import com.jobrecruitment.backend.repositories.UserRepository;
import com.jobrecruitment.backend.services.ApplicationServiceV1;
import com.jobrecruitment.backend.specifications.ApplicationSpecification;
import com.jobrecruitment.backend.utils.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ApplicationServiceV1Impl - Service triển khai logic nghiệp vụ quản lý Application (Đơn ứng tuyển)
 * 
 * Chức năng chính:
 * - Hiển thị danh sách application với phân trang và lọc động
 * - Nộp đơn ứng tuyển (Apply to Job) - Candidate only
 * - Xem đơn ứng tuyển của mình - Candidate
 * - Xem đơn ứng tuyển cho job của mình - Employer
 * - Duyệt/Từ chối đơn ứng tuyển (Approve/Reject) - Employer only
 * - Xóa đơn ứng tuyển - Candidate hoặc Employer
 * 
 * Đặc điểm kỹ thuật:
 * - JPA Specifications: Lọc động theo nhiều tiêu chí
 * - Spring Data Pageable: Phân trang hiệu quả
 * - Transaction Management: Đảm bảo ACID
 * - ApplicationCode Generation: "DX" + 8 số ngẫu nhiên unique
 * - Business Rule Validation: RBNT, RBCV, RBUT
 * 
 * Business Rules được áp dụng:
 * - RBNT (Section 4.6.C.7): Chỉ được apply vào job ACTIVE và trong khoảng thời gian (StartDate <= Today <= EndDate)
 * - RBCV (Section 4.6.D.9): CV phải ở trạng thái ACTIVE mới được dùng để apply
 * - RBUT (Section 4.6.D.10): ApplicationStatus phải là PENDING, APPROVED hoặc REJECTED
 * - Duplicate Check: Không được apply trùng job (1 candidate - 1 job chỉ 1 application)
 * - Ownership Validation: Candidate chỉ dùng CV của mình
 * 
 * @see ApplicationServiceV1
 * @see ApplicationControllerV1
 * @see ApplicationSpecification
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApplicationServiceV1Impl implements ApplicationServiceV1 {
    
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final CVRepository cvRepository;
    private final CandidateRepository candidateRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ApplicationMapper applicationMapper;
    private final CodeGenerator codeGenerator;
    
    /**
     * Lấy danh sách tất cả application với phân trang và lọc động.
     * 
     * Chức năng dành cho Admin xem toàn bộ application trong hệ thống.
     * 
     * Quy trình xử lý:
     * 1. Xây dựng JPA Specification dựa trên các tiêu chí lọc
     * 2. Thực thi query với Specification và Pageable
     * 3. Chuyển đổi Application entity sang ApplicationResponse DTO
     * 4. Trả về Page<ApplicationResponse> cho frontend
     * 
     * Các tiêu chí lọc được hỗ trợ:
     * - status: Trạng thái application (PENDING, APPROVED, REJECTED)
     * - candidateId: Lọc theo ứng viên cụ thể
     * - jobId: Lọc theo job cụ thể
     * - companyId: Lọc theo công ty
     * - startTime/endTime: Lọc theo khoảng thời gian nộp đơn
     * 
     * @param pageable Đối tượng phân trang (page, size, sort)
     * @param status Trạng thái application (optional)
     * @param candidateId ID ứng viên (optional)
     * @param jobId ID job (optional)
     * @param companyId ID công ty (optional)
     * @param startTime Thời gian bắt đầu (optional)
     * @param endTime Thời gian kết thúc (optional)
     * @return Page<ApplicationResponse> chứa danh sách application và thông tin phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getAllApplications(
            Pageable pageable,
            ApplicationStatus status,
            Long candidateId,
            Long jobId,
            Long companyId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        log.info("Fetching applications with filters - status: {}, candidateId: {}, jobId: {}, companyId: {}, time: {}-{}",
                status, candidateId, jobId, companyId, startTime, endTime);
        
        // Build dynamic specification using filter criteria
        Specification<Application> spec = ApplicationSpecification.withFilters(
                status, candidateId, jobId, companyId, startTime, endTime
        );
        
        // Execute query with specification and pagination
        Page<Application> applicationPage = applicationRepository.findAll(spec, pageable);
        
        log.info("Found {} applications (page {} of {})", 
                applicationPage.getTotalElements(), 
                applicationPage.getNumber() + 1, 
                applicationPage.getTotalPages());
        
        // Map entities to DTOs
        return applicationPage.map(applicationMapper::toResponse);
    }
    
    /**
     * Lấy thông tin chi tiết application theo ID.
     * 
     * Thông tin trả về bao gồm:
     * - Thông tin đơn: applicationCode, applyTime, applicationStatus
     * - Thông tin job: jobTitle, jobLocation, jobSalary
     * - Thông tin ứng viên: candidateName, candidateEmail, candidatePhone
     * - Thông tin CV: cvCode, cvFile (link download)
     * - Thông tin công ty: companyName, companyEmail
     * 
     * @param applicationId ID của application cần lấy
     * @return ApplicationResponse chứa thông tin chi tiết
     * @throws ResourceNotFoundException Nếu không tìm thấy application với ID này
     */
    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long applicationId) {
        log.info("Fetching application with ID: {}", applicationId);
        
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));
        
        return applicationMapper.toResponse(application);
    }
    
    /**
     * Nộp đơn ứng tuyển vào job (Chỉ dành cho Candidate - Role UV).
     * 
     * Quy trình thực hiện (Transaction - Rollback nếu lỗi):
     * 1. Lấy User và Candidate profile từ username (JWT token)
     * 2. Lấy Job theo jobId
     * 3. Validation 1: Kiểm tra Job ở trạng thái ACTIVE (RBNT rule)
     * 4. Validation 2: Kiểm tra thời gian apply nằm trong khoảng [StartDate, EndDate] (RBNT rule)
     * 5. Lấy CV theo cvId
     * 6. Validation 3: Kiểm tra CV thuộc về candidate (ownership check)
     * 7. Validation 4: Kiểm tra CV ở trạng thái ACTIVE (RBCV rule)
     * 8. Validation 5: Kiểm tra candidate chưa apply vào job này (duplicate check)
     * 9. Tạo ApplicationCode unique: "DX" + 8 số ngẫu nhiên
     * 10. Tạo Application entity với:
     *     - Job, CV liên kết
     *     - ApplicationCode
     *     - ApplyTime = LocalDateTime.now()
     *     - ApplicationStatus = PENDING (chờ employer duyệt)
     * 11. Lưu Application vào database
     * 12. Trả về ApplicationResponse
     * 
     * @param request ApplicationRequest chứa jobId và cvId
     * @param username Email của candidate (từ JWT token)
     * @return ApplicationResponse chứa thông tin đơn vừa nộp
     * @throws ResourceNotFoundException Nếu không tìm thấy User, Candidate, Job hoặc CV
     * @throws ValidationException Nếu vi phạm business rules (Job không ACTIVE, CV không ACTIVE, đã apply, etc.)
     */
    @Override
    public ApplicationResponse applyToJob(ApplicationRequest request, String username) {
        log.info("Processing job application for candidate: {}", username);
        
        // Get authenticated user and their candidate profile
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + username));

        // Get job
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + request.getJobId()));

        // Validation 1: Check if job is ACTIVE (RBNT rule)
        if (job.getJobStatus() != JobStatus.ACTIVE) {
            throw new ValidationException("Job is not active. Cannot apply to this job.");
        }

        // Validation 2: Check if current date is within job date range (RBNT rule)
        LocalDate currentDate = LocalDate.now();
        if (currentDate.isBefore(job.getStartDate()) || currentDate.isAfter(job.getEndDate())) {
            throw new ValidationException(
                "Application date must be within job posting period (" + 
                job.getStartDate() + " to " + job.getEndDate() + ")"
            );
        }

        // Get CV
        CV cv = cvRepository.findById(request.getCvId())
                .orElseThrow(() -> new ResourceNotFoundException("CV not found with ID: " + request.getCvId()));

        // Validation 3: Check if CV belongs to candidate
        if (!cv.getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            throw new ValidationException("CV does not belong to you");
        }

        // Validation 4: Check if CV is ACTIVE (RBCV rule)
        if (cv.getCvStatus() != CVStatus.ACTIVE) {
            throw new ValidationException("CV is not active. Please activate your CV before applying.");
        }

        // Validation 5: Check if candidate has already applied to this job
        if (applicationRepository.existsByJobIdAndCandidateId(job.getJobId(), candidate.getCandidateId())) {
            throw new ValidationException("You have already applied to this job");
        }

        // Generate unique ApplicationCode
        String applicationCode = codeGenerator.generateApplicationCode(code -> applicationRepository.existsByApplicationCode(code));

        // Create application entity
        Application application = new Application();
        application.setJob(job);
        application.setCv(cv);
        application.setApplicationCode(applicationCode);
        application.setApplyTime(LocalDateTime.now());
        application.setApplicationStatus(ApplicationStatus.PENDING);

        Application savedApplication = applicationRepository.save(application);
        log.info("Application created successfully with ID: {}", savedApplication.getApplicationId());
        
        return applicationMapper.toResponse(savedApplication);
    }
    
    /**
     * Update application status (Employer only)
     */
    @Override
    public ApplicationResponse updateApplicationStatus(Long applicationId, ApplicationStatus newStatus, String username) {
        log.info("Updating status of application {} to {} by employer: {}", applicationId, newStatus, username);
        
        // Get application
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));
        
        // Verify employer owns the job
        verifyJobOwnership(application.getJob(), username);
        
        // Update status
        application.setApplicationStatus(newStatus);
        Application updatedApplication = applicationRepository.save(application);
        
        log.info("Application {} status updated to {}", applicationId, newStatus);
        return applicationMapper.toResponse(updatedApplication);
    }
    
    /**
     * Withdraw application (Candidate only)
     */
    @Override
    public void withdrawApplication(Long applicationId, String username) {
        log.info("Withdrawing application {} by candidate: {}", applicationId, username);
        
        // Get application
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));
        
        // Verify candidate owns the application
        verifyCandidateOwnership(application, username);
        
        // Delete application (hard delete for withdrawal)
        applicationRepository.delete(application);
        
        log.info("Application {} withdrawn successfully", applicationId);
    }
    
    /**
     * Get applications by authenticated candidate with pagination
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getMyApplications(Pageable pageable, String username) {
        log.info("Fetching applications for candidate: {}", username);
        
        // Get candidate
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + username));
        
        // Build specification to filter by candidate
        Specification<Application> spec = ApplicationSpecification.hasCandidateId(candidate.getCandidateId());
        
        // Execute query with pagination
        Page<Application> applicationPage = applicationRepository.findAll(spec, pageable);
        
        log.info("Found {} applications for candidate (page {} of {})",
                applicationPage.getTotalElements(),
                applicationPage.getNumber() + 1,
                applicationPage.getTotalPages());
        
        // Map to DTOs
        return applicationPage.map(applicationMapper::toResponse);
    }
    
    /**
     * Get applications for jobs posted by authenticated employer with pagination
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsForMyJobs(
            Pageable pageable, 
            String username, 
            Long jobId, 
            ApplicationStatus status
    ) {
        log.info("Fetching applications for employer: {} (jobId: {}, status: {})", username, jobId, status);
        
        // Get employer
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Company company = companyRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found for user: " + username));
        
        // Build specification
        Specification<Application> spec = ApplicationSpecification.hasCompanyId(company.getCompanyId());
        
        // Add optional filters
        if (jobId != null) {
            spec = spec.and(ApplicationSpecification.hasJobId(jobId));
        }
        if (status != null) {
            spec = spec.and(ApplicationSpecification.hasStatus(status));
        }
        
        // Execute query with pagination
        Page<Application> applicationPage = applicationRepository.findAll(spec, pageable);
        
        log.info("Found {} applications for employer (page {} of {})",
                applicationPage.getTotalElements(),
                applicationPage.getNumber() + 1,
                applicationPage.getTotalPages());
        
        // Map to DTOs
        return applicationPage.map(applicationMapper::toResponse);
    }
    
    /**
     * Verify job ownership by employer
     */
    private void verifyJobOwnership(Job job, String username) {
        User employer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Company company = companyRepository.findByUserUserId(employer.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found for user: " + username));
        
        if (!Objects.equals(job.getCompany().getCompanyId(), company.getCompanyId())) {
            throw new ValidationException("You can only manage applications for your own job postings");
        }
    }
    
    /**
     * Verify application ownership by candidate
     */
    private void verifyCandidateOwnership(Application application, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + username));
        
        Long applicationCandidateId = application.getCv().getCandidate().getCandidateId();
        if (!Objects.equals(applicationCandidateId, candidate.getCandidateId())) {
            throw new ValidationException("You can only withdraw your own applications");
        }
    }
}
