package com.jobrecruitment.backend.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO Response cho Tin tìm việc của Ứng viên
 * 
 * Tin đăng tìm việc hiển thị thông tin ứng viên cho nhà tuyển dụng.
 * Bao gồm thông tin về vị trí mong muốn, kỹ năng, kinh nghiệm và thông tin liên hệ.
 * 
 * Privacy Modes:
 * - Public (Không đăng nhập): Hiển thị thông tin cơ bản, ẩn tên và avatar
 * - Authenticated (Nhà tuyển dụng): Hiển thị đầy đủ thông tin
 * - Owner (Ứng viên sở hữu): Hiển thị tất cả bao gồm status và thống kê
 * 
 * @author Job Recruitment System
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    description = "Response DTO for job seeking post information",
    example = """
        {
          "id": 1,
          "title": "Tìm việc Java Developer Senior tại TP.HCM",
          "desiredSalary": 25000000,
          "location": "TP. Hồ Chí Minh",
          "skills": ["Java", "Spring Boot", "PostgreSQL", "Docker", "Microservices"],
          "introduction": "Tôi là lập trình viên Java với 5 năm kinh nghiệm...",
          "candidateName": "Nguyễn Văn A",
          "candidateAvatar": "/uploads/avatars/nguyen_van_a.jpg",
          "createdDate": "2025-12-20T10:30:00",
          "status": "ACTIVE"
        }
        """
)
public class JobSeekPostResponse {

    /**
     * ID của tin đăng tìm việc
     */
    @Schema(
        description = "Unique identifier của tin đăng tìm việc",
        example = "1",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    /**
     * Tiêu đề tin đăng
     */
    @Schema(
        description = "Tiêu đề tin đăng tìm việc của ứng viên. " +
                     "Bao gồm vị trí mong muốn và địa điểm.",
        example = "Tìm việc Java Developer Senior tại TP.HCM",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String title;

    /**
     * Mức lương mong muốn (VNĐ)
     */
    @Schema(
        description = "Mức lương mong muốn của ứng viên (VNĐ). " +
                     "Giá trị 0 có nghĩa là 'Thỏa thuận'.",
        example = "25000000",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Double desiredSalary;

    /**
     * Địa điểm làm việc mong muốn
     */
    @Schema(
        description = "Địa điểm làm việc mong muốn của ứng viên. " +
                     "Có thể là tỉnh/thành phố, khu vực hoặc chế độ làm việc.",
        example = "TP. Hồ Chí Minh",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String location;

    /**
     * Danh sách kỹ năng
     */
    @Schema(
        description = "Danh sách kỹ năng của ứng viên. " +
                     "Bao gồm ngôn ngữ lập trình, framework, công nghệ và soft skills.",
        example = "[\"Java\", \"Spring Boot\", \"PostgreSQL\", \"Docker\", \"Microservices\"]",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private List<String> skills;

    /**
     * Giới thiệu bản thân
     */
    @Schema(
        description = "Giới thiệu bản thân của ứng viên. " +
                     "Bao gồm kinh nghiệm, điểm mạnh, mục tiêu nghề nghiệp.",
        example = "Tôi là lập trình viên Java với 5 năm kinh nghiệm phát triển backend. " +
                 "Thành thạo Spring Boot, Microservices, Docker và các công nghệ cloud...",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String introduction;

    /**
     * Tên ứng viên
     * 
     * Privacy:
     * - Public: Ẩn tên (hiển thị "Ứng viên ****")
     * - Authenticated: Hiển thị tên đầy đủ
     */
    @Schema(
        description = "Tên đầy đủ của ứng viên. " +
                     "Đối với người dùng chưa đăng nhập, tên sẽ được ẩn một phần. " +
                     "Chỉ nhà tuyển dụng đã đăng nhập mới xem được tên đầy đủ.",
        example = "Nguyễn Văn A",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String candidateName;

    /**
     * Avatar của ứng viên
     * 
     * Privacy:
     * - Public: Hiển thị avatar mặc định
     * - Authenticated: Hiển thị avatar thật
     */
    @Schema(
        description = "URL đến ảnh đại diện của ứng viên. " +
                     "Đối với người dùng chưa đăng nhập, hiển thị avatar mặc định. " +
                     "Chỉ nhà tuyển dụng đã đăng nhập mới xem được avatar thật.",
        example = "/uploads/avatars/nguyen_van_a.jpg",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String candidateAvatar;

    /**
     * Ngày tạo tin đăng
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(
        description = "Thời điểm tin đăng được tạo. " +
                     "Format: ISO 8601 (yyyy-MM-dd'T'HH:mm:ss)",
        example = "2025-12-20T10:30:00",
        accessMode = Schema.AccessMode.READ_ONLY,
        type = "string",
        format = "date-time"
    )
    private LocalDateTime createdDate;

    /**
     * Trạng thái tin đăng
     * 
     * Các trạng thái:
     * - ACTIVE: Đang công khai, nhà tuyển dụng có thể xem
     * - HIDDEN: Ứng viên tạm ẩn, không hiển thị công khai
     * - DELETED: Admin xóa do vi phạm
     * 
     * Note: Chỉ hiển thị cho chính ứng viên và admin
     */
    @Schema(
        description = "Trạng thái hiện tại của tin đăng. " +
                     "ACTIVE: Đang công khai | HIDDEN: Tạm ẩn | DELETED: Đã bị xóa. " +
                     "Chỉ hiển thị cho chính ứng viên và admin.",
        example = "ACTIVE",
        allowableValues = {"ACTIVE", "HIDDEN", "DELETED"},
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String status;
}
