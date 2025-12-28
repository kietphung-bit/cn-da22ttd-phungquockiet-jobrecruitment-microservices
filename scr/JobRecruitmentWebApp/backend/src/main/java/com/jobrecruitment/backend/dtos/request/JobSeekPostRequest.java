package com.jobrecruitment.backend.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 10, max = 200, message = "Tiêu đề phải từ 10 đến 200 ký tự")
    @Schema(
        description = "Tiêu đề tin đăng tìm việc của ứng viên. " +
                     "Nên bao gồm vị trí mong muốn và địa điểm. " +
                     "Độ dài: 10-200 ký tự.",
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
    @Min(value = 0, message = "Mức lương phải >= 0 (0 = Thỏa thuận)")
    @Schema(
        description = "Mức lương mong muốn của ứng viên (VNĐ). " +
                     "Giá trị 0 có nghĩa là 'Thỏa thuận'. " +
                     "Giá trị > 0 là mức lương cụ thể mong muốn.",
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
        description = "Địa điểm làm việc mong muốn của ứng viên. " +
                     "Có thể là tỉnh/thành phố cụ thể, khu vực, hoặc chế độ làm việc (Remote/Hybrid/Onsite). " +
                     "Độ dài tối đa: 100 ký tự.",
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
    @NotEmpty(message = "Phải có ít nhất 1 kỹ năng")
    @Size(min = 1, message = "Phải có ít nhất 1 kỹ năng")
    @Schema(
        description = "Danh sách kỹ năng của ứng viên. " +
                     "Bao gồm ngôn ngữ lập trình, framework, công nghệ và soft skills. " +
                     "Tối thiểu 1 kỹ năng, khuyến nghị 5-15 kỹ năng.",
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
    @Size(min = 50, max = 2000, message = "Giới thiệu phải từ 50 đến 2000 ký tự")
    @Schema(
        description = "Giới thiệu bản thân của ứng viên. " +
                     "Nên bao gồm kinh nghiệm, điểm mạnh, mục tiêu nghề nghiệp và mong muốn về công việc. " +
                     "Độ dài: 50-2000 ký tự.",
        example = "Tôi là lập trình viên Java với 5 năm kinh nghiệm phát triển backend. " +
                 "Thành thạo Spring Boot, Microservices, Docker và các công nghệ cloud. " +
                 "Đã tham gia nhiều dự án lớn cho khách hàng quốc tế tại FPT Software. " +
                 "Tìm kiếm cơ hội làm việc tại các công ty công nghệ hàng đầu với môi trường chuyên nghiệp và cơ hội phát triển.",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 50,
        maxLength = 2000
    )
    private String introduction;
}
