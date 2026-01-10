package com.jobrecruitment.backend.controllers;

import com.jobrecruitment.backend.dtos.response.ApiResponse;
import com.jobrecruitment.backend.dtos.response.DashboardStatsResponse;
import com.jobrecruitment.backend.dtos.response.UserResponse;
import com.jobrecruitment.backend.enums.CompanyStatus;
import com.jobrecruitment.backend.enums.JobStatus;
import com.jobrecruitment.backend.services.AdminServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * AdminControllerV1 - RESTful API Controller quản trị hệ thống
 * 
 * Base URL: /api/v1/admin
 * 
 * Danh sách endpoint (Tất cả yêu cầu vai trò ADM):
 * 1. GET /dashboard/stats           - Thống kê tổng quan hệ thống
 * 2. GET /users                     - Danh sách người dùng (phân trang, lọc theo vai trò)
 * 3. PATCH /users/{id}/lock         - Khóa tài khoản người dùng
 * 4. PATCH /users/{id}/unlock       - Mở khóa tài khoản người dùng
 * 5. PATCH /companies/{id}/status   - Thay đổi trạng thái doanh nghiệp (kiểm duyệt)
 * 6. PATCH /jobs/{id}/status        - Thay đổi trạng thái công việc (Post-moderation)
 * 7. DELETE /jobs/{id}              - Xóa tin tuyển dụng vi phạm (Post-moderation)
 * 8. DELETE /seeking-posts/{id}     - Xóa tin đăng tìm việc vi phạm (Post-moderation)
 * 
 * Bảo mật:
 * - Tất cả endpoint yêu cầu JWT Authentication (Bearer Token)
 * - Chỉ người dùng có vai trò ADM (Admin) mới truy cập được
 * - Áp dụng @PreAuthorize("hasAuthority('ROLE_ADM')") ở class-level
 * 
 * Chức năng chính:
 * - Thống kê dashboard: Người dùng, công việc, đơn ứng tuyển
 * - Quản lý người dùng: Xem danh sách, khóa/mở khóa
 * - Kiểm duyệt: Duyệt doanh nghiệp
 * - Post-moderation: DELETE/BLOCK vi phạm (jobs, seeking posts) - NO pre-approval
 * 
 * Post-moderation Policy:
 * - Admin does NOT approve content before publication
 * - Admin ONLY removes violations after publication
 * - Users are fully responsible for content accuracy and legality
 * 
 * Phụ thuộc:
 * - AdminServiceV1: Xử lý business logic cho tất cả chức năng Admin
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management V1", description = "RESTful API for admin operations")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ROLE_ADM')")
public class AdminControllerV1 {

    private final AdminServiceV1 adminServiceV1;

    /**
     * Lấy thống kê tổng quan dashboard
     * 
     * Endpoint dành cho Admin - Trả về thống kê toàn hệ thống bao gồm:
     * - Số lượng người dùng: Tổng số, ứng viên, nhà tuyển dụng (theo trạng thái)
     * - Số lượng công việc: Tổng số, theo trạng thái (ACTIVE/PENDING/CLOSED/HIDDEN)
     * - Số lượng đơn ứng tuyển: Tổng số, hôm nay, tháng này
     * 
     * HTTP Method: GET /api/v1/admin/dashboard/stats
     * 
     * Bảo mật: Yêu cầu JWT + Vai trò ADM
     * 
     * @return ResponseEntity<ApiResponse<DashboardStatsResponse>> - Thống kê hệ thống
     */
    @GetMapping("/dashboard/stats")
    @Operation(
            summary = "Get dashboard statistics",
            description = "Admin-only endpoint. Returns comprehensive system statistics including:" +
                    "\n- User counts (total, candidates, employers by status)" +
                    "\n- Job statistics (total, by status)" +
                    "\n- Application metrics (total, today, this month)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DashboardStatsResponse.class))
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
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = adminServiceV1.getDashboardStats();
        
        return ResponseEntity.ok(
                ApiResponse.<DashboardStatsResponse>builder()
                        .status(200)
                        .message("Dashboard statistics retrieved successfully")
                        .data(stats)
                        .build()
        );
    }

    /**
     * Lấy danh sách người dùng (phân trang và lọc theo vai trò)
     * 
     * Endpoint dành cho Admin - Trả về danh sách người dùng có phân trang
     * 
     * Tham số lọc:
     * - roleCode (optional): Lọc theo vai trò (ADM, DN, UV)
     * 
     * Tham số phân trang:
     * - page: Số trang (bắt đầu từ 0)
     * - size: Kích thước trang (mặc định: 20)
     * - sort: Sắp xếp theo trường (ví dụ: 'username,asc')
     * 
     * HTTP Method: GET /api/v1/admin/users?roleCode=UV&page=0&size=20
     * 
     * Bảo mật: Yêu cầu JWT + Vai trò ADM
     * 
     * @param pageable Tham số phân trang và sắp xếp
     * @param roleCode Mã vai trò lọc (ADM, DN, UV) - Tùy chọn
     * @return ResponseEntity<ApiResponse<Page<UserResponse>>> - Danh sách người dùng phân trang
     */
    @GetMapping("/users")
    @Operation(
            summary = "Get all users",
            description = "Admin-only endpoint. Returns paginated list of users. " +
                    "\n\nOptional Filters:" +
                    "\n- roleCode: Filter by role (ADM, DN, UV)" +
                    "\n\nPagination Parameters:" +
                    "\n- page: Page number (0-indexed)" +
                    "\n- size: Page size (default: 20)" +
                    "\n- sort: Sort by field (e.g., 'username,asc')"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
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
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @Parameter(description = "Pagination and sorting parameters. Valid sort fields: 'userId', 'username', 'email', 'roleCode'. Example: 'userId,asc'",
                      schema = @Schema(type = "string", example = "userId,asc"))
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "Optional role code filter (ADM, DN, UV)", example = "UV")
            @RequestParam(required = false) String roleCode
    ) {
        Page<UserResponse> users = adminServiceV1.getAllUsers(pageable, roleCode);
        
        return ResponseEntity.ok(
                ApiResponse.<Page<UserResponse>>builder()
                        .status(200)
                        .message("Users retrieved successfully")
                        .data(users)
                        .build()
        );
    }

    /**
     * Khóa tài khoản người dùng (chặn đăng nhập)
     * 
     * Endpoint dành cho Admin - Khóa tài khoản người dùng, ngăn chặn đăng nhập
     * 
     * Chức năng:
     * - Đặt User.locked = true
     * - Người dùng bị khóa không thể đăng nhập vào hệ thống
     * - Không thể khóa lại tài khoản đã bị khóa (ném IllegalStateException)
     * 
     * HTTP Method: PATCH /api/v1/admin/users/{userId}/lock
     * 
     * Bảo mật: Yêu cầu JWT + Vai trò ADM
     * 
     * @param userId ID người dùng cần khóa
     * @return ResponseEntity<ApiResponse<String>> - Thông báo khóa thành công
     */
    @PatchMapping("/users/{userId}/lock")
    @Operation(
            summary = "Lock user account",
            description = "Admin-only endpoint. Lock/ban a user account, preventing login."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User locked successfully",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - User already locked",
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
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<String>> lockUser(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long userId
    ) {
        String message = adminServiceV1.lockUser(userId);
        
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message(message)
                        .data(null)
                        .build()
        );
    }

    /**
     * Mở khóa tài khoản người dùng (cho phép đăng nhập)
     * 
     * Endpoint dành cho Admin - Mở khóa tài khoản người dùng, cho phép đăng nhập lại
     * 
     * Chức năng:
     * - Đặt User.locked = false
     * - Người dùng có thể đăng nhập vào hệ thống trở lại
     * - Không thể mở khóa tài khoản chưa bị khóa (ném IllegalStateException)
     * 
     * HTTP Method: PATCH /api/v1/admin/users/{userId}/unlock
     * 
     * Bảo mật: Yêu cầu JWT + Vai trò ADM
     * 
     * @param userId ID người dùng cần mở khóa
     * @return ResponseEntity<ApiResponse<String>> - Thông báo mở khóa thành công
     */
    @PatchMapping("/users/{userId}/unlock")
    @Operation(
            summary = "Unlock user account",
            description = "Admin-only endpoint. Unlock/unban a user account, allowing login."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User unlocked successfully",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - User not locked",
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
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<String>> unlockUser(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long userId
    ) {
        String message = adminServiceV1.unlockUser(userId);
        
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message(message)
                        .data(null)
                        .build()
        );
    }

    /**
     * Thay đổi trạng thái doanh nghiệp (kiểm duyệt)
     * 
     * Endpoint dành cho Admin - Kiểm duyệt và thay đổi trạng thái doanh nghiệp
     * 
     * Các trạng thái:
     * - PENDING: Chờ xét duyệt (mới đăng ký)
     * - ACTIVE: Đã duyệt, đang hoạt động (có thể đăng tin tuyển dụng)
     * - BLOCKED: Bị khóa (không thể đăng tin tuyển dụng)
     * 
     * HTTP Method: PATCH /api/v1/admin/companies/{companyId}/status?newStatus=ACTIVE
     * 
     * Bảo mật: Yêu cầu JWT + Vai trò ADM
     * 
     * @param companyId ID doanh nghiệp cần thay đổi trạng thái
     * @param newStatus Trạng thái mới (PENDING/ACTIVE/BLOCKED)
     * @return ResponseEntity<ApiResponse<String>> - Thông báo thay đổi thành công
     */
    @PatchMapping("/companies/{companyId}/status")
    @Operation(
            summary = "Moderate company status",
            description = "Admin-only endpoint. Change company status for moderation workflow. " +
                    "\n\nStatus Options:" +
                    "\n- PENDING: Awaiting approval" +
                    "\n- ACTIVE: Approved and active" +
                    "\n- BLOCKED: Blocked from posting jobs"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Company status updated successfully",
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
                    description = "Company not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<String>> changeCompanyStatus(
            @Parameter(description = "Company ID", example = "1")
            @PathVariable Long companyId,
            @Parameter(description = "New company status", example = "ACTIVE")
            @RequestParam CompanyStatus newStatus
    ) {
        String message = adminServiceV1.changeCompanyStatus(companyId, newStatus);
        
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message(message)
                        .data(null)
                        .build()
        );
    }

    /**
     * Thay đổi trạng thái công việc (Post-moderation)
     * 
     * Endpoint dành cho Admin - Thay đổi trạng thái tin tuyển dụng
     * 
     * Post-moderation Model:
     * - Admin KHÔNG pre-approve content
     * - Admin CHỈ thay đổi status để DELETE/BLOCK violations
     * - Recommended: Use DELETE /jobs/{id} for permanent removal
     * 
     * Các trạng thái:
     * - WAIT: Chưa mở (startDate > today)
     * - ACTIVE: Đang mở (ứng viên có thể ứng tuyển)
     * - CLOSED: Đã đóng (đủ ứng viên hoặc hết hạn)
     * - HIDDEN: Tạm ẩn/Bị khóa (vi phạm)
     * 
     * HTTP Method: PATCH /api/v1/admin/jobs/{jobId}/status?newStatus=HIDDEN
     * 
     * Bảo mật: Yêu cầu JWT + Vai trò ADM
     * 
     * @param jobId ID công việc cần thay đổi trạng thái
     * @param newStatus Trạng thái mới (WAIT/ACTIVE/CLOSED/HIDDEN - NOT PENDING)
     * @return ResponseEntity<ApiResponse<String>> - Thông báo thay đổi thành công
     */
    @PatchMapping("/jobs/{jobId}/status")
    @Operation(
            summary = "Moderate job status (Post-moderation)",
            description = "Admin-only endpoint. Change job status for Post-moderation workflow. " +
                    "\n\n**Post-moderation Model:**" +
                    "\n- Admin does NOT pre-approve jobs" +
                    "\n- Admin ONLY blocks/deletes violations after publication" +
                    "\n- Use DELETE /jobs/{id} for permanent removal" +
                    "\n\n**Status Options:**" +
                    "\n- WAIT: Not yet open (startDate > today)" +
                    "\n- ACTIVE: Published and accepting applications" +
                    "\n- CLOSED: Position filled or expired" +
                    "\n- HIDDEN: Blocked by admin for policy violation"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job status updated successfully",
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
                    description = "Job not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<String>> changeJobStatus(
            @Parameter(description = "Job ID", example = "1")
            @PathVariable Long jobId,
            @Parameter(description = "New job status (WAIT/ACTIVE/CLOSED/HIDDEN)", example = "HIDDEN")
            @RequestParam JobStatus newStatus
    ) {
        String message = adminServiceV1.changeJobStatus(jobId, newStatus);
        
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message(message)
                        .data(null)
                        .build()
        );
    }
    
    /**
     * Xóa tin tuyển dụng vi phạm (Post-moderation)
     * 
     * Endpoint dành cho Admin - Xóa tin tuyển dụng do vi phạm chính sách
     * 
     * Post-moderation Policy:
     * - Admin removes content AFTER publication when violations are detected
     * - No pre-approval process, immediate action on violations
     * - Employer is fully responsible for content legality
     * 
     * Chức năng:
     * - Soft delete: Chuyển JobStatus thành HIDDEN
     * - Sử dụng khi phát hiện: Scam, Gambling, Offensive content
     * - Admin có quyền xóa mà không cần thông báo trước
     * 
     * HTTP Method: DELETE /api/v1/admin/jobs/{jobId}
     * 
     * Bảo mật: Yêu cầu JWT + Vai trò ADM
     * 
     * @param jobId ID tin tuyển dụng cần xóa
     * @return ResponseEntity<ApiResponse<String>> - Thông báo xóa thành công
     */
    @DeleteMapping("/jobs/{jobId}")
    @Operation(
            summary = "Delete job posting (Post-moderation)",
            description = "Admin-only endpoint. Remove job posting for policy violations. " +
                    "\n\n**Post-moderation Policy:**" +
                    "\n- Admin removes content AFTER publication" +
                    "\n- Immediate action on violations (scam, offensive)" +
                    "\n- Employer is fully responsible for content" +
                    "\n\n**Action:** Soft delete (changes status to HIDDEN)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job deleted successfully",
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
                    description = "Job not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<String>> deleteJob(
            @Parameter(description = "Job ID to delete", example = "1")
            @PathVariable Long jobId
    ) {
        String message = adminServiceV1.deleteJob(jobId);
        
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message(message)
                        .data(null)
                        .build()
        );
    }
    
    /**
     * Xóa tin đăng tìm việc vi phạm (Post-moderation)
     * 
     * Endpoint dành cho Admin - Xóa tin đăng tìm việc do vi phạm chính sách
     * 
     * Post-moderation Policy:
     * - Admin removes content AFTER publication when violations are detected
     * - No pre-approval process for candidate seeking posts
     * - Candidate is fully responsible for profile authenticity
     * 
     * Chức năng:
     * - Soft delete: Chuyển SKPostStatus thành CLOSED
     * - Sử dụng khi phát hiện: Fake profile, Inappropriate content
     * - Admin có quyền xóa mà không cần thông báo trước
     * 
     * HTTP Method: DELETE /api/v1/admin/seeking-posts/{seekingPostId}
     * 
     * Bảo mật: Yêu cầu JWT + Vai trò ADM
     * 
     * @param seekingPostId ID tin đăng tìm việc cần xóa
     * @return ResponseEntity<ApiResponse<String>> - Thông báo xóa thành công
     */
    @DeleteMapping("/seeking-posts/{seekingPostId}")
    @Operation(
            summary = "Delete seeking post (Post-moderation)",
            description = "Admin-only endpoint. Remove candidate seeking post for policy violations. " +
                    "\n\n**Post-moderation Policy:**" +
                    "\n- Admin removes content AFTER publication" +
                    "\n- Immediate action on violations (fake, inappropriate)" +
                    "\n- Candidate is fully responsible for profile authenticity" +
                    "\n\n**Action:** Soft delete (changes status to CLOSED)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Seeking post deleted successfully",
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
                    description = "Seeking post not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<String>> deleteSeekingPost(
            @Parameter(description = "Seeking post ID to delete", example = "1")
            @PathVariable Long seekingPostId
    ) {
        String message = adminServiceV1.deleteSeekingPost(seekingPostId);
        
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message(message)
                        .data(null)
                        .build()
        );
    }
    
    /**
     * Toggle trạng thái công việc (ACTIVE <-> HIDDEN)
     * 
     * Endpoint dành cho Admin - Toggle visibility của tin tuyển dụng
     * 
     * Chức năng:
     * - Nếu JobStatus = ACTIVE: Chuyển thành HIDDEN (ẩn tin)
     * - Nếu JobStatus = HIDDEN: Chuyển thành ACTIVE (hiện tin)
     * - Dùng để quản lý nội dung nhanh chóng
     * 
     * HTTP Method: PATCH /api/v1/admin/jobs/{jobId}/toggle-status
     * 
     * Bảo mật: Yêu cầu JWT + Vai trò ADM
     * 
     * @param jobId ID công việc cần toggle
     * @return ResponseEntity<ApiResponse<String>> - Thông báo toggle thành công
     */
    @PatchMapping("/jobs/{jobId}/toggle-status")
    @Operation(
            summary = "Toggle job visibility (ACTIVE ↔ HIDDEN)",
            description = "Admin-only endpoint. Toggle job visibility between ACTIVE and HIDDEN status. " +
                    "\n\nUseful for quick content moderation."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job status toggled successfully",
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
                    description = "Job not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<String>> toggleJobStatus(
            @Parameter(description = "Job ID", example = "1")
            @PathVariable Long jobId
    ) {
        String message = adminServiceV1.toggleJobStatus(jobId);
        
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message(message)
                        .data(null)
                        .build()
        );
    }
    
    /**
     * Lấy tất cả tin đăng tìm việc (Admin)
     * 
     * Endpoint dành cho Admin - Lấy danh sách tin đăng tìm việc để quản lý
     * 
     * Chức năng:
     * - Lấy tất cả SeekingPost bao gồm ACTIVE, HIDDEN, CLOSED
     * - Phân trang và sắp xếp theo thời gian tạo mới nhất
     * - Admin có thể xem tất cả tin đăng để moderation
     * 
     * HTTP Method: GET /api/v1/admin/seeking-posts?page=0&size=20
     * 
     * Bảo mật: Yêu cầu JWT + Vai trò ADM
     * 
     * @param page Số trang (0-indexed)
     * @param size Kích thước trang
     * @return ResponseEntity<ApiResponse<Page<JobSeekPostResponse>>> - Danh sách tin đăng phân trang
     */
    @GetMapping("/seeking-posts")
    @Operation(
            summary = "Get all seeking posts (Admin)",
            description = "Admin-only endpoint. Retrieve all seeking posts including ACTIVE, HIDDEN, and CLOSED status for content moderation."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Seeking posts retrieved successfully",
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
    public ResponseEntity<ApiResponse<Page<com.jobrecruitment.backend.dtos.response.JobSeekPostResponse>>> getAllSeekingPosts(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.jobrecruitment.backend.dtos.response.JobSeekPostResponse> seekingPosts = adminServiceV1.getAllSeekingPosts(pageable);
        
        return ResponseEntity.ok(
                ApiResponse.<Page<com.jobrecruitment.backend.dtos.response.JobSeekPostResponse>>builder()
                        .status(200)
                        .message("Seeking posts retrieved successfully")
                        .data(seekingPosts)
                        .build()
        );
    }
    
    /**
     * Toggle trạng thái tin đăng tìm việc (ACTIVE <-> HIDDEN)
     * 
     * Endpoint dành cho Admin - Toggle visibility của tin đăng tìm việc
     * 
     * Chức năng:
     * - Nếu SKPostStatus = ACTIVE: Chuyển thành HIDDEN (ẩn tin)
     * - Nếu SKPostStatus = HIDDEN: Chuyển thành ACTIVE (hiện tin)
     * - Dùng để quản lý nội dung nhanh chóng
     * 
     * HTTP Method: PATCH /api/v1/admin/seeking-posts/{seekingPostId}/toggle-status
     * 
     * Bảo mật: Yêu cầu JWT + Vai trò ADM
     * 
     * @param seekingPostId ID tin đăng tìm việc cần toggle
     * @return ResponseEntity<ApiResponse<String>> - Thông báo toggle thành công
     */
    @PatchMapping("/seeking-posts/{seekingPostId}/toggle-status")
    @Operation(
            summary = "Toggle seeking post visibility (ACTIVE ↔ HIDDEN)",
            description = "Admin-only endpoint. Toggle seeking post visibility between ACTIVE and HIDDEN status. " +
                    "\n\nUseful for quick content moderation."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Seeking post status toggled successfully",
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
                    description = "Seeking post not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<String>> toggleSeekingPostStatus(
            @Parameter(description = "Seeking post ID", example = "1")
            @PathVariable Long seekingPostId
    ) {
        String message = adminServiceV1.toggleSeekingPostStatus(seekingPostId);
        
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message(message)
                        .data(null)
                        .build()
        );
    }
}
