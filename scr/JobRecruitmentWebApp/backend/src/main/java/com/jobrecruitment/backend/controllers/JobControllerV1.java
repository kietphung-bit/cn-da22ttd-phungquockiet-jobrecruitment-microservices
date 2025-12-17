package com.jobrecruitment.backend.controllers;

import com.jobrecruitment.backend.dtos.request.JobRequest;
import com.jobrecruitment.backend.dtos.response.ApiResponse;
import com.jobrecruitment.backend.dtos.response.JobResponse;
import com.jobrecruitment.backend.enums.JobStatus;
import com.jobrecruitment.backend.services.JobServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * JobController - RESTful API v1 cho quản lý tin tuyển dụng
 * 
 * Tuân thủ nghiêm ngặt các tiêu chuẩn RESTful:
 * - URL dựa trên tài nguyên (danh từ, không phải động từ)
 * - Phương thức HTTP phù hợp (GET, POST, PUT, PATCH, DELETE)
 * - Mã trạng thái chính xác (200 OK, 201 Created, 204 No Content)
 * - Phân trang sử dụng Spring Data Pageable
 * - Lọc động với JPA Specifications
 * 
 * Endpoints gốc: /api/v1/jobs
 */
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Management", description = "RESTful API for Job CRUD and Search Operations")
public class JobControllerV1 {

    private final JobServiceV1 jobService;

    // ==================== PUBLIC ENDPOINTS ====================

    /**
     * GET /api/v1/jobs
     * Liệt kê tất cả tin tuyển dụng với phân trang và lọc
     * 
     * Query Parameters:
     * - page: Số trang (bắt đầu từ 0)
     * - size: Kích thước trang (mặc định: 20)
     * - sort: Tiêu chí sắp xếp (ví dụ: jobSalary,desc hoặc createdDate,asc)
     * - jobTitle: Lọc theo tiêu đề công việc (khớp một phần)
     * - jobStatus: Lọc theo trạng thái công việc
     * - jobLocation: Lọc theo địa điểm (khớp một phần)
     * - companyId: Lọc theo ID công ty
     * - jcId: Lọc theo ID danh mục công việc
     * - minSalary: Lọc theo mức lương tối thiểu
     * - maxSalary: Lọc theo mức lương tối đa
     * 
     * Ví dụ: /api/v1/jobs?page=0&size=10&jobTitle=Java&jobStatus=ACTIVE&sort=jobSalary,desc
     */
    @GetMapping
    @Operation(
            summary = "List All Jobs (Paginated & Filtered)",
            description = "Retrieve paginated list of jobs with optional filters. " +
                    "Supports filtering by title, status, location, company, category, and salary range. " +
                    "Public endpoint - no authentication required."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Jobs retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getAllJobs(
            @Parameter(description = "Pagination and sorting parameters. Example sort values: 'jobId', 'jobTitle', 'jobSalary', 'createdDate'. Format: 'property,direction' (e.g., 'jobSalary,desc')",
                      schema = @Schema(type = "string", example = "jobId,asc"))
            @PageableDefault(size = 20, sort = "jobId") Pageable pageable,
            @Parameter(description = "Filter by job title (partial match)") 
            @RequestParam(required = false) String jobTitle,
            @Parameter(description = "Filter by job status") 
            @RequestParam(required = false) JobStatus jobStatus,
            @Parameter(description = "Filter by job location (partial match)") 
            @RequestParam(required = false) String jobLocation,
            @Parameter(description = "Filter by company ID") 
            @RequestParam(required = false) Long companyId,
            @Parameter(description = "Filter by category ID") 
            @RequestParam(required = false) Integer jcId,
            @Parameter(description = "Minimum salary") 
            @RequestParam(required = false) Double minSalary,
            @Parameter(description = "Maximum salary") 
            @RequestParam(required = false) Double maxSalary
    ) {
        Page<JobResponse> jobs = jobService.getAllJobs(
                pageable, jobTitle, jobStatus, jobLocation, companyId, jcId, minSalary, maxSalary
        );
        
        return ResponseEntity.ok(
                ApiResponse.<Page<JobResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Jobs retrieved successfully")
                        .data(jobs)
                        .build()
        );
    }

    /**
     * GET /api/v1/jobs/{jobId}
     * Lấy thông tin chi tiết của một tin tuyển dụng theo ID
     */
    @GetMapping("/{jobId}")
    @Operation(
            summary = "Get Job by ID",
            description = "Retrieve detailed information about a specific job. Public endpoint."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable Long jobId) {
        JobResponse job = jobService.getJobById(jobId);
        
        return ResponseEntity.ok(
                ApiResponse.<JobResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Job retrieved successfully")
                        .data(job)
                        .build()
        );
    }

    // ==================== EMPLOYER ENDPOINTS ====================

    /**
     * POST /api/v1/jobs
     * Tạo tin tuyển dụng mới (Chỉ dành cho Nhà tuyển dụng)
     * 
     * Trả về: 201 Created với Location header
     */
    @PostMapping
    @PreAuthorize("hasRole('DN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create Job Posting",
            description = "Create a new job posting. Employer only. " +
                    "Validates StartDate <= EndDate (RBNT). " +
                    "Generates unique JobCode (VL + 8 digits). " +
                    "Status: WAIT if startDate > today, otherwise PENDING."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Job created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error (StartDate > EndDate, invalid salary, etc.)",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only Employers (DN) can create jobs",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Company profile or Job category not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @Valid @RequestBody JobRequest request,
            Authentication authentication
    ) {
        JobResponse job = jobService.createJob(request, authentication.getName());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<JobResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Job created successfully")
                        .data(job)
                        .build()
        );
    }

    /**
     * PUT /api/v1/jobs/{jobId}
     * Cập nhật tin tuyển dụng (Cập nhật toàn bộ - Chỉ dành cho Nhà tuyển dụng)
     */
    @PutMapping("/{jobId}")
    @PreAuthorize("hasRole('DN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update Job Posting",
            description = "Full update of existing job posting. Employer can only update own jobs. " +
                    "Validates ownership and StartDate <= EndDate."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Can only modify own jobs",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable Long jobId,
            @Valid @RequestBody JobRequest request,
            Authentication authentication
    ) {
        JobResponse job = jobService.updateJob(jobId, request, authentication.getName());
        
        return ResponseEntity.ok(
                ApiResponse.<JobResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Job updated successfully")
                        .data(job)
                        .build()
        );
    }

    /**
     * PATCH /api/v1/jobs/{jobId}/status
     * Cập nhật trạng thái tin tuyển dụng (Chỉ dành cho Nhà tuyển dụng)
     */
    @PatchMapping("/{jobId}/status")
    @PreAuthorize("hasRole('DN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update Job Status",
            description = "Change job status (ACTIVE, CLOSED, HIDDEN). Employer can only modify own jobs."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job status updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Can only modify own jobs",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<JobResponse>> updateJobStatus(
            @PathVariable Long jobId,
            @Parameter(description = "New job status") @RequestParam JobStatus status,
            Authentication authentication
    ) {
        JobResponse job = jobService.updateJobStatus(jobId, status, authentication.getName());
        
        return ResponseEntity.ok(
                ApiResponse.<JobResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Job status updated successfully")
                        .data(job)
                        .build()
        );
    }

    /**
     * DELETE /api/v1/jobs/{jobId}
     * Xóa mềm tin tuyển dụng (Chỉ dành cho Nhà tuyển dụng)
     * 
     * Trả về: 204 No Content
     */
    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('DN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete Job",
            description = "Soft delete job by changing status to HIDDEN. Employer can only delete own jobs."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Job deleted successfully (No Content)",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Can only delete own jobs",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long jobId,
            Authentication authentication
    ) {
        jobService.deleteJob(jobId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/jobs/me
     * Lấy các tin tuyển dụng do nhà tuyển dụng đã xác thực đăng
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('DN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get My Posted Jobs (Paginated)",
            description = "Retrieve all jobs posted by the authenticated employer with pagination support."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Jobs retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only Employers can access",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getMyJobs(
            @PageableDefault(size = 20, sort = "jobId") Pageable pageable,
            Authentication authentication
    ) {
        Page<JobResponse> jobs = jobService.getMyJobs(pageable, authentication.getName());
        
        return ResponseEntity.ok(
                ApiResponse.<Page<JobResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Your jobs retrieved successfully")
                        .data(jobs)
                        .build()
        );
    }

    // ==================== NESTED RESOURCE ENDPOINTS ====================

    /**
     * GET /api/v1/companies/{companyId}/jobs
     * Lấy các tin tuyển dụng do công ty cụ thể đăng (Tài nguyên lồng nhau)
     * 
     * Note: Endpoint này được triển khai trong CompanyController như tài nguyên lồng nhau
     * URL: /api/v1/companies/{companyId}/jobs
     */

    /**
     * GET /api/v1/categories/{jcId}/jobs
     * Lấy các tin tuyển dụng trong danh mục cụ thể (Tài nguyên lồng nhau)
     * 
     * Note: Endpoint này được triển khai trong JobCategoryController như tài nguyên lồng nhau
     * URL: /api/v1/categories/{jcId}/jobs
     */
}
