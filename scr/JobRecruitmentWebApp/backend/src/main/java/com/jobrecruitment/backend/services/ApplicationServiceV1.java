package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.dtos.request.ApplicationRequest;
import com.jobrecruitment.backend.dtos.response.ApplicationResponse;
import com.jobrecruitment.backend.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * ApplicationServiceV1 - Service interface cho quản lý đơn ứng tuyển
 * 
 * Chức năng:
 * - Candidate: Nộp đơn, xem danh sách đơn của mình, rút đơn
 * - Employer: Xem danh sách đơn cho job của mình, duyệt/từ chối đơn
 * - Admin: Xem tất cả đơn (phân trang, filter)
 * 
 * Business Rules:
 * - 1 Candidate chỉ nộp được 1 đơn cho 1 Job
 * - Chỉ Employer sở hữu Job mới có thể duyệt/từ chối đơn
 * - Chỉ Candidate sở hữu đơn mới có thể rút đơn
 * 
 * Features:
 * - Pagination: Spring Data JPA Pageable
 * - Dynamic filtering: JPA Specifications (status, candidateId, jobId, companyId, time range)
 * - Code generation: ApplicationCode = APP + 8 số
 * 
 * @see ApplicationServiceV1Impl
 */
public interface ApplicationServiceV1 {
    
    /**
     * Lấy danh sách tất cả đơn ứng tuyển (Admin only, phân trang + filter)
     * 
     * Sử dụng:
     * - API GET /api/v1/admin/applications (Admin Dashboard)
     * 
     * Dynamic filtering (JPA Specifications):
     * - status: PENDING/APPROVED/REJECTED (có thể null)
     * - candidateId: Lọc theo Candidate (có thể null)
     * - jobId: Lọc theo Job (có thể null)
     * - companyId: Lọc theo Company (JOIN qua Job, có thể null)
     * - startTime/endTime: Lọc theo khoảng thời gian nộp đơn (có thể null)
     * 
     * @param pageable Pagination parameters (page, size, sort)
     * @param status Filter theo trạng thái (optional)
     * @param candidateId Filter theo candidateId (optional)
     * @param jobId Filter theo jobId (optional)
     * @param companyId Filter theo companyId (JOIN qua Job, optional)
     * @param startTime Filter theo applyTime >= startTime (optional)
     * @param endTime Filter theo applyTime <= endTime (optional)
     * @return Page<ApplicationResponse> (phân trang)
     */
    Page<ApplicationResponse> getAllApplications(
            Pageable pageable,
            ApplicationStatus status,
            Long candidateId,
            Long jobId,
            Long companyId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
    
    /**
     * Lấy chi tiết đơn ứng tuyển theo ID
     * 
     * Sử dụng:
     * - API GET /api/v1/applications/{applicationId}
     * - Employer xem chi tiết đơn ứng tuyển vào job của mình
     * - Candidate xem chi tiết đơn của mình
     * 
     * @param applicationId Application ID
     * @return ApplicationResponse (bao gồm jobId, jobTitle, cvId, cvCode, applyTime, status)
     * @throws ResourceNotFoundException nếu không tìm thấy application
     */
    ApplicationResponse getApplicationById(Long applicationId);
    
    /**
     * Nộp đơn ứng tuyển (Candidate only)
     * 
     * Sử dụng:
     * - API POST /api/v1/applications
     * 
     * Business Logic:
     * 1. Kiểm tra Job có tồn tại không (jobId)
     * 2. Kiểm tra Job có đang mở không (JobStatus = OPEN)
     * 3. Kiểm tra CV có thuộc Candidate không (cvId)
     * 4. Kiểm tra CV có ACTIVE không (CVStatus = ACTIVE)
     * 5. Kiểm tra Candidate đã ứng tuyển chưa (existsByJobIdAndCandidateId)
     * 6. Tạo Application mới với ApplicationCode = APP + 8 số
     * 
     * @param request ApplicationRequest (jobId, cvId)
     * @param username Username của Candidate đang authenticate
     * @return ApplicationResponse (bao gồm applicationCode, applicationId, applyTime)
     * @throws ValidationException nếu đã ứng tuyển, Job không mở, CV không hợp lệ
     * @throws ResourceNotFoundException nếu Job hoặc CV không tìm thấy
     */
    ApplicationResponse applyToJob(ApplicationRequest request, String username);
    
    /**
     * Cập nhật trạng thái đơn ứng tuyển (Employer only - duyệt/từ chối đơn)
     * 
     * Sử dụng:
     * - API PATCH /api/v1/applications/{applicationId}/status
     * 
     * Business Logic:
     * 1. Kiểm tra Application có tồn tại không
     * 2. Kiểm tra Employer có sở hữu Job không (qua Job.company.user.username)
     * 3. Cập nhật applicationStatus (PENDING -> APPROVED/REJECTED)
     * 
     * @param applicationId Application ID
     * @param newStatus Trạng thái mới (APPROVED/REJECTED)
     * @param username Username của Employer đang authenticate
     * @return ApplicationResponse (đã cập nhật status)
     * @throws ResourceNotFoundException nếu Application không tìm thấy
     * @throws ValidationException nếu Employer không sở hữu Job
     */
    ApplicationResponse updateApplicationStatus(Long applicationId, ApplicationStatus newStatus, String username);
    
    /**
     * Rút đơn ứng tuyển (Candidate only)
     * 
     * Sử dụng:
     * - API DELETE /api/v1/applications/{applicationId}
     * 
     * Business Logic:
     * 1. Kiểm tra Application có tồn tại không
     * 2. Kiểm tra Candidate có sở hữu đơn không (qua CV.candidate.user.username)
     * 3. Xóa Application khỏi database (hard delete hoặc soft delete)
     * 
     * @param applicationId Application ID
     * @param username Username của Candidate đang authenticate
     * @throws ResourceNotFoundException nếu Application không tìm thấy
     * @throws ValidationException nếu Candidate không sở hữu đơn
     */
    void withdrawApplication(Long applicationId, String username);
    
    /**
     * Lấy danh sách đơn ứng tuyển của Candidate (Candidate only, phân trang)
     * 
     * Sử dụng:
     * - API GET /api/v1/candidates/my-applications
     * - Candidate xem tất cả đơn mình đã nộp
     * 
     * Business Logic:
     * 1. Tìm Candidate qua username
     * 2. Lấy tất cả Application qua candidateId (JOIN qua CV)
     * 
     * @param pageable Pagination parameters (page, size, sort)
     * @param username Username của Candidate đang authenticate
     * @return Page<ApplicationResponse> (danh sách đơn của Candidate)
     */
    Page<ApplicationResponse> getMyApplications(Pageable pageable, String username);
    
    /**
     * Lấy danh sách đơn ứng tuyển cho các Job của Employer (Employer only, phân trang + filter)
     * 
     * Sử dụng:
     * - API GET /api/v1/companies/applications
     * - Employer xem tất cả đơn ứng tuyển vào các Job của mình
     * 
     * Business Logic:
     * 1. Tìm Company qua username
     * 2. Lấy tất cả Application cho các Job của Company (JOIN qua Job)
     * 3. Optional filter: jobId (chỉ xem đơn của 1 Job cụ thể)
     * 4. Optional filter: status (PENDING/APPROVED/REJECTED)
     * 
     * @param pageable Pagination parameters (page, size, sort)
     * @param username Username của Employer đang authenticate
     * @param jobId Filter theo jobId (optional, có thể null)
     * @param status Filter theo status (optional, có thể null)
     * @return Page<ApplicationResponse> (danh sách đơn cho các Job của Employer)
     */
    Page<ApplicationResponse> getApplicationsForMyJobs(
            Pageable pageable, 
            String username, 
            Long jobId, 
            ApplicationStatus status
    );
}
