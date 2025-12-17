package com.jobrecruitment.backend.controllers;

import com.jobrecruitment.backend.dtos.request.JobCategoryRequest;
import com.jobrecruitment.backend.dtos.response.ApiResponse;
import com.jobrecruitment.backend.dtos.response.JobCategoryResponse;
import com.jobrecruitment.backend.services.JobCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * JobCategoryControllerV1 - RESTful API Controller quản lý danh mục công việc
 * 
 * Base URL: /api/v1/categories
 * 
 * Danh sách endpoint:
 * 1. GET /         - Danh sách tất cả danh mục (Công khai)
 * 2. POST /        - Tạo danh mục mới (Chỉ Admin)
 * 3. PUT /{id}     - Cập nhật danh mục (Chỉ Admin)
 * 4. DELETE /{id}  - Xoá danh mục (Chỉ Admin)
 * 
 * Bảo mật:
 * - Endpoint công khai (GET): Không cần xác thực - Ai cũng xem được
 * - Endpoint Admin (POST/PUT/DELETE): Yêu cầu JWT + Vai trò ADM
 * 
 * Quy tắc nghiệp vụ:
 * - RBGTN: Mức lương cơ bản (JCBaseSalary) phải > 0
 * - JobCategory là Master Data: Cần quản lý cẩn thận bởi Admin
 * - Ứng viên và Nhà tuyển dụng có thể xem nhưng không thể sửa/xoá
 * 
 * Phụ thuộc:
 * - JobCategoryService: Xử lý business logic cho CRUD danh mục
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Job Category Management V1", description = "RESTful API for job category management")
public class JobCategoryControllerV1 {

    private final JobCategoryService jobCategoryService;

    /**
     * Lấy danh sách tất cả danh mục công việc
     * 
     * Endpoint công khai - Bất kỳ ai cũng có thể xem danh sách danh mục
     * 
     * Chức năng:
     * - Trả về tất cả danh mục công việc (IT, Marketing, Kế toán...)
     * - Không phân trang - Trả về toàn bộ danh sách
     * - Dùng cho dropdown/filter trong các form tìm kiếm công việc
     * 
     * HTTP Method: GET /api/v1/categories
     * 
     * Bảo mật: Không cần xác thực (Public)
     * 
     * @return ResponseEntity<ApiResponse<List<JobCategoryResponse>>> - Danh sách danh mục
     */
    @GetMapping
    @Operation(
            summary = "Get all job categories",
            description = "Public endpoint. Returns list of all job categories."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Categories retrieved successfully",
                    content = @Content(schema = @Schema(implementation = JobCategoryResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<JobCategoryResponse>>> getAllCategories() {
        List<JobCategoryResponse> categories = jobCategoryService.getAllCategories();
        
        return ResponseEntity.ok(
                ApiResponse.<List<JobCategoryResponse>>builder()
                        .status(200)
                        .message("Categories retrieved successfully")
                        .data(categories)
                        .build()
        );
    }

    /**
     * Tạo danh mục công việc mới
     * 
     * Endpoint dành cho Admin - Tạo mới danh mục công việc
     * 
     * Chức năng:
     * - Tạo danh mục mới với JCName, JCDescription, JCBaseSalary
     * - Validate @Valid: JCBaseSalary phải > 0 (RBGTN)
     * - Trả về HTTP 201 Created nếu thành công
     * 
     * HTTP Method: POST /api/v1/categories
     * Request Body: JobCategoryRequest (JSON)
     * 
     * Bảo mật: Yêu cầu JWT + Vai trò ADM
     * 
     * @param request JobCategoryRequest - Dữ liệu danh mục mới
     * @return ResponseEntity<ApiResponse<JobCategoryResponse>> - Danh mục vừa tạo (HTTP 201)
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADM')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create job category",
            description = "Admin-only endpoint. Create a new job category."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Category created successfully",
                    content = @Content(schema = @Schema(implementation = JobCategoryResponse.class))
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
                    description = "Forbidden - Admin role required",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<JobCategoryResponse>> createCategory(
            @Parameter(description = "Category data")
            @Valid @RequestBody JobCategoryRequest request
    ) {
        JobCategoryResponse categoryResponse = jobCategoryService.createCategory(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<JobCategoryResponse>builder()
                        .status(201)
                        .message("Category created successfully")
                        .data(categoryResponse)
                        .build()
        );
    }

    /**
     * Cập nhật danh mục công việc
     * 
     * Endpoint dành cho Admin - Cập nhật thông tin danh mục hiện có
     * 
     * Chức năng:
     * - Cập nhật toàn bộ (full update) thông tin danh mục
     * - Validate @Valid: JCBaseSalary phải > 0 (RBGTN)
     * - Trả về thông tin danh mục sau khi cập nhật
     * 
     * HTTP Method: PUT /api/v1/categories/{jcId}
     * Path Variable: jcId (Integer)
     * Request Body: JobCategoryRequest (JSON)
     * 
     * Bảo mật: Yêu cầu JWT + Vai trò ADM
     * 
     * @param jcId ID danh mục cần cập nhật
     * @param request JobCategoryRequest - Dữ liệu cập nhật
     * @return ResponseEntity<ApiResponse<JobCategoryResponse>> - Danh mục sau khi cập nhật
     */
    @PutMapping("/{jcId}")
    @PreAuthorize("hasAuthority('ROLE_ADM')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update job category",
            description = "Admin-only endpoint. Update an existing job category."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Category updated successfully",
                    content = @Content(schema = @Schema(implementation = JobCategoryResponse.class))
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
                    description = "Forbidden - Admin role required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<JobCategoryResponse>> updateCategory(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Integer jcId,
            @Parameter(description = "Updated category data")
            @Valid @RequestBody JobCategoryRequest request
    ) {
        JobCategoryResponse categoryResponse = jobCategoryService.updateCategory(jcId, request);
        
        return ResponseEntity.ok(
                ApiResponse.<JobCategoryResponse>builder()
                        .status(200)
                        .message("Category updated successfully")
                        .data(categoryResponse)
                        .build()
        );
    }

    /**
     * Xoá danh mục công việc
     * 
     * Endpoint dành cho Admin - Xoá danh mục khỏi hệ thống
     * 
     * Chức năng:
     * - Xoá cứng (hard delete) danh mục khỏi database
     * - Cảnh báo: Nếu có Job đang dùng danh mục này -> Có thể gây lỗi Foreign Key
     * - Trả về HTTP 200 với thông báo xoá thành công
     * 
     * HTTP Method: DELETE /api/v1/categories/{jcId}
     * Path Variable: jcId (Integer)
     * 
     * Bảo mật: Yêu cầu JWT + Vai trò ADM
     * 
     * @param jcId ID danh mục cần xoá
     * @return ResponseEntity<ApiResponse<Void>> - Thông báo xoá thành công
     */
    @DeleteMapping("/{jcId}")
    @PreAuthorize("hasAuthority('ROLE_ADM')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete job category",
            description = "Admin-only endpoint. Delete a job category."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Category deleted successfully",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Integer jcId
    ) {
        jobCategoryService.deleteCategory(jcId);
        
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(200)
                        .message("Category deleted successfully")
                        .data(null)
                        .build()
        );
    }
}
