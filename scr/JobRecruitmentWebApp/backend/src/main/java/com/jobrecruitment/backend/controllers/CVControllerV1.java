package com.jobrecruitment.backend.controllers;

import com.jobrecruitment.backend.dtos.response.ApiResponse;
import com.jobrecruitment.backend.dtos.response.CVResponse;
import com.jobrecruitment.backend.enums.CVStatus;
import com.jobrecruitment.backend.services.CVServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * CVControllerV1 - Controller REST API cho Quản lý CV (Version 1).
 * 
 * Base URL: /api/v1/cvs
 * 
 * Các endpoint:
 * 1. POST /                - Tải lên CV (Candidate only)
 * 2. GET /me               - Xem danh sách CV của mình (Candidate only)
 * 3. PATCH /{id}/status   - Cập nhật trạng thái CV (Candidate only)
 * 4. DELETE /{id}         - Xóa CV (Soft delete) (Candidate only)
 * 
 * Business Rules (RBCV):
 * - RBCV: CVStatus phải là ACTIVE hoặc HIDDEN
 * - CVCode: Auto-generated theo format "CV" + 8 chữ số (unique)
 * - Ownership: Ứng viên chỉ quản lý CV của chính mình
 * - Soft Delete: Xóa là soft delete (đặt CVStatus = HIDDEN, không xóa vĩnh viễn)
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
@RequestMapping("/api/v1/cvs")
@RequiredArgsConstructor
@Tag(name = "CV Management V1", description = "RESTful API for CV management")
@SecurityRequirement(name = "bearerAuth")
public class CVControllerV1 {

    private final CVServiceV1 cvServiceV1;

    /**
     * Tải lên CV mới.
     * 
     * Candidate-only endpoint - tạo mới bản ghi CV với CVCode tự động sinh.
     * 
     * HTTP Method: POST
     * URL: /api/v1/cvs
     * Content-Type: multipart/form-data
     * Authentication: JWT Bearer Token
     * Authorization: @PreAuthorize("hasRole('UV')")
     * 
     * CRITICAL: Frontend must send FormData with key 'file' matching @RequestParam("file")
     * 
     * @param file MultipartFile từ form upload (PDF/DOCX)
     * @param userDetails Thông tin user đang đăng nhập (auto-inject bởi Spring Security)
     * @return ResponseEntity chứa ApiResponse<CVResponse> với CVCode được tạo
     */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('UV')")
    @Operation(
            summary = "Upload CV",
            description = "Candidate-only endpoint. Upload a new CV file (PDF/DOCX) with auto-generated CVCode (CV + 8 digits)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "CV created successfully",
                    content = @Content(schema = @Schema(implementation = CVResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file or file too large",
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
                    description = "Candidate profile not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<CVResponse>> uploadCV(
            @Parameter(description = "CV file (PDF/DOCX, max 10MB)", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CVResponse cvResponse = cvServiceV1.createCV(file, userDetails.getUsername());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<CVResponse>builder()
                        .status(201)
                        .message("CV uploaded successfully")
                        .data(cvResponse)
                        .build()
        );
    }

    /**
     * Lấy danh sách tất cả CV của ứng viên đang đăng nhập.
     * 
     * Candidate-only endpoint - trả về danh sách tất cả CV thuộc về ứng viên.
     * 
     * HTTP Method: GET
     * URL: /api/v1/cvs/me
     * Authentication: JWT Bearer Token
     * Authorization: @PreAuthorize("hasRole('UV')")
     * 
     * @param userDetails Thông tin user đang đăng nhập (auto-inject bởi Spring Security)
     * @return ResponseEntity chứa ApiResponse<List<CVResponse>> với danh sách CV
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('UV')")
    @Operation(
            summary = "Get my CVs",
            description = "Candidate-only endpoint. Returns list of all CVs belonging to the authenticated candidate."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CVs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CVResponse.class))
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
    public ResponseEntity<ApiResponse<List<CVResponse>>> getMyCVs(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<CVResponse> cvs = cvServiceV1.getMyCVs(userDetails.getUsername());
        
        return ResponseEntity.ok(
                ApiResponse.<List<CVResponse>>builder()
                        .status(200)
                        .message("CVs retrieved successfully")
                        .data(cvs)
                        .build()
        );
    }

    /**
     * Cập nhật trạng thái CV (ACTIVE/HIDDEN).
     * 
     * Candidate-only endpoint - cập nhật trạng thái CV của chính mình.
     * 
     * HTTP Method: PATCH
     * URL: /api/v1/cvs/{cvId}/status
     * Authentication: JWT Bearer Token
     * Authorization: @PreAuthorize("hasRole('UV')")
     * 
     * Business Rule (RBCV): CVStatus chỉ có thể là ACTIVE hoặc HIDDEN
     * 
     * @param cvId ID của CV cần cập nhật (path variable)
     * @param newStatus Trạng thái mới (ACTIVE/HIDDEN) (request parameter)
     * @param userDetails Thông tin user đang đăng nhập (auto-inject bởi Spring Security)
     * @return ResponseEntity chứa ApiResponse<CVResponse> với trạng thái đã cập nhật
     */
    @PatchMapping("/{cvId}/status")
    @PreAuthorize("hasRole('UV')")
    @Operation(
            summary = "Update CV status",
            description = "Candidate-only endpoint. Update CV status (ACTIVE/HIDDEN). Can only update own CVs."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CV status updated successfully",
                    content = @Content(schema = @Schema(implementation = CVResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Can only update own CVs",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "CV not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<CVResponse>> updateCVStatus(
            @Parameter(description = "CV ID", example = "1")
            @PathVariable Long cvId,
            @Parameter(description = "New CV status", example = "ACTIVE")
            @RequestParam CVStatus newStatus,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CVResponse cvResponse = cvServiceV1.updateCVStatus(cvId, newStatus, userDetails.getUsername());
        
        return ResponseEntity.ok(
                ApiResponse.<CVResponse>builder()
                        .status(200)
                        .message("CV status updated successfully")
                        .data(cvResponse)
                        .build()
        );
    }

    /**
     * Xóa CV (Soft Delete).
     * 
     * Candidate-only endpoint - xóa CV bằng cách đặt trạng thái HIDDEN.
     * 
     * HTTP Method: DELETE
     * URL: /api/v1/cvs/{cvId}
     * Authentication: JWT Bearer Token
     * Authorization: @PreAuthorize("hasRole('UV')")
     * 
     * Lưu ý: Đây là Soft Delete - CV không bị xóa vĩnh viễn, chỉ ẩn đi
     * 
     * @param cvId ID của CV cần xóa (path variable)
     * @param userDetails Thông tin user đang đăng nhập (auto-inject bới Spring Security)
     * @return ResponseEntity chứa ApiResponse<Void> với thông báo thành công
     */
    @DeleteMapping("/{cvId}")
    @PreAuthorize("hasRole('UV')")
    @Operation(
            summary = "Delete CV (soft delete)",
            description = "Candidate-only endpoint. Soft delete CV by setting status to HIDDEN. Can only delete own CVs."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CV deleted successfully",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Can only delete own CVs",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "CV not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteCV(
            @Parameter(description = "CV ID", example = "1")
            @PathVariable Long cvId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        cvServiceV1.deleteCV(cvId, userDetails.getUsername());
        
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(200)
                        .message("CV deleted successfully")
                        .data(null)
                        .build()
        );
    }
}
