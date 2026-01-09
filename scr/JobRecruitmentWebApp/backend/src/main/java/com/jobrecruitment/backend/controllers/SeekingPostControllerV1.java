package com.jobrecruitment.backend.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.jobrecruitment.backend.dtos.request.JobSeekPostRequest;
import com.jobrecruitment.backend.dtos.response.ApiResponse;
import com.jobrecruitment.backend.dtos.response.JobSeekPostResponse;
import com.jobrecruitment.backend.enums.SeekingPostStatus;
import com.jobrecruitment.backend.services.SeekingPostService;

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

/**
 * SeekingPostControllerV1 - Controller REST API cho Tin tìm việc (Version 1)
 * 
 * Base URL: /api/v1/seeking-posts
 * 
 * Các endpoint:
 * 1. POST /                      - Ứng viên tạo tin đăng (ROLE_UV)
 * 2. PUT /{id}                   - Ứng viên cập nhật tin đăng (Owner only)
 * 3. PATCH /{id}/status         - Ứng viên thay đổi trạng thái (Owner only)
 * 4. GET /                       - Public/Employer tìm kiếm tin đăng (với privacy logic)
 * 5. GET /{id}                   - Xem chi tiết một tin đăng (với privacy logic)
 * 6. GET /me                     - Ứng viên xem tin đăng của mình (ROLE_UV)
 * 7. DELETE /{id}                - Admin xóa tin đăng vi phạm (ROLE_ADM)
 * 
 * Business Rules:
 * - Tin đăng mới tự động có status=ACTIVE (Section 4.2)
 * - Một ứng viên chỉ có 1 tin ACTIVE tại một thời điểm
 * 
 * Privacy Logic (Section 4.2):
 * - Guest/Candidate: Xem masked data (name="Nguyễn Văn ***", no contact)
 * - Employer (DN): Xem full data (name, phone, email)
 * 
 * Security:
 * - JWT Bearer Authentication (except public search/detail)
 * - Role-based access control (@PreAuthorize)
 * 
 * @author JobRecruitment Development Team
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/api/v1/seeking-posts")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Job Seeking Posts Management V1", 
    description = "RESTful API for Job Seeking Posts (Reverse Recruitment). " +
                 "Candidates can post job seeking profiles, employers can search for talents."
)
public class SeekingPostControllerV1 {

    private final SeekingPostService seekingPostService;

    // ==================== CREATE JOB SEEKING POST ====================

    /**
     * POST /api/v1/seeking-posts
     * Ứng viên tạo tin đăng tìm việc mới
     * 
     * HTTP Method: POST
     * URL: /api/v1/seeking-posts
     * Content-Type: application/json
     * Authentication: JWT Bearer Token
     * Authorization: @PreAuthorize("hasRole('UV')")
     * 
     * Business Rules:
     * - Tin đăng mới tự động có status=ACTIVE
     * - Nếu ứng viên đã có tin ACTIVE, tin cũ tự động chuyển sang HIDDEN
     * 
     * @param request Dữ liệu tin đăng (validated)
     * @param userDetails Thông tin user đang đăng nhập (auto-inject)
     * @return ResponseEntity chứa ApiResponse<JobSeekPostResponse>
     */
    @PostMapping
    @PreAuthorize("hasRole('UV')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Tạo tin đăng tìm việc mới",
        description = "Candidate-only endpoint. Tạo tin đăng tìm việc với status=ACTIVE. " +
                     "Nếu đã có tin ACTIVE, tin cũ tự động chuyển sang HIDDEN."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Tin đăng được tạo thành công",
            content = @Content(schema = @Schema(implementation = JobSeekPostResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Dữ liệu không hợp lệ",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Chưa đăng nhập",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Không có quyền (yêu cầu ROLE_UV)",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy hồ sơ ứng viên",
            content = @Content
        )
    })
    public ResponseEntity<ApiResponse<JobSeekPostResponse>> createPost(
        @Valid @RequestBody JobSeekPostRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("POST /api/v1/seeking-posts - Create seeking post by user: {}", userDetails.getUsername());
        
        JobSeekPostResponse response = seekingPostService.createPost(userDetails.getUsername(), request);
        
        ApiResponse<JobSeekPostResponse> apiResponse = ApiResponse.<JobSeekPostResponse>builder()
            .status(HttpStatus.CREATED.value())
            .message("Tạo tin đăng tìm việc thành công")
            .data(response)
            .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    // ==================== UPDATE JOB SEEKING POST ====================

    /**
     * PUT /api/v1/seeking-posts/{id}
     * Ứng viên cập nhật tin đăng của mình
     * 
     * HTTP Method: PUT
     * URL: /api/v1/seeking-posts/{id}
     * Content-Type: application/json
     * Authentication: JWT Bearer Token
     * Authorization: Owner only (verified in service)
     * 
     * @param id ID tin đăng
     * @param request Dữ liệu cập nhật (validated)
     * @param userDetails Thông tin user đang đăng nhập (auto-inject)
     * @return ResponseEntity chứa ApiResponse<JobSeekPostResponse>
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('UV')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Cập nhật tin đăng tìm việc",
        description = "Candidate-only endpoint. Chỉ owner mới có thể cập nhật. " +
                     "Không thay đổi status (dùng PATCH /{id}/status)."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Cập nhật thành công",
            content = @Content(schema = @Schema(implementation = JobSeekPostResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Dữ liệu không hợp lệ",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Không có quyền (không phải owner)",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy tin đăng",
            content = @Content
        )
    })
    public ResponseEntity<ApiResponse<JobSeekPostResponse>> updatePost(
        @PathVariable Long id,
        @Valid @RequestBody JobSeekPostRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("PUT /api/v1/seeking-posts/{} - Update seeking post by user: {}", id, userDetails.getUsername());
        
        JobSeekPostResponse response = seekingPostService.updatePost(id, userDetails.getUsername(), request);
        
        ApiResponse<JobSeekPostResponse> apiResponse = ApiResponse.<JobSeekPostResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Cập nhật tin đăng thành công")
            .data(response)
            .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    // ==================== CHANGE STATUS ====================

    /**
     * PATCH /api/v1/seeking-posts/{id}/status
     * Ứng viên thay đổi trạng thái tin đăng
     * 
     * HTTP Method: PATCH
     * URL: /api/v1/seeking-posts/{id}/status?status=ACTIVE
     * Authentication: JWT Bearer Token
     * Authorization: Owner only (verified in service)
     * 
     * @param id ID tin đăng
     * @param status Trạng thái mới (ACTIVE, HIDDEN, CLOSED)
     * @param userDetails Thông tin user đang đăng nhập (auto-inject)
     * @return ResponseEntity chứa ApiResponse<JobSeekPostResponse>
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('UV')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Thay đổi trạng thái tin đăng",
        description = "Candidate-only endpoint. Chỉ owner mới có thể thay đổi trạng thái. " +
                     "Nếu chuyển sang ACTIVE khi đã có tin ACTIVE khác, tin khác sẽ chuyển sang HIDDEN."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Thay đổi trạng thái thành công",
            content = @Content(schema = @Schema(implementation = JobSeekPostResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Trạng thái không hợp lệ",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Không có quyền (không phải owner)",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy tin đăng",
            content = @Content
        )
    })
    public ResponseEntity<ApiResponse<JobSeekPostResponse>> changeStatus(
        @PathVariable Long id,
        @RequestParam @Parameter(description = "Trạng thái mới: ACTIVE, HIDDEN, CLOSED") SeekingPostStatus status,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("PATCH /api/v1/seeking-posts/{}/status - Change status to {} by user: {}", 
                id, status, userDetails.getUsername());
        
        JobSeekPostResponse response = seekingPostService.changeStatus(id, userDetails.getUsername(), status);
        
        ApiResponse<JobSeekPostResponse> apiResponse = ApiResponse.<JobSeekPostResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Thay đổi trạng thái thành công")
            .data(response)
            .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    // ==================== PUBLIC SEARCH ====================

    /**
     * GET /api/v1/seeking-posts
     * Tìm kiếm tin đăng công khai (với filter)
     * 
     * HTTP Method: GET
     * URL: /api/v1/seeking-posts?location=HCM&skills=Java&page=0&size=10
     * Authentication: Optional (JWT Bearer Token)
     * Authorization: Public access
     * 
     * Privacy Logic:
     * - Guest (không đăng nhập): Xem masked data
     * - Candidate (ROLE_UV): Xem masked data
     * - Employer (ROLE_DN): Xem full data
     * 
     * @param location Địa điểm (nullable)
     * @param skills Kỹ năng (nullable)
     * @param pageable Phân trang (default page=0, size=10)
     * @param userDetails Thông tin user đang đăng nhập (nullable)
     * @return ResponseEntity chứa ApiResponse<Page<JobSeekPostResponse>>
     */
    @GetMapping
    @Operation(
        summary = "Tìm kiếm tin đăng tìm việc",
        description = "Public endpoint (không cần đăng nhập). Tìm kiếm tin đăng ACTIVE với filter. " +
                     "Privacy: Guest/Candidate xem masked data, Employer xem full data."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Tìm kiếm thành công",
            content = @Content(schema = @Schema(implementation = JobSeekPostResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Page<JobSeekPostResponse>>> searchPosts(
        @RequestParam(required = false) @Parameter(description = "Lọc theo địa điểm (LIKE)") String location,
        @RequestParam(required = false) @Parameter(description = "Lọc theo kỹ năng (LIKE)") String skills,
        @PageableDefault(size = 10) Pageable pageable,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        log.info("GET /api/v1/seeking-posts - Search seeking posts by user: {} (location: {}, skills: {})", 
                username, location, skills);
        
        Page<JobSeekPostResponse> response = seekingPostService.searchPosts(username, location, skills, pageable);
        
        ApiResponse<Page<JobSeekPostResponse>> apiResponse = ApiResponse.<Page<JobSeekPostResponse>>builder()
            .status(HttpStatus.OK.value())
            .message("Tìm kiếm tin đăng thành công")
            .data(response)
            .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    // ==================== GET POST DETAIL ====================

    /**
     * GET /api/v1/seeking-posts/{id}
     * Xem chi tiết một tin đăng
     * 
     * HTTP Method: GET
     * URL: /api/v1/seeking-posts/{id}
     * Authentication: Optional (JWT Bearer Token)
     * Authorization: Public access
     * 
     * Privacy Logic: Tương tự searchPosts
     * 
     * @param id ID tin đăng
     * @param userDetails Thông tin user đang đăng nhập (nullable)
     * @return ResponseEntity chứa ApiResponse<JobSeekPostResponse>
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Xem chi tiết tin đăng",
        description = "Public endpoint. Xem chi tiết một tin đăng. " +
                     "Privacy: Guest/Candidate xem masked data, Employer xem full data."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lấy thông tin thành công",
            content = @Content(schema = @Schema(implementation = JobSeekPostResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy tin đăng",
            content = @Content
        )
    })
    public ResponseEntity<ApiResponse<JobSeekPostResponse>> getPostById(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        log.info("GET /api/v1/seeking-posts/{} - Get post detail by user: {}", id, username);
        
        JobSeekPostResponse response = seekingPostService.getPostById(id, username);
        
        ApiResponse<JobSeekPostResponse> apiResponse = ApiResponse.<JobSeekPostResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Lấy thông tin tin đăng thành công")
            .data(response)
            .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    // ==================== GET MY POSTS ====================

    /**
     * GET /api/v1/seeking-posts/me
     * Ứng viên xem danh sách tin đăng của mình
     * 
     * HTTP Method: GET
     * URL: /api/v1/seeking-posts/me?page=0&size=10
     * Authentication: JWT Bearer Token
     * Authorization: @PreAuthorize("hasRole('UV')")
     * 
     * @param pageable Phân trang (default page=0, size=10)
     * @param userDetails Thông tin user đang đăng nhập (auto-inject)
     * @return ResponseEntity chứa ApiResponse<Page<JobSeekPostResponse>>
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('UV')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Xem tin đăng của tôi",
        description = "Candidate-only endpoint. Xem tất cả tin đăng của mình (bao gồm HIDDEN, CLOSED). " +
                     "Trả về full data (owner)."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lấy danh sách thành công",
            content = @Content(schema = @Schema(implementation = JobSeekPostResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Chưa đăng nhập",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Không có quyền (yêu cầu ROLE_UV)",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy hồ sơ ứng viên",
            content = @Content
        )
    })
    public ResponseEntity<ApiResponse<Page<JobSeekPostResponse>>> getMyPosts(
        @PageableDefault(size = 10) Pageable pageable,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("GET /api/v1/seeking-posts/me - Get my posts by user: {}", userDetails.getUsername());
        
        Page<JobSeekPostResponse> response = seekingPostService.getMyPosts(userDetails.getUsername(), pageable);
        
        ApiResponse<Page<JobSeekPostResponse>> apiResponse = ApiResponse.<Page<JobSeekPostResponse>>builder()
            .status(HttpStatus.OK.value())
            .message("Lấy danh sách tin đăng thành công")
            .data(response)
            .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    // ==================== DELETE POST (ADMIN) ====================

    /**
     * DELETE /api/v1/seeking-posts/{id}
     * Admin xóa tin đăng vi phạm
     * 
     * HTTP Method: DELETE
     * URL: /api/v1/seeking-posts/{id}
     * Authentication: JWT Bearer Token
     * Authorization: @PreAuthorize("hasRole('ADM')")
     * 
     * Business Rules:
     * - Chỉ Admin (ROLE_ADM) mới có quyền
     * - Xóa vĩnh viễn (hard delete)
     * 
     * @param id ID tin đăng
     * @return ResponseEntity chứa ApiResponse<Void>
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADM')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Admin xóa tin đăng vi phạm",
        description = "Admin-only endpoint. Xóa vĩnh viễn tin đăng vi phạm chính sách."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Xóa thành công",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Không có quyền (yêu cầu ROLE_ADM)",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy tin đăng",
            content = @Content
        )
    })
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        log.info("DELETE /api/v1/seeking-posts/{} - Delete seeking post by admin", id);
        
        seekingPostService.deletePost(id);
        
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
            .status(HttpStatus.OK.value())
            .message("Xóa tin đăng thành công")
            .data(null)
            .build();
        
        return ResponseEntity.ok(apiResponse);
    }
}
