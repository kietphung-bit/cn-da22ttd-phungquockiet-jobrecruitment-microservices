package com.jobrecruitment.backend.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.jobrecruitment.backend.enums.SeekingPostStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SeekingPost - Entity đại diện cho bảng seeking_posts (Tin tìm việc của ứng viên)
 * 
 * Mô tả:
 * - Tin đăng tìm việc do ứng viên tạo (Reverse Recruitment)
 * - Nhà tuyển dụng có thể tìm kiếm và xem thông tin ứng viên
 * - Hỗ trợ privacy: Guest xem masked data, Employer xem full data
 * 
 * Quan hệ:
 * - Many-to-One với Candidate (1 ứng viên có nhiều tin đăng)
 * 
 * Quy tắc nghiệp vụ:
 * - Tin đăng mới tự động có status=ACTIVE (Section 4.2)
 * - Mã tin đăng: "BV" + 8 chữ số (Section 4.5.B)
 * - Một ứng viên chỉ có 1 tin ACTIVE tại một thời điểm
 * 
 * Privacy Rules (Section 4.2):
 * - Guest/Candidate: Xem masked name, không xem contact
 * - Employer (DN): Xem full name + contact info
 * 
 * Tham khảo: Section 3.1 (Table 10) - SeekingPost
 */
@Entity
@Data
@Table(name = "seeking_posts")
@NoArgsConstructor
@AllArgsConstructor
public class SeekingPost {
    /**
     * Primary Key - ID tự động tăng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long skPostId;

    /**
     * Quan hệ Many-to-One với Candidate
     * - FetchType.LAZY: Tối ưu hiệu năng
     * - Không cascade: Xóa tin đăng không ảnh hưởng đến ứng viên
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", referencedColumnName = "candidateId", nullable = false)
    @NotNull(message = "Ứng viên không được để trống")
    private Candidate candidate;

    /**
     * Mã tin đăng
     * - Unique constraint: Không trùng lặp
     * - Format: "BV" + 8 chữ số (ví dụ: BV12345678)
     * Tham khảo: Section 4.5.B - Prefix Definitions
     */
    @Column(unique = true, nullable = false, length = 10)
    private String skPostCode;

    /**
     * Tiêu đề tin đăng
     * - Validation: @NotBlank, @Size
     * - Độ dài: 10-200 ký tự
     * - Ví dụ: "Java Developer 3 Years Exp looking for Remote Job"
     */
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 10, max = 200, message = "Tiêu đề phải từ 10 đến 200 ký tự")
    @Column(nullable = false, length = 200)
    private String skPostTitle;

    /**
     * Mức lương mong muốn
     * - Validation: @Min(0)
     * - 0 = Thỏa thuận
     * - > 0 = Mức lương cụ thể (VNĐ)
     */
    @Min(value = 0, message = "Mức lương phải >= 0")
    @Column(nullable = false)
    private Double desiredSalary;

    /**
     * Địa điểm làm việc mong muốn
     * - Validation: @NotBlank
     * - Ví dụ: "TP. Hồ Chí Minh", "Remote", "Hà Nội"
     */
    @NotBlank(message = "Địa điểm không được để trống")
    @Column(nullable = false, length = 100)
    private String desiredLocation;

    /**
     * Danh sách kỹ năng
     * - Column Type: TEXT
     * - Format: JSON array hoặc comma-separated
     * - Ví dụ: "Java,Spring Boot,PostgreSQL,Docker"
     */
    @Column(columnDefinition = "TEXT")
    private String skPostSkills;

    /**
     * Giới thiệu bản thân
     * - Column Type: TEXT (Rich text)
     * - Validation: @NotBlank, @Size
     * - Độ dài: 50-2000 ký tự
     */
    @NotBlank(message = "Giới thiệu không được để trống")
    @Size(min = 50, max = 2000, message = "Giới thiệu phải từ 50 đến 2000 ký tự")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String skPostIntro;

    /**
     * Trạng thái tin đăng
     * - ACTIVE: Đang công khai
     * - HIDDEN: Tạm ẩn
     * - CLOSED: Đã đóng
     * - Default: ACTIVE (Section 4.2)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeekingPostStatus skPostStatus = SeekingPostStatus.ACTIVE;

    /**
     * Ngày hết hạn
     * - Optional: Có thể để null (không giới hạn thời gian)
     */
    @Column
    private LocalDate expiryDate;

    /**
     * Thời gian tạo
     * - Auto-generated: CreationTimestamp
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
