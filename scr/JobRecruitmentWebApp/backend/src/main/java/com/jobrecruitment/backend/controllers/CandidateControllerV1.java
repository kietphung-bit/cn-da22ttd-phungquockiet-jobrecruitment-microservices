package com.jobrecruitment.backend.controllers;

import com.jobrecruitment.backend.dtos.request.CandidateProfileRequest;
import com.jobrecruitment.backend.dtos.response.ApiResponse;
import com.jobrecruitment.backend.dtos.response.CandidateResponse;
import com.jobrecruitment.backend.services.CandidateServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * CandidateControllerV1 - Controller REST API cho Quản lý Hồ sơ Ứng viên (Version 1).
 * 
 * Base URL: /api/v1/candidates
 * 
 * Các endpoint:
 * 1. GET /api/v1/candidates/{id}  - Xem thông tin hồ sơ ứng viên (Public/Employer/Admin)
 * 2. GET /api/v1/candidates/me    - Lấy hồ sơ của mình (Candidate only)
 * 3. PUT /api/v1/candidates/me    - Cập nhật hồ sơ của mình (Candidate only)
 * 
 * Business Rules (RBHT, RBSDT, RBEML, RBNS):
 * - RBHT: Định dạng tên (chỉ chứa chữ cái và khoảng trắng)
 * - RBSDT: Số điện thoại (10-11 chữ số)
 * - RBEML: Định dạng email hợp lệ
 * - RBNS: Tuổi lao động (age >= 18, ngày sinh phải trong quá khứ)
 * 
 * Security:
 * - JWT Bearer Authentication cho tất cả endpoints
 * - @PreAuthorize("hasRole('UV')") cho các endpoint Candidate-only
 * 
 * @author JobRecruitment Development Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
@Tag(name = "Candidate Management V1", description = "RESTful API for candidate profile management")
@SecurityRequirement(name = "bearerAuth")
public class CandidateControllerV1 {

    private final CandidateServiceV1 candidateServiceV1;

    /**
     * Lấy thông tin hồ sơ ứng viên theo ID (với IDOR Protection).
     * 
     * Endpoint có authentication - cho phép employer và admin xem thông tin ứng viên
     * khi đánh giá hồ sơ ứng tuyển. Candidate chỉ có thể xem hồ sơ của chính mình.
     * 
     * HTTP Method: GET
     * URL: /api/v1/candidates/{candidateId}
     * Authentication: JWT Bearer Token (Required)
     * 
     * Security - Bảo vệ IDOR:
     * - Admin (ADM): Có thể xem tất cả ứng viên
     * - Employer (DN): Có thể xem tất cả ứng viên (cho tuyển dụng)
     * - Candidate (UV): Chỉ có thể xem hồ sơ của chính mình
     * - Unauthenticated: 401 Unauthorized
     * 
     * @param candidateId ID của ứng viên cần xem
     * @param userDetails Thông tin user đang đăng nhập (từ JWT, auto-inject)
     * @return ResponseEntity chứa ApiResponse<CandidateResponse> với thông tin hồ sơ
     */
    @GetMapping("/{candidateId}")
    @PreAuthorize("hasAnyRole('ADM', 'DN', 'UV')")
    @Operation(
            summary = "Get candidate profile by ID (IDOR Protected)",
            description = "Authenticated endpoint to view candidate profile details. " +
                    "Admin and Employers can view all candidates. " +
                    "Candidates can only view their own profile. " +
                    "\n\nAccess Control:" +
                    "\n- Admin (ADM): Full access to all candidates" +
                    "\n- Employer (DN): Can view all candidates (for recruitment evaluation)" +
                    "\n- Candidate (UV): Can only view own profile (IDOR protection)" +
                    "\n- Unauthenticated: 401 Unauthorized",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Candidate profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CandidateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Access denied (IDOR attempt blocked)",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Candidate not found",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<CandidateResponse>> getCandidateById(
            @Parameter(description = "Candidate ID", example = "1")
            @PathVariable Long candidateId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CandidateResponse candidateResponse = candidateServiceV1.getCandidateById(candidateId, userDetails.getUsername());
        
        return ResponseEntity.ok(
                ApiResponse.<CandidateResponse>builder()
                        .status(200)
                        .message("Candidate profile retrieved successfully")
                        .data(candidateResponse)
                        .build()
        );
    }

    /**
     * Lấy thông tin hồ sơ của ứng viên đang đăng nhập.
     * 
     * Candidate-only endpoint - chỉ ứng viên mới có thể xem hồ sơ của chính mình.
     * 
     * HTTP Method: GET
     * URL: /api/v1/candidates/me
     * Authentication: JWT Bearer Token
     * Authorization: @PreAuthorize("hasRole('UV')")
     * 
     * @param userDetails Thông tin user đang đăng nhập (từ JWT, auto-inject bởi Spring Security)
     * @return ResponseEntity chứa ApiResponse<CandidateResponse> với thông tin hồ sơ đầy đủ
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('UV')")
    @Operation(
            summary = "Get my candidate profile",
            description = "Candidate-only endpoint. Returns the profile of the authenticated candidate user."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CandidateResponse.class))
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
    public ResponseEntity<ApiResponse<CandidateResponse>> getMyProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CandidateResponse candidateResponse = candidateServiceV1.getMyProfile(userDetails.getUsername());
        
        return ResponseEntity.ok(
                ApiResponse.<CandidateResponse>builder()
                        .status(200)
                        .message("Candidate profile retrieved successfully")
                        .data(candidateResponse)
                        .build()
        );
    }

    /**
     * Cập nhật thông tin hồ sơ ứng viên.
     * 
     * Candidate-only endpoint - chỉ ứng viên mới có thể cập nhật hồ sơ của chính mình.
     * 
     * HTTP Method: PUT
     * URL: /api/v1/candidates/me
     * Authentication: JWT Bearer Token
     * Authorization: @PreAuthorize("hasRole('UV')")
     * 
     * Business Rules áp dụng:
     * - RBHT: Tên chỉ chứa chữ cái và khoảng trắng (không có số, ký tự đặc biệt)
     * - RBSDT: Số điện thoại phải là 10-11 chữ số
     * - RBEML: Email phải đúng định dạng (chứa @ và domain)
     * - RBNS: Ngày sinh phải trong quá khứ và age >= 18
     * 
     * Lưu ý: Chỉ cập nhật các field được cung cấp (partial update)
     * 
     * @param request CandidateProfileRequest chứa thông tin cần cập nhật (@Valid để validate)
     * @param userDetails Thông tin user đang đăng nhập (auto-inject bởi Spring Security)
     * @return ResponseEntity chứa ApiResponse<CandidateResponse> với thông tin đã cập nhật
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('UV')")
    @Operation(
            summary = "Update my candidate profile",
            description = "Candidate-only endpoint. Updates the profile of the authenticated candidate user. " +
                    "Only provided fields will be updated (partial update). " +
                    "\n\nBusiness Rules:" +
                    "\n- RBHT: Name must contain only letters and spaces" +
                    "\n- RBSDT: Phone number must be 10-11 digits" +
                    "\n- RBEML: Email must be valid format" +
                    "\n- RBNS: Birthdate must be in the past and age >= 18"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = CandidateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation failed",
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
    public ResponseEntity<ApiResponse<CandidateResponse>> updateMyProfile(
            @Parameter(description = "Candidate profile update request")
            @Valid @RequestBody CandidateProfileRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CandidateResponse candidateResponse = candidateServiceV1.updateProfile(request, userDetails.getUsername());
        
        return ResponseEntity.ok(
                ApiResponse.<CandidateResponse>builder()
                        .status(200)
                        .message("Candidate profile updated successfully")
                        .data(candidateResponse)
                        .build()
        );
    }
}
