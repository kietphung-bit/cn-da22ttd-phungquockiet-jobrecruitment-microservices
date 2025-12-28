package com.jobrecruitment.backend.controllers;

import com.jobrecruitment.backend.dtos.request.ApplicationRequest;
import com.jobrecruitment.backend.dtos.response.ApiResponse;
import com.jobrecruitment.backend.dtos.response.ApplicationResponse;
import com.jobrecruitment.backend.enums.ApplicationStatus;
import com.jobrecruitment.backend.services.ApplicationServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * ApplicationControllerV1 - RESTful API cho quản lý đơn ứng tuyển (Version 1)
 * 
 * Endpoints: /api/v1/applications
 * 
 * Chức năng chính:
 * - Hỗ trợ phân trang sử dụng Spring Data Pageable
 * - Lọc động sử dụng JPA Specifications
 * - Thiết kế endpoint RESTful với các phương thức HTTP phù hợp
 * - Kiểm soát truy cập dựa trên vai trò (@PreAuthorize)
 * - Tài liệu API toàn diện (Swagger/OpenAPI)
 * 
 * Endpoints:
 * 1. GET    /api/v1/applications                - Liệt kê tất cả đơn ứng tuyển (phân trang, lọc)
 * 2. GET    /api/v1/applications/{id}           - Lấy đơn ứng tuyển theo ID
 * 3. POST   /api/v1/applications                - Nộp đơn ứng tuyển (Chỉ dành cho Ứng viên)
 * 4. PATCH  /api/v1/applications/{id}/status    - Cập nhật trạng thái đơn ứng tuyển (Chỉ dành cho Nhà tuyển dụng)
 * 5. DELETE /api/v1/applications/{id}           - Rút đơn ứng tuyển (Chỉ dành cho Ứng viên)
 * 6. GET    /api/v1/applications/me             - Lấy đơn ứng tuyển của tôi (Chỉ dành cho Ứng viên)
 * 7. GET    /api/v1/applications/company        - Lấy đơn ứng tuyển cho các tin tuyển dụng của tôi (Chỉ dành cho Nhà tuyển dụng)
 */
@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Application Management V1", description = "RESTful APIs for managing job applications with pagination and filtering")
public class ApplicationControllerV1 {
    
    private final ApplicationServiceV1 applicationServiceV1;
    
    /**
     * GET /api/v1/applications - Liệt kê tất cả đơn ứng tuyển (phân trang, lọc)
     * 
     * Query Parameters:
     * - status: Lọc theo trạng thái đơn ứng tuyển (PENDING, APPROVED, REJECTED)
     * - candidateId: Lọc theo ID ứng viên
     * - jobId: Lọc theo ID tin tuyển dụng
     * - companyId: Lọc theo ID công ty
     * - startTime: Lọc các đơn ứng tuyển sau thời gian này
     * - endTime: Lọc các đơn ứng tuyển trước thời gian này
     * - page: Số trang (bắt đầu từ 0, mặc định: 0)
     * - size: Kích thước trang (mặc định: 10)
     * - sort: Tiêu chí sắp xếp (ví dụ: "applyTime,desc")
     * 
     * Quyền truy cập: Người dùng đã xác thực (Quản trị viên/Nhà tuyển dụng/Ứng viên)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADM', 'DN', 'UV')")
    @Operation(
        summary = "List all applications",
        description = "Retrieve a paginated list of job applications with optional filters. " +
                     "Supports filtering by status, candidate, job, company, and time range.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Applications retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        )
    })
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getAllApplications(
            @Parameter(description = "Pagination and sorting parameters. Valid sort fields: 'applyTime', 'appStatus', 'appId'. Example: 'applyTime,desc'",
                      schema = @Schema(type = "string", example = "applyTime,desc"))
            @PageableDefault(size = 10, sort = "applyTime") 
            Pageable pageable,
            
            @RequestParam(required = false) 
            @Parameter(description = "Filter by application status (PENDING, APPROVED, REJECTED)")
            ApplicationStatus status,
            
            @RequestParam(required = false) 
            @Parameter(description = "Filter by candidate ID")
            Long candidateId,
            
            @RequestParam(required = false) 
            @Parameter(description = "Filter by job ID")
            Long jobId,
            
            @RequestParam(required = false) 
            @Parameter(description = "Filter by company ID")
            Long companyId,
            
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "Filter applications submitted after this time (ISO 8601 format)")
            LocalDateTime startTime,
            
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "Filter applications submitted before this time (ISO 8601 format)")
            LocalDateTime endTime
    ) {
        log.info("GET /api/v1/applications - page: {}, size: {}, filters: [status={}, candidateId={}, jobId={}, companyId={}, time={} to {}]",
                pageable.getPageNumber(), pageable.getPageSize(), status, candidateId, jobId, companyId, startTime, endTime);
        
        Page<ApplicationResponse> applications = applicationServiceV1.getAllApplications(
                pageable, status, candidateId, jobId, companyId, startTime, endTime
        );
        
        ApiResponse<Page<ApplicationResponse>> response = ApiResponse.success(applications);
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v1/applications/{id} - Lấy đơn ứng tuyển theo ID (với IDOR Protection)
     * 
     * Path Variables:
     * - id: ID đơn ứng tuyển (Long)
     * 
     * Quyền truy cập: Người dùng đã xác thực (Quản trị viên/Nhà tuyển dụng/Ứng viên)
     * 
     * Security - IDOR Protection:
     * - Admin: Có thể xem tất cả applications
     * - Candidate: Chỉ xem applications của mình
     * - Employer: Chỉ xem applications cho jobs của mình
     * - Unauthorized access: HTTP 403 Forbidden
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADM', 'DN', 'UV')")
    @Operation(
        summary = "Get application by ID (IDOR Protected)",
        description = "Retrieve a single job application by its unique ID. " +
                     "Access control: Admin sees all, Candidate sees own, Employer sees applications to own jobs.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Application retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Access denied (IDOR attempt blocked)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Application not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        )
    })
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(
            @PathVariable @Parameter(description = "Application ID", required = true) Long id,
            Authentication authentication
    ) {
        String username = authentication.getName();
        log.info("GET /api/v1/applications/{} - User: {}", id, username);
        
        ApplicationResponse application = applicationServiceV1.getApplicationById(id, username);
        ApiResponse<ApplicationResponse> response = ApiResponse.success(application);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/v1/applications - Nộp đơn ứng tuyển
     * 
     * Request Body: ApplicationRequest
     * - jobId: ID tin tuyển dụng (Long, bắt buộc)
     * - cvId: ID CV sử dụng để ứng tuyển (Long, bắt buộc)
     * 
     * Business Validations (RBNT Rules):
     * 1. Tin tuyển dụng phải ở trạng thái ACTIVE
     * 2. Ngày hiện tại nằm trong khoảng StartDate-EndDate của tin tuyển dụng
     * 3. CV thuộc về ứng viên
     * 4. CV ở trạng thái ACTIVE (quy tắc RBCV)
     * 5. Ứng viên chưa từng ứng tuyển vào tin này
     * 
     * Quyền truy cập: Chỉ ứng viên (role: UV)
     * HTTP Status: 201 Created
     */
    @PostMapping
    @PreAuthorize("hasRole('UV')")
    @Operation(
        summary = "Submit job application",
        description = "Submit a new job application as a candidate. Validates business rules: " +
                     "job must be active, within posting period, CV must belong to candidate, " +
                     "CV must be active, and no duplicate applications.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Application submitted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation failed (job inactive, expired, CV issues, duplicate)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Only candidates can submit applications"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Job or CV not found"
        )
    })
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyToJob(
            @Valid @RequestBody @Parameter(description = "Application request with jobId and cvId", required = true) 
            ApplicationRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        log.info("POST /api/v1/applications - User: {}, JobId: {}, CvId: {}", 
                username, request.getJobId(), request.getCvId());
        
        ApplicationResponse application = applicationServiceV1.applyToJob(request, username);
        ApiResponse<ApplicationResponse> response = ApiResponse.success("Application submitted successfully", application);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    
    /**
     * PATCH /api/v1/applications/{id}/status - Cập nhật trạng thái đơn ứng tuyển
     * 
     * Path Variables:
     * - id: ID đơn ứng tuyển (Long)
     * 
     * Request Parameters:
     * - status: Trạng thái mới (ApplicationStatus: PENDING, APPROVED, REJECTED)
     * 
     * Quyền truy cập: Chỉ nhà tuyển dụng (role: NTD)
     * Validation: Nhà tuyển dụng phải sở hữu tin tuyển dụng liên quan đến đơn ứng tuyển
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('DN')")
    @Operation(
        summary = "Update application status",
        description = "Update the status of a job application (PENDING → APPROVED/REJECTED). " +
                     "Only the employer who posted the job can update the application status.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Application status updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad Request - Employer does not own the job"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Only employers can update application status"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Application not found"
        )
    })
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationStatus(
            @PathVariable @Parameter(description = "Application ID", required = true) Long id,
            @RequestParam @Parameter(description = "New application status", required = true) ApplicationStatus status,
            Authentication authentication
    ) {
        String username = authentication.getName();
        log.info("PATCH /api/v1/applications/{}/status - User: {}, NewStatus: {}", id, username, status);
        
        ApplicationResponse application = applicationServiceV1.updateApplicationStatus(id, status, username);
        ApiResponse<ApplicationResponse> response = ApiResponse.success("Application status updated to " + status, application);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/v1/applications/{id} - Rút đơn ứng tuyển
     * 
     * Path Variables:
     * - id: ID đơn ứng tuyển (Long)
     * 
     * Quyền truy cập: Chỉ ứng viên (role: UV)
     * Validation: Ứng viên phải sở hữu đơn ứng tuyển
     * HTTP Status: 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('UV')")
    @Operation(
        summary = "Withdraw application",
        description = "Withdraw (delete) a job application. Only the candidate who submitted " +
                     "the application can withdraw it.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "Application withdrawn successfully (no content)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad Request - Candidate does not own the application"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Only candidates can withdraw applications"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Application not found"
        )
    })
    public ResponseEntity<Void> withdrawApplication(
            @PathVariable @Parameter(description = "Application ID", required = true) Long id,
            Authentication authentication
    ) {
        String username = authentication.getName();
        log.info("DELETE /api/v1/applications/{} - User: {}", id, username);
        
        applicationServiceV1.withdrawApplication(id, username);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * GET /api/v1/applications/me - Lấy danh sách đơn ứng tuyển của tôi (Ứng viên)
     * 
     * Query Parameters:
     * - page: Số trang (0-indexed, default: 0)
     * - size: Kích thước trang (default: 10)
     * - sort: Tiêu chí sắp xếp (ví dụ: "applyTime,desc")
     * 
     * Quyền truy cập: Chỉ ứng viên (role: UV)
     * Trả về: Danh sách phân trang các đơn ứng tuyển do ứng viên xác thực gửi
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('UV')")
    @Operation(
        summary = "Get my applications",
        description = "Retrieve a paginated list of job applications submitted by the authenticated candidate.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Applications retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Only candidates can access this endpoint"
        )
    })
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getMyApplications(
            @Parameter(description = "Pagination and sorting parameters. Valid sort fields: 'applyTime', 'appStatus', 'appId'. Example: 'applyTime,desc'",
                      schema = @Schema(type = "string", example = "applyTime,desc"))
            @PageableDefault(size = 10, sort = "applyTime") 
            Pageable pageable,
            Authentication authentication
    ) {
        String username = authentication.getName();
        log.info("GET /api/v1/applications/me - User: {}, page: {}, size: {}", 
                username, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<ApplicationResponse> applications = applicationServiceV1.getMyApplications(pageable, username);
        ApiResponse<Page<ApplicationResponse>> response = ApiResponse.success(applications);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v1/applications/company - Lấy danh sách đơn ứng tuyển cho các tin tuyển dụng của tôi (Nhà tuyển dụng)
     * 
     * Query Parameters:
     * - jobId: Lọc theo tin tuyển dụng cụ thể (tùy chọn)
     * - status: Lọc theo trạng thái đơn ứng tuyển (tùy chọn)
     * - page: Số trang (0-indexed, default: 0)
     * - size: Kích thước trang (default: 10)
     * - sort: Tiêu chí sắp xếp (ví dụ: "applyTime,desc")
     * 
     * Quyền truy cập: Chỉ nhà tuyển dụng (role: NTD)
     * Trả về: Danh sách phân trang các đơn ứng tuyển nhận được cho các tin tuyển dụng của nhà tuyển dụng
     */
    @GetMapping("/company")
    @PreAuthorize("hasRole('DN')")
    @Operation(
        summary = "Get applications for my jobs",
        description = "Retrieve a paginated list of job applications received for the authenticated employer's job postings. " +
                     "Supports optional filtering by specific job and application status.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Applications retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Only employers can access this endpoint"
        )
    })
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getApplicationsForMyJobs(
            @Parameter(description = "Pagination and sorting parameters. Valid sort fields: 'applyTime', 'appStatus', 'appId'. Example: 'applyTime,desc'",
                      schema = @Schema(type = "string", example = "applyTime,desc"))
            @PageableDefault(size = 10, sort = "applyTime") 
            Pageable pageable,
            
            @RequestParam(required = false) 
            @Parameter(description = "Filter by specific job ID (optional)")
            Long jobId,
            
            @RequestParam(required = false) 
            @Parameter(description = "Filter by application status (optional)")
            ApplicationStatus status,
            
            Authentication authentication
    ) {
        String username = authentication.getName();
        log.info("GET /api/v1/applications/company - User: {}, page: {}, size: {}, jobId: {}, status: {}", 
                username, pageable.getPageNumber(), pageable.getPageSize(), jobId, status);
        
        Page<ApplicationResponse> applications = applicationServiceV1.getApplicationsForMyJobs(
                pageable, username, jobId, status
        );
        ApiResponse<Page<ApplicationResponse>> response = ApiResponse.success(applications);
        
        return ResponseEntity.ok(response);
    }
}
