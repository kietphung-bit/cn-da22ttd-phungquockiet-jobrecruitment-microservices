package com.jobrecruitment.backend.controllers;

import com.jobrecruitment.backend.dtos.request.CompanyProfileRequest;
import com.jobrecruitment.backend.dtos.response.ApiResponse;
import com.jobrecruitment.backend.dtos.response.CompanyResponse;
import com.jobrecruitment.backend.services.CompanyServiceV1;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * CompanyControllerV1 - Controller REST API cho Quản lý Doanh nghiệp (Version 1).
 * 
 * Base Path: /api/v1/companies
 * 
 * Tính năng chính:
 * - Danh sách doanh nghiệp công khai với phân trang và tìm kiếm
 * - Xem thông tin doanh nghiệp công khai
 * - Employer quản lý profile doanh nghiệp (cập nhật thông tin, upload logo)
 * - Thiết kế RESTful chuẩn với HTTP methods đúng
 * 
 * Các endpoint:
 * 1. GET    /api/v1/companies                - Danh sách doanh nghiệp (public, paginated)
 * 2. GET    /api/v1/companies/{id}           - Xem doanh nghiệp theo ID (public)
 * 3. GET    /api/v1/companies/me             - Lấy profile của mình (Employer only)
 * 4. PUT    /api/v1/companies/me             - Cập nhật profile (Employer only)
 * 5. PATCH  /api/v1/companies/me/logo        - Cập nhật logo (Employer only)
 * 
 * Security:
 * - Endpoints công khai (GET list, GET by ID): Không yêu cầu authentication
 * - Endpoints Employer-only: JWT Bearer Authentication + @PreAuthorize("hasRole('DN')")
 * 
 * @author JobRecruitment Development Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Company Management V1", description = "RESTful APIs for company profiles with pagination")
public class CompanyControllerV1 {
    
    private final CompanyServiceV1 companyServiceV1;
    
    /**
     * Lấy danh sách tất cả doanh nghiệp với phân trang và tìm kiếm.
     * 
     * Endpoint công khai - không yêu cầu authentication.
     * 
     * HTTP Method: GET
     * URL: /api/v1/companies
     * 
     * Query Parameters:
     * - name: Tìm kiếm theo tên doanh nghiệp (partial match, case-insensitive, optional)
     * - page: Số trang (0-indexed, default: 0)
     * - size: Kích thước trang (default: 10)
     * - sort: Thứ tự sắp xếp (ví dụ: "companyName,asc")
     * 
     * @param pageable Đối tượng phân trang (page, size, sort)
     * @param name Từ khóa tìm kiếm tên doanh nghiệp (optional)
     * @return ResponseEntity chứa ApiResponse<Page<CompanyResponse>>
     */
    @GetMapping
    @Operation(
        summary = "List all companies",
        description = "Retrieve a paginated list of companies with optional name filter. " +
                     "Public endpoint - no authentication required. " +
                     "Use 'name' parameter for searching companies by name."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Companies retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<ApiResponse<Page<CompanyResponse>>> getAllCompanies(
            @Parameter(description = "Pagination and sorting parameters. Valid sort fields: 'companyName', 'companyId'. Example: 'companyName,asc'",
                      schema = @Schema(type = "string", example = "companyName,asc"))
            @PageableDefault(size = 10, sort = "companyName") 
            Pageable pageable,
            
            @RequestParam(required = false) 
            @Parameter(description = "Filter by company name (partial match, case-insensitive)")
            String name
    ) {
        log.info("GET /api/v1/companies - page: {}, size: {}, name filter: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), name);
        
        Page<CompanyResponse> companies = companyServiceV1.getAllCompanies(pageable, name);
        ApiResponse<Page<CompanyResponse>> response = ApiResponse.success(companies);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy thông tin chi tiết doanh nghiệp theo ID.
     * 
     * Endpoint công khai - không yêu cầu authentication.
     * 
     * HTTP Method: GET
     * URL: /api/v1/companies/{id}
     * 
     * @param id ID của doanh nghiệp cần xem
     * @return ResponseEntity chứa ApiResponse<CompanyResponse> với thông tin chi tiết
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get company by ID",
        description = "Retrieve company profile details by company ID. " +
                     "Public endpoint - no authentication required. " +
                     "Returns company information including name, description, address, logo, and status."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Company retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Company not found"
        )
    })
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompanyById(
            @PathVariable @Parameter(description = "Company ID", required = true) Long id
    ) {
        log.info("GET /api/v1/companies/{}", id);
        
        CompanyResponse company = companyServiceV1.getCompanyById(id);
        ApiResponse<CompanyResponse> response = ApiResponse.success(company);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy thông tin profile doanh nghiệp của employer đang đăng nhập.
     * 
     * Employer-only endpoint - chỉ employer mới có thể xem profile doanh nghiệp của mình.
     * 
     * HTTP Method: GET
     * URL: /api/v1/companies/me
     * Authentication: JWT Bearer Token
     * Authorization: @PreAuthorize("hasRole('DN')")
     * 
     * @param authentication Thông tin authentication (auto-inject bởi Spring Security)
     * @return ResponseEntity chứa ApiResponse<CompanyResponse> với thông tin profile
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('DN')")
    @Operation(
        summary = "Get my company profile",
        description = "Retrieve the authenticated employer's company profile. " +
                     "Returns all company details for the logged-in employer.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Company profile retrieved successfully",
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
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Company profile not found for user"
        )
    })
    public ResponseEntity<ApiResponse<CompanyResponse>> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        log.info("GET /api/v1/companies/me - User: {}", username);
        
        CompanyResponse company = companyServiceV1.getMyProfile(username);
        ApiResponse<CompanyResponse> response = ApiResponse.success(company);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cập nhật thông tin profile doanh nghiệp.
     * 
     * Employer-only endpoint - chỉ employer mới có thể cập nhật profile doanh nghiệp của mình.
     * 
     * HTTP Method: PUT
     * URL: /api/v1/companies/me
     * Authentication: JWT Bearer Token
     * Authorization: @PreAuthorize("hasRole('DN')")
     * 
     * Request Body: CompanyProfileRequest
     * - companyName: Tên chính thức (RBHT validation: chỉ chữ cái và khoảng trắng)
     * - companyDescription: Mô tả chi tiết (optional)
     * - companyAddress: Địa chỉ đầy đủ (optional)
     * - companyWebsite: Website URL (optional)
     * - companyEmail: Email liên hệ (RBEML validation: định dạng email)
     * 
     * @param request CompanyProfileRequest chứa thông tin cần cập nhật (@Valid để validate)
     * @param authentication Thông tin authentication (auto-inject bởi Spring Security)
     * @return ResponseEntity chứa ApiResponse<CompanyResponse> với thông tin đã cập nhật
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('DN')")
    @Operation(
        summary = "Update my company profile",
        description = "Update the authenticated employer's company profile. " +
                     "All fields except logoURL can be updated. " +
                     "Use PATCH /api/v1/companies/me/logo to update logo.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Company profile updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation error (invalid name, email format)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Only employers can update their profile"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Company profile not found"
        )
    })
    public ResponseEntity<ApiResponse<CompanyResponse>> updateProfile(
            @Valid @RequestBody @Parameter(description = "Company profile update data", required = true) 
            CompanyProfileRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        log.info("PUT /api/v1/companies/me - User: {}, Company: {}", username, request.getCompanyName());
        
        CompanyResponse company = companyServiceV1.updateProfile(request, username);
        ApiResponse<CompanyResponse> response = ApiResponse.success("Company profile updated successfully", company);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cập nhật logo doanh nghiệp (File Upload).
     * 
     * Employer-only endpoint - chỉ employer mới có thể cập nhật logo doanh nghiệp của mình.
     * 
     * HTTP Method: PATCH
     * URL: /api/v1/companies/me/logo
     * Authentication: JWT Bearer Token
     * Authorization: @PreAuthorize("hasRole('DN')")
     * Content-Type: multipart/form-data
     * 
     * Request Parameters:
     * - file: Logo image file (MultipartFile, jpg/jpeg/png/gif)
     * 
     * Business Logic:
     * 1. Xác thực ownership: Chỉ employer được phép update logo công ty mình
     * 2. Validate file: Extension (jpg, jpeg, png, gif), size (max 10MB)
     * 3. Upload file vào thư mục uploads/logos/
     * 4. Xóa logo cũ nếu tồn tại
     * 5. Cập nhật logoURL trong database
     * 6. Trả về CompanyResponse với URL logo mới
     * 
     * Ví dụ URL truy cập logo:
     * - http://localhost:8080/uploads/logos/uuid-filename.png
     * 
     * @param file Logo image file (multipart/form-data)
     * @param authentication Thông tin authentication (auto-inject bởi Spring Security)
     * @return ResponseEntity chứa ApiResponse<CompanyResponse> với thông tin đã cập nhật logo
     */
    @PatchMapping(value = "/me/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DN')")
    @Operation(
        summary = "Upload company logo",
        description = "Upload a logo image file for the company. " +
                     "Accepts multipart/form-data with image file (jpg, jpeg, png, gif). " +
                     "Maximum file size: 10MB. " +
                     "Old logo will be deleted if exists.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Company logo uploaded successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid file format or size exceeds limit"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Only employers can update their logo"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Company profile not found"
        )
    })
    public ResponseEntity<ApiResponse<CompanyResponse>> uploadLogo(
            @RequestParam("file") @Parameter(description = "Logo image file (jpg, jpeg, png, gif)", required = true) 
            MultipartFile file,
            Authentication authentication
    ) {
        String username = authentication.getName();
        log.info("PATCH /api/v1/companies/me/logo - User: {}, File: {}, Size: {} bytes", 
                username, file.getOriginalFilename(), file.getSize());
        
        CompanyResponse company = companyServiceV1.uploadLogo(file, username);
        ApiResponse<CompanyResponse> response = ApiResponse.success("Company logo uploaded successfully", company);
        
        return ResponseEntity.ok(response);
    }
}
