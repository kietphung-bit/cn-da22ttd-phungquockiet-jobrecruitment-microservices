package com.jobrecruitment.backend.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO Request cho Tin tìm việc của Ứng viên
 * 
 * Ứng viên có thể tạo tin đăng tìm việc để các nhà tuyển dụng tìm thấy.
 * Tin đăng bao gồm thông tin về vị trí mong muốn, mức lương, kỹ năng và giới thiệu bản thân.
 * 
 * Validation Rules:
 * - Tiêu đề: Bắt buộc, 10-200 ký tự
 * - Mức lương: Phải >= 0 (0 = Thỏa thuận)
 * - Địa điểm: Bắt buộc, tối đa 100 ký tự
 * - Kỹ năng: Ít nhất 1 kỹ năng
 * - Giới thiệu: Bắt buộc, 50-2000 ký tự
 * 
 * @author Job Recruitment System
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    description = "Request DTO for creating or updating a job seeking post by candidate",
    example = """
        {
          "title": "Tìm việc Java Developer Senior tại TP.HCM",
          "desiredSalary": 25000000,
          "location": "TP. Hồ Chí Minh",
          "skills": ["Java", "Spring Boot", "PostgreSQL", "Docker", "Microservices"],
          "introduction": "Tôi là lập trình viên Java với 5 năm kinh nghiệm phát triển backend. Thành thạo Spring Boot, Microservices, Docker và các công nghệ cloud. Tìm kiếm cơ hội làm việc tại các công ty công nghệ hàng đầu với môi trường chuyên nghiệp và cơ hội phát triển."
        }
        """
)
public class JobSeekPostRequest {

    /**
     * Tiêu đề tin đăng tìm việc
     * 
     * Ví dụ:
     * - "Tìm việc Java Developer Senior tại TP.HCM"
     * - "Marketing Manager với 5 năm kinh nghiệm"
     * - "Frontend Developer React/Next.js - Remote OK"
     */
    @NotBlank(message = "Tiêu đề tin đăng không được để trống")
    @Size(min = 10, max = 200, message = "Tiêu đề tin đăng phải từ 10 đến 200 ký tự")
    @Schema(
        description = "Seeking post title. " +
                     "Length between 10 and 200 characters.",
        example = "Tìm việc Java Developer Senior tại TP.HCM",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 10,
        maxLength = 200
    )
    private String title;

    /**
     * Mức lương mong muốn (VNĐ)
     * 
     * Quy ước:
     * - 0: Thỏa thuận
     * - > 0: Mức lương cụ thể
     * 
     * Ví dụ:
     * - 0: Thỏa thuận theo năng lực
     * - 15000000: 15 triệu đồng/tháng
     * - 25000000: 25 triệu đồng/tháng
     */
    @NotNull(message = "Mức lương mong muốn không được để trống")
    @Min(value = 0, message = "Mức lương mong muốn phải >= 0 (0 = Thỏa thuận)")
    @Schema(
        description = "Desired salary of the candidate (VND). " +
                     "Value 0 means 'Negotiable'. " +
                     "Value > 0 is the specific desired salary.",
        example = "25000000",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0"
    )
    private Double desiredSalary;

    /**
     * Địa điểm làm việc mong muốn
     * 
     * Có thể là:
     * - Tỉnh/Thành phố: "TP. Hồ Chí Minh", "Hà Nội", "Đà Nẵng"
     * - Khu vực: "Miền Nam", "Miền Bắc"
     * - Chế độ: "Remote", "Hybrid", "Onsite"
     * - Kết hợp: "TP.HCM hoặc Remote"
     */
    @NotBlank(message = "Địa điểm không được để trống")
    @Size(max = 100, message = "Địa điểm không được vượt quá 100 ký tự")
    @Schema(
        description = "Desired work location of the candidate. " +
                     "Can be a specific city/province, region, or work mode (Remote/Hybrid/Onsite). " +
                     "Maximum length: 100 characters.",
        example = "TP. Hồ Chí Minh",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 100
    )
    private String location;

    /**
     * Danh sách kỹ năng của ứng viên
     * 
     * Nên bao gồm:
     * - Ngôn ngữ lập trình: Java, Python, JavaScript, C#, etc.
     * - Frameworks: Spring Boot, React, Angular, .NET, etc.
     * - Công nghệ: Docker, Kubernetes, AWS, Azure, etc.
     * - Soft skills: Agile, Scrum, Teamwork, Communication, etc.
     * 
     * Tối thiểu: 1 kỹ năng
     * Tối đa: Không giới hạn (khuyến nghị 5-15 kỹ năng)
     */
    @NotEmpty(message = "Danh sách kỹ năng không được để trống")
    @Size(min = 1, message = "Phải có ít nhất 1 kỹ năng")
    @Schema(
        description = "Skills list of the candidate. " +
                     "Includes programming languages, frameworks, technologies, and soft skills. " +
                     "At least 1 skill, recommended 5-15 skills.",
        example = "[\"Java\", \"Spring Boot\", \"PostgreSQL\", \"Docker\", \"Microservices\"]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<String> skills;

    /**
     * Giới thiệu bản thân
     * 
     * Nội dung nên bao gồm:
     * - Kinh nghiệm làm việc (số năm, công ty, dự án)
     * - Điểm mạnh, thế mạnh
     * - Mục tiêu nghề nghiệp
     * - Lý do tìm việc mới
     * - Điều kiện làm việc mong muốn
     * 
     * Độ dài: 50-2000 ký tự
     */
    @NotBlank(message = "Giới thiệu bản thân không được để trống")
    @Size(min = 50, max = 2000, message = "Giới thiệu bản thân phải từ 50 đến 2000 ký tự")
    @Schema(
        description = "Introduction of the candidate. " +
                     "Should include experience, strengths, career goals, and job preferences. " +
                     "Length: 50-2000 characters.",
        example = "Tôi là lập trình viên Java với 5 năm kinh nghiệm phát triển backend. " +
                 "Thành thạo Spring Boot, Microservices, Docker và các công nghệ cloud. " +
                 "Đã tham gia nhiều dự án lớn cho khách hàng quốc tế tại FPT Software. " +
                 "Tìm kiếm cơ hội làm việc tại các công ty công nghệ hàng đầu với môi trường chuyên nghiệp và cơ hội phát triển.",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 50,
        maxLength = 2000
    )
    private String introduction;
    /**
     * Ngày hết hạn tin đăng
     * 
     * Quy tắc:
     * - Không bắt buộc, nếu không cung cấp sẽ tự động đặt 30 ngày từ ngày tạo
     * - Phải là ngày trong tương lai
     * - Tối đa 90 ngày từ ngày tạo
     */
    @Schema(
        description = "Expiry date of the job post. " +
                     "Format: yyyy-MM-dd. " +
                     "If not provided, defaults to 30 days from the creation date.",
        example = "2026-02-15",
        type = "string",
        format = "date"
    )
    private LocalDate expiryDate;}
