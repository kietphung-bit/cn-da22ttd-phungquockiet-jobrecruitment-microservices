package com.jobrecruitment.backend.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
        description = "Unique identifier of the job seeking post",
        example = "1",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    /**
     * Tiêu đề tin đăng
     */
    @Schema(
        description = "Title of the job seeking post by the candidate. " +
                     "Includes desired position and location.",
        example = "Tìm việc Java Developer Senior tại TP.HCM",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String title;

    /**
     * Mức lương mong muốn (VNĐ)
     */
    @Schema(
        description = "Desired salary of the candidate (VND). " +
                     "Value 0 means 'Negotiable'.",
        example = "25000000",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Double desiredSalary;

    /**
     * Địa điểm làm việc mong muốn
     */
    @Schema(
        description = "Desired work location of the candidate. " +
                     "Can be a specific city/province, region, or work mode (Remote/Hybrid/Onsite).",
        example = "TP. Hồ Chí Minh",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String location;

    /**
     * Danh sách kỹ năng
     */
    @Schema(
        description = "Skills list of the candidate. " +
                     "Includes programming languages, frameworks, technologies, and soft skills.",
        example = "[\"Java\", \"Spring Boot\", \"PostgreSQL\", \"Docker\", \"Microservices\"]",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private List<String> skills;

    /**
     * Giới thiệu bản thân
     */
    @Schema(
        description = "Introduction of the candidate. " +
                     "Includes experience, strengths, career goals.",
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
        description = "Full name of the candidate. " +
                     "For unauthenticated users, the name will be partially hidden. " +
                     "Only logged-in recruiters can see the full name.",
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
        description = "URL to the candidate's avatar. " +
                     "For unauthenticated users, the default avatar is shown. " +
                     "Only logged-in recruiters can see the real avatar.",
        example = "/uploads/avatars/nguyen_van_a.jpg",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String candidateAvatar;

    /**
     * Ngày tạo tin đăng
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(
        description = "The timestamp when the job post was created. " +
                     "Format: ISO 8601 (yyyy-MM-dd'T'HH:mm:ss)",
        example = "2025-12-20T10:30:00",
        accessMode = Schema.AccessMode.READ_ONLY,
        type = "string",
        format = "date-time"
    )
    private LocalDateTime createdDate;

    /**
     * Ngày hết hạn tin đăng
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(
        description = "Expiry date of the job post. " +
                     "Format: yyyy-MM-dd",
        example = "2026-02-15",
        accessMode = Schema.AccessMode.READ_ONLY,
        type = "string",
        format = "date"
    )
    private LocalDate expiryDate;

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
        description = "Current status of the job post. " +
                     "ACTIVE: Public | HIDDEN: Temporarily hidden | DELETED: Removed due to violation. " +
                     "Only visible to the candidate and admin.",
        example = "ACTIVE",
        allowableValues = {"ACTIVE", "HIDDEN", "DELETED"},
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String status;
}
