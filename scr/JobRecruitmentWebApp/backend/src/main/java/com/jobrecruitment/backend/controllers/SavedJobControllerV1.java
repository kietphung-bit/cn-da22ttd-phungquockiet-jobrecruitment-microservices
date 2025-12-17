package com.jobrecruitment.backend.controllers;

import com.jobrecruitment.backend.dtos.request.SaveJobRequest;
import com.jobrecruitment.backend.dtos.response.ApiResponse;
import com.jobrecruitment.backend.dtos.response.SavedJobResponse;
import com.jobrecruitment.backend.services.SavedJobServiceV1;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * SavedJobControllerV1 - Controller REST API cho Quản lý Job Đã lưu (Version 1).
 * 
 * Base URL: /api/v1/saved-jobs
 * 
 * Các endpoint:
 * 1. POST /                - Lưu/bookmark job (Candidate only)
 * 2. GET /me               - Xem danh sách job đã lưu (Candidate only, paginated)
 * 3. DELETE /{jobId}       - Bỏ lưu/unsave job (Candidate only)
 * 
 * Business Rules (RBSL):
 * - RBSL: Ứng viên có thể lưu/bookmark job để xem sau
 * - RBSL: Ngăn chặn duplicate saves (validation error nếu đã lưu)
 * - Pagination: Hỗ trợ phân trang cho danh sách job đã lưu
 * - Ownership: Ứng viên chỉ quản lý saved jobs của chính mình
 * 
 * Security:
 * - JWT Bearer Authentication cho tất cả endpoints
 * - @PreAuthorize("hasRole('UV')") cho tất cả endpoints (Candidate-only)
 * 
 * @author JobRecruitment Development Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/v1/saved-jobs")
@RequiredArgsConstructor
@Tag(name = "Saved Job Management V1", description = "RESTful API for saved job management")
@SecurityRequirement(name = "bearerAuth")
public class SavedJobControllerV1 {

    private final SavedJobServiceV1 savedJobServiceV1;

    /**
     * Lưu/bookmark job để xem sau.
     * 
     * Candidate-only endpoint - lưu job để xem sau này.
     * 
     * HTTP Method: POST
     * URL: /api/v1/saved-jobs
     * Authentication: JWT Bearer Token
     * Authorization: @PreAuthorize("hasRole('UV')")
     * 
     * Business Rule (RBSL): Ngăn chặn duplicate saves
     * 
     * @param request SaveJobRequest chứa jobId cần lưu (@Valid để validate)
     * @param userDetails Thông tin user đang đăng nhập (auto-inject bởi Spring Security)
     * @return ResponseEntity chứa ApiResponse<SavedJobResponse> với thông tin job vừa lưu
     */
    @PostMapping
    @PreAuthorize("hasRole('UV')")
    @Operation(
            summary = "Save a job",
            description = "Candidate-only endpoint. Save/bookmark a job for later viewing. " +
                    "Returns validation error if job is already saved."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Job saved successfully",
                    content = @Content(schema = @Schema(implementation = SavedJobResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Job already saved or validation failed",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Candidate role required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<SavedJobResponse>> saveJob(
            @Parameter(description = "Save job request")
            @Valid @RequestBody SaveJobRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SavedJobResponse savedJobResponse = savedJobServiceV1.saveJob(request, userDetails.getUsername());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<SavedJobResponse>builder()
                        .status(201)
                        .message("Job saved successfully")
                        .data(savedJobResponse)
                        .build()
        );
    }

    /**
     * Lấy danh sách các job đã lưu với phân trang.
     * 
     * Candidate-only endpoint - trả về danh sách job đã lưu có phân trang.
     * 
     * HTTP Method: GET
     * URL: /api/v1/saved-jobs/me
     * Authentication: JWT Bearer Token
     * Authorization: @PreAuthorize("hasRole('UV')")
     * 
     * Query Parameters:
     * - page: Số trang (0-indexed, default: 0)
     * - size: Kích thước trang (default: 10)
     * - sort: Thứ tự sắp xếp (ví dụ: "savedDate,desc")
     * 
     * @param userDetails Thông tin user đang đăng nhập (auto-inject bởi Spring Security)
     * @param pageable Đối tượng phân trang (page, size, sort)
     * @return ResponseEntity chứa ApiResponse<Page<SavedJobResponse>> với danh sách job đã lưu
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('UV')")
    @Operation(
            summary = "Get my saved jobs",
            description = "Candidate-only endpoint. Returns paginated list of saved jobs. " +
                    "\n\nPagination Parameters:" +
                    "\n- page: Page number (0-indexed)" +
                    "\n- size: Page size (default: 10)" +
                    "\n- sort: Sort by field (e.g., 'savedTime,desc')"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Saved jobs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Candidate role required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Candidate profile not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<Page<SavedJobResponse>>> getMySavedJobs(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Pagination and sorting parameters. Valid sort fields: 'savedDate', 'jobId'. Example: 'savedDate,desc'",
                      schema = @Schema(type = "string", example = "savedDate,desc"))
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<SavedJobResponse> savedJobs = savedJobServiceV1.getMySavedJobs(userDetails.getUsername(), pageable);
        
        return ResponseEntity.ok(
                ApiResponse.<Page<SavedJobResponse>>builder()
                        .status(200)
                        .message("Saved jobs retrieved successfully")
                        .data(savedJobs)
                        .build()
        );
    }

    /**
     * Bỏ lưu/unsave job.
     * 
     * Candidate-only endpoint - xóa job khỏi danh sách đã lưu.
     * 
     * HTTP Method: DELETE
     * URL: /api/v1/saved-jobs/{jobId}
     * Authentication: JWT Bearer Token
     * Authorization: @PreAuthorize("hasRole('UV')")
     * 
     * Lưu ý: Đây là hard delete - xóa hoàn toàn khỏi database
     * 
     * @param jobId ID của job cần bỏ lưu (path variable)
     * @param userDetails Thông tin user đang đăng nhập (auto-inject bởi Spring Security)
     * @return ResponseEntity chứa ApiResponse<Void> với thông báo thành công
     */
    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('UV')")
    @Operation(
            summary = "Unsave a job",
            description = "Candidate-only endpoint. Remove job from saved list."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job unsaved successfully",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Candidate role required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Saved job not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<Void>> unsaveJob(
            @Parameter(description = "Job ID to unsave", example = "1")
            @PathVariable Long jobId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        savedJobServiceV1.unsaveJob(jobId, userDetails.getUsername());
        
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(200)
                        .message("Job unsaved successfully")
                        .data(null)
                        .build()
        );
    }
}
