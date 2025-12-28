package com.jobrecruitment.backend.controllers;

import com.jobrecruitment.backend.dtos.request.JobSeekPostRequest;
import com.jobrecruitment.backend.dtos.response.ApiResponse;
import com.jobrecruitment.backend.dtos.response.JobSeekPostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JobSeekPostController (Mock for Documentation)
 * 
 * REST API Controller cho Tin tìm việc của Ứng viên.
 * Cho phép ứng viên đăng tin tìm việc và nhà tuyển dụng tìm kiếm ứng viên phù hợp.
 * 
 * Luồng hoạt động:
 * 1. Ứng viên tạo tin đăng tìm việc (POST /)
 * 2. Tin đăng được công khai với status=ACTIVE
 * 3. Nhà tuyển dụng tìm kiếm tin đăng (GET / với filters)
 * 4. Ứng viên có thể chỉnh sửa (PUT /{id}) hoặc ẩn tin (PATCH /{id}/status)
 * 5. Admin có thể xóa tin vi phạm (DELETE /{id})
 * 
 * Privacy:
 * - Public (không đăng nhập): Xem được tin đăng nhưng thông tin ứng viên bị ẩn
 * - Nhà tuyển dụng (DN): Xem được đầy đủ thông tin ứng viên
 * - Ứng viên (UV): Xem được tin đăng của mình và có thể chỉnh sửa
 * - Admin (ADM): Có quyền xóa tin vi phạm
 * 
 * @author Job Recruitment System
 * @version 1.0 (Mock for Documentation)
 */
@RestController
@RequestMapping("/api/v1/job-seek-posts")
@RequiredArgsConstructor
@Tag(
    name = "Job Seeking Posts Management", 
    description = "API quản lý tin tìm việc của ứng viên. " +
                 "Ứng viên có thể đăng tin tìm việc, nhà tuyển dụng tìm kiếm ứng viên phù hợp. " +
                 "Hỗ trợ tìm kiếm theo kỹ năng, địa điểm, mức lương."
)
public class JobSeekPostController {

    // ==================== CREATE JOB SEEKING POST ====================

    /**
     * POST /api/v1/job-seek-posts
     * Ứng viên tạo tin đăng tìm việc mới
     * 
     * Quyền truy cập: Chỉ ứng viên (ROLE_UV)
     * 
     * Business Rules:
     * - Ứng viên phải có hồ sơ hoàn chỉnh (profile đầy đủ thông tin)
     * - Một ứng viên có thể có nhiều tin đăng nhưng chỉ 1 tin ACTIVE tại một thời điểm
     * - Tin đăng mới tự động có status=ACTIVE
     * - Tiêu đề và giới thiệu phải tuân thủ nguyên tắc cộng đồng
     */
    @PostMapping
    @Operation(
        summary = "Ứng viên đăng tin tìm việc mới",
        description = """
            Tạo tin đăng tìm việc mới cho ứng viên.
            
            **Yêu cầu:**
            - Ứng viên phải đăng nhập (JWT token)
            - Hồ sơ ứng viên phải hoàn chỉnh
            - Chỉ được có 1 tin ACTIVE tại một thời điểm
            
            **Quy trình:**
            1. Validate dữ liệu đầu vào
            2. Kiểm tra ứng viên có tin ACTIVE nào không
            3. Nếu có, tự động chuyển tin cũ sang HIDDEN
            4. Tạo tin mới với status=ACTIVE
            5. Trả về thông tin tin đăng đã tạo
            
            **Lưu ý:**
            - Nội dung không được chứa thông tin nhạy cảm
            - Không được spam hoặc vi phạm chính sách
            - Mức lương = 0 có nghĩa là "Thỏa thuận"
            """,
        tags = {"Job Seeking Posts Management"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Tin đăng được tạo thành công",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Dữ liệu đầu vào không hợp lệ hoặc ứng viên đã có tin ACTIVE",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Chưa đăng nhập hoặc token không hợp lệ",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Không có quyền truy cập - Chỉ ứng viên mới được tạo tin",
            content = @Content
        )
    })
    public ResponseEntity<ApiResponse<JobSeekPostResponse>> createJobSeekPost(
        @Valid @RequestBody 
        @Parameter(
            description = "Thông tin tin đăng tìm việc mới",
            required = true
        )
        JobSeekPostRequest request
    ) {
        // Mock response
        JobSeekPostResponse response = JobSeekPostResponse.builder()
            .id(1L)
            .title(request.getTitle())
            .desiredSalary(request.getDesiredSalary())
            .location(request.getLocation())
            .skills(request.getSkills())
            .introduction(request.getIntroduction())
            .candidateName("Nguyễn Văn A")
            .candidateAvatar("/uploads/avatars/nguyen_van_a.jpg")
            .createdDate(LocalDateTime.now())
            .status("ACTIVE")
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.<JobSeekPostResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tin đăng tìm việc đã được tạo thành công")
                .data(response)
                .build()
        );
    }

    // ==================== UPDATE JOB SEEKING POST ====================

    /**
     * PUT /api/v1/job-seek-posts/{id}
     * Cập nhật nội dung tin đăng tìm việc
     * 
     * Quyền truy cập: Chỉ chính ứng viên sở hữu
     * 
     * Business Rules:
     * - Chỉ ứng viên sở hữu tin mới được cập nhật
     * - Không thể cập nhật tin đã bị xóa (status=DELETED)
     * - Tin HIDDEN có thể cập nhật và sẽ tự động chuyển sang ACTIVE
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Cập nhật nội dung tin tìm việc",
        description = """
            Cập nhật toàn bộ nội dung tin đăng tìm việc.
            
            **Yêu cầu:**
            - Ứng viên phải là chủ sở hữu tin đăng
            - Tin đăng chưa bị xóa (status != DELETED)
            
            **Quy trình:**
            1. Kiểm tra quyền sở hữu
            2. Validate dữ liệu mới
            3. Cập nhật tất cả các trường
            4. Nếu tin đang HIDDEN, tự động chuyển sang ACTIVE
            5. Trả về thông tin tin đăng đã cập nhật
            
            **Lưu ý:**
            - Tất cả các trường đều bắt buộc (Full Update)
            - Không thể cập nhật tin đã bị Admin xóa
            - Thời gian createdDate không thay đổi
            """,
        tags = {"Job Seeking Posts Management"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Tin đăng được cập nhật thành công",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Dữ liệu đầu vào không hợp lệ",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Không có quyền cập nhật tin đăng này",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy tin đăng",
            content = @Content
        )
    })
    public ResponseEntity<ApiResponse<JobSeekPostResponse>> updateJobSeekPost(
        @PathVariable 
        @Parameter(
            description = "ID của tin đăng cần cập nhật",
            example = "1",
            required = true
        )
        Long id,
        
        @Valid @RequestBody
        @Parameter(
            description = "Thông tin mới cho tin đăng",
            required = true
        )
        JobSeekPostRequest request
    ) {
        // Mock response
        JobSeekPostResponse response = JobSeekPostResponse.builder()
            .id(id)
            .title(request.getTitle())
            .desiredSalary(request.getDesiredSalary())
            .location(request.getLocation())
            .skills(request.getSkills())
            .introduction(request.getIntroduction())
            .candidateName("Nguyễn Văn A")
            .candidateAvatar("/uploads/avatars/nguyen_van_a.jpg")
            .createdDate(LocalDateTime.now().minusDays(5))
            .status("ACTIVE")
            .build();

        return ResponseEntity.ok(
            ApiResponse.<JobSeekPostResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Tin đăng đã được cập nhật thành công")
                .data(response)
                .build()
        );
    }

    // ==================== SEARCH JOB SEEKING POSTS (PUBLIC) ====================

    /**
     * GET /api/v1/job-seek-posts
     * Tìm kiếm hồ sơ ứng viên (Public)
     * 
     * Quyền truy cập: Public (cả guest và authenticated)
     * 
     * Privacy Mode:
     * - Guest (không đăng nhập): Thông tin ứng viên bị ẩn một phần
     * - Nhà tuyển dụng (đã đăng nhập): Xem đầy đủ thông tin
     * 
     * Filters hỗ trợ:
     * - skills: Tìm theo kỹ năng (partial match)
     * - location: Tìm theo địa điểm (partial match)
     * - minSalary/maxSalary: Lọc theo mức lương mong muốn
     * - keyword: Tìm trong title và introduction
     */
    @GetMapping
    @Operation(
        summary = "Tìm kiếm hồ sơ ứng viên (Public)",
        description = """
            Tìm kiếm và lọc danh sách tin đăng tìm việc của ứng viên.
            
            **Chế độ hiển thị:**
            - **Guest (không đăng nhập):** Thông tin ứng viên bị ẩn một phần
              - Tên: "Ứng viên ****"
              - Avatar: Hình mặc định
              - Các thông tin khác: Hiển thị bình thường
            
            - **Nhà tuyển dụng (đã đăng nhập):** Xem đầy đủ thông tin
              - Tên: Tên đầy đủ
              - Avatar: Avatar thật
              - Có thể liên hệ ứng viên
            
            **Filters:**
            - `skills`: Tìm theo kỹ năng (có thể có nhiều kỹ năng, cách nhau dấu phẩy)
            - `location`: Tìm theo địa điểm (partial match)
            - `minSalary`: Lương tối thiểu
            - `maxSalary`: Lương tối đa
            - `keyword`: Tìm trong tiêu đề và giới thiệu
            
            **Sorting:**
            - Mặc định: Tin mới nhất trước (createdDate desc)
            - Có thể sort theo: desiredSalary, createdDate
            
            **Pagination:**
            - Default: page=0, size=20
            - Max size: 100
            """,
        tags = {"Job Seeking Posts Management"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Danh sách tin đăng được trả về thành công",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        )
    })
    public ResponseEntity<ApiResponse<Page<JobSeekPostResponse>>> searchJobSeekPosts(
        @RequestParam(required = false)
        @Parameter(
            description = "Tìm theo kỹ năng. Có thể có nhiều kỹ năng cách nhau dấu phẩy. " +
                         "Ví dụ: 'Java,Spring Boot,Docker'",
            example = "Java,Spring Boot"
        )
        String skills,
        
        @RequestParam(required = false)
        @Parameter(
            description = "Tìm theo địa điểm làm việc (partial match). " +
                         "Ví dụ: 'TP.HCM', 'Hà Nội', 'Remote'",
            example = "TP. Hồ Chí Minh"
        )
        String location,
        
        @RequestParam(required = false)
        @Parameter(
            description = "Mức lương tối thiểu (VNĐ)",
            example = "15000000"
        )
        Double minSalary,
        
        @RequestParam(required = false)
        @Parameter(
            description = "Mức lương tối đa (VNĐ)",
            example = "30000000"
        )
        Double maxSalary,
        
        @RequestParam(required = false)
        @Parameter(
            description = "Từ khóa tìm kiếm trong tiêu đề và giới thiệu",
            example = "Java Developer"
        )
        String keyword,
        
        @PageableDefault(size = 20, sort = "createdDate")
        @Parameter(
            description = "Pagination parameters. " +
                         "Example: ?page=0&size=20&sort=desiredSalary,desc",
            schema = @Schema(type = "string")
        )
        Pageable pageable
    ) {
        // Mock data
        List<JobSeekPostResponse> posts = new ArrayList<>();
        posts.add(JobSeekPostResponse.builder()
            .id(1L)
            .title("Tìm việc Java Developer Senior tại TP.HCM")
            .desiredSalary(25000000.0)
            .location("TP. Hồ Chí Minh")
            .skills(List.of("Java", "Spring Boot", "PostgreSQL", "Docker", "Microservices"))
            .introduction("Tôi là lập trình viên Java với 5 năm kinh nghiệm...")
            .candidateName("Nguyễn Văn A")
            .candidateAvatar("/uploads/avatars/nguyen_van_a.jpg")
            .createdDate(LocalDateTime.now().minusDays(2))
            .status("ACTIVE")
            .build());

        Page<JobSeekPostResponse> page = new PageImpl<>(posts, pageable, posts.size());

        return ResponseEntity.ok(
            ApiResponse.<Page<JobSeekPostResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách tin tìm việc được trả về thành công")
                .data(page)
                .build()
        );
    }

    // ==================== GET JOB SEEKING POST DETAIL ====================

    /**
     * GET /api/v1/job-seek-posts/{id}
     * Xem chi tiết tin đăng tìm việc
     * 
     * Quyền truy cập: Public
     * 
     * Privacy Mode:
     * - Guest: Thông tin ứng viên bị ẩn
     * - Nhà tuyển dụng: Xem đầy đủ thông tin + có nút "Liên hệ ứng viên"
     * - Ứng viên sở hữu: Xem tất cả bao gồm status và thống kê
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Xem chi tiết tin đăng tìm việc",
        description = """
            Lấy thông tin chi tiết của một tin đăng tìm việc.
            
            **Chế độ hiển thị:**
            - **Guest:** Thông tin ứng viên bị ẩn một phần
            - **Nhà tuyển dụng:** Xem đầy đủ + có thể liên hệ
            - **Ứng viên sở hữu:** Xem tất cả bao gồm:
              - Status của tin đăng
              - Số lượt xem
              - Số nhà tuyển dụng đã liên hệ
              - Nút "Chỉnh sửa" và "Ẩn/Hiện"
            
            **Response:**
            - 200: Trả về thông tin tin đăng
            - 404: Không tìm thấy tin đăng hoặc tin đã bị xóa
            """,
        tags = {"Job Seeking Posts Management"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Thông tin tin đăng được trả về thành công",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy tin đăng hoặc tin đã bị xóa",
            content = @Content
        )
    })
    public ResponseEntity<ApiResponse<JobSeekPostResponse>> getJobSeekPostById(
        @PathVariable
        @Parameter(
            description = "ID của tin đăng cần xem",
            example = "1",
            required = true
        )
        Long id
    ) {
        // Mock response
        JobSeekPostResponse response = JobSeekPostResponse.builder()
            .id(id)
            .title("Tìm việc Java Developer Senior tại TP.HCM")
            .desiredSalary(25000000.0)
            .location("TP. Hồ Chí Minh")
            .skills(List.of("Java", "Spring Boot", "PostgreSQL", "Docker", "Microservices"))
            .introduction("Tôi là lập trình viên Java với 5 năm kinh nghiệm phát triển backend...")
            .candidateName("Nguyễn Văn A")
            .candidateAvatar("/uploads/avatars/nguyen_van_a.jpg")
            .createdDate(LocalDateTime.now().minusDays(5))
            .status("ACTIVE")
            .build();

        return ResponseEntity.ok(
            ApiResponse.<JobSeekPostResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thông tin tin đăng được trả về thành công")
                .data(response)
                .build()
        );
    }

    // ==================== UPDATE JOB SEEKING POST STATUS ====================

    /**
     * PATCH /api/v1/job-seek-posts/{id}/status
     * Ẩn/Hiện tin đăng tìm việc
     * 
     * Quyền truy cập: Chỉ ứng viên sở hữu
     * 
     * Status transitions:
     * - ACTIVE → HIDDEN: Ẩn tin tạm thời
     * - HIDDEN → ACTIVE: Hiện lại tin đăng
     * - DELETED: Không thể thay đổi
     */
    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Ẩn/Hiện tin đăng tìm việc",
        description = """
            Thay đổi trạng thái hiển thị của tin đăng.
            
            **Yêu cầu:**
            - Chỉ ứng viên sở hữu tin mới được thay đổi status
            - Tin đã bị xóa (DELETED) không thể thay đổi
            
            **Status transitions:**
            - `ACTIVE → HIDDEN`: Ẩn tin tạm thời (không hiển thị công khai)
            - `HIDDEN → ACTIVE`: Hiện lại tin đăng (công khai trở lại)
            
            **Use cases:**
            - Ứng viên đã tìm được việc → Ẩn tin
            - Ứng viên muốn tạm ngưng nhận liên hệ → Ẩn tin
            - Ứng viên muốn tìm việc lại → Hiện tin
            
            **Request Body:**
            ```json
            {
              "status": "HIDDEN"
            }
            ```
            
            Giá trị cho phép: "ACTIVE", "HIDDEN"
            """,
        tags = {"Job Seeking Posts Management"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Trạng thái tin đăng được cập nhật thành công",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Trạng thái không hợp lệ hoặc tin đã bị xóa",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Không có quyền thay đổi trạng thái tin đăng này",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy tin đăng",
            content = @Content
        )
    })
    public ResponseEntity<ApiResponse<String>> updateJobSeekPostStatus(
        @PathVariable
        @Parameter(
            description = "ID của tin đăng cần thay đổi trạng thái",
            example = "1",
            required = true
        )
        Long id,
        
        @RequestParam
        @Parameter(
            description = "Trạng thái mới của tin đăng",
            example = "HIDDEN",
            required = true,
            schema = @Schema(allowableValues = {"ACTIVE", "HIDDEN"})
        )
        String status
    ) {
        return ResponseEntity.ok(
            ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Trạng thái tin đăng đã được cập nhật thành công")
                .data("Tin đăng hiện đang ở trạng thái: " + status)
                .build()
        );
    }

    // ==================== DELETE JOB SEEKING POST (ADMIN) ====================

    /**
     * DELETE /api/v1/job-seek-posts/{id}
     * Admin xóa tin đăng vi phạm
     * 
     * Quyền truy cập: Chỉ Admin (ROLE_ADM)
     * 
     * Business Rules:
     * - Chỉ Admin mới có quyền xóa tin
     * - Xóa là soft delete (chuyển status=DELETED)
     * - Tin bị xóa không hiển thị ở bất kỳ đâu
     * - Không thể khôi phục tin đã bị xóa
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Admin xóa tin đăng vi phạm",
        description = """
            Xóa tin đăng tìm việc vi phạm quy định.
            
            **Yêu cầu:**
            - Chỉ Admin (ROLE_ADM) mới có quyền
            - Cần ghi rõ lý do xóa trong hệ thống audit log
            
            **Quy trình:**
            1. Admin kiểm tra tin đăng
            2. Xác nhận tin vi phạm (spam, nội dung không phù hợp, etc.)
            3. Xóa tin (soft delete - chuyển status=DELETED)
            4. Gửi thông báo cho ứng viên về lý do xóa
            5. Ghi log hành động của Admin
            
            **Lý do xóa thường gặp:**
            - Nội dung spam
            - Thông tin giả mạo
            - Nội dung không phù hợp
            - Vi phạm chính sách cộng đồng
            - Sử dụng ngôn từ không phù hợp
            
            **Lưu ý:**
            - Xóa là KHÔNG THỂ KHÔI PHỤC
            - Tin bị xóa hoàn toàn không hiển thị
            - Ứng viên có thể tạo tin mới tuân thủ quy định
            """,
        tags = {"Job Seeking Posts Management"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Tin đăng đã được xóa thành công",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Không có quyền xóa tin đăng - Chỉ Admin",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy tin đăng",
            content = @Content
        )
    })
    public ResponseEntity<ApiResponse<String>> deleteJobSeekPost(
        @PathVariable
        @Parameter(
            description = "ID của tin đăng cần xóa",
            example = "1",
            required = true
        )
        Long id
    ) {
        return ResponseEntity.ok(
            ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Tin đăng đã được xóa thành công")
                .data("Tin đăng ID " + id + " đã bị xóa khỏi hệ thống")
                .build()
        );
    }
}
