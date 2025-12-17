package com.jobrecruitment.backend.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.jobrecruitment.backend.enums.Gender;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Candidate - Entity đại diện cho bảng candidates (Hồ sơ ứng viên)
 * 
 * Mô tả:
 * - Lưu trữ thông tin chi tiết của ứng viên tìm việc
 * - Quan hệ One-to-One với User (1 User = 1 Candidate profile)
 * - CandidateCode đồng bộ với UserCode (Section 4.5.C)
 * 
 * Quy tắc nghiệp vụ:
 * - RBHT: CandidateName chỉ chứa chữ cái và khoảng trắng
 * - RBSDT: CandidatePhone phải là 10-11 chữ số
 * - RBEML: CandidateEmail phải đúng định dạng email
 * - RBNS: CandidateBirthdate phải trong quá khứ và tuổi >= 18
 * - RBGTH: CandidateGender phải là MALE/FEMALE/OTHER
 * 
 * Tham khảo: Section 4.3 - Candidate Module
 */
@Entity
@Data
@Table(name = "candidates")
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {
    /**
     * Primary Key - ID tự động tăng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long candidateId;

    /**
     * Quan hệ One-to-One với User
     * - Cascade: MERGE, REFRESH, REMOVE (không có PERSIST - User tạo trước)
     * - Khi xóa Candidate -> xóa User tương ứng
     */
    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;

    /**
     * Mã ứng viên
     * - Unique constraint: Không trùng lặp
     * - Format: "UV" + 8 chữ số (ví dụ: UV12345678)
     * - Đồng bộ với UserCode (Section 4.5.C)
     * Tham khảo: Section 4.5.B - Prefix Definitions
     */
    @Column(unique = true, nullable = false, length = 10)
    private String candidateCode;

    /**
     * Họ và tên ứng viên
     * - Validation: @NotBlank, @Pattern
     * - Quy tắc: RBHT - Chỉ chứa chữ cái và khoảng trắng (hỗ trợ tiếng Việt)
     * - Ví dụ: "Nguyễn Văn A", "Trần Thị B"
     */
    @NotBlank(message = "Họ và tên không được để trống")
    @Pattern(regexp = "^[a-zA-Z\\s\\p{L}]+$", message = "Họ và tên không chứa ký tự đặc biệt và số")
    private String candidateName;

    /**
     * Giới thiệu bản thân
     * - Column Type: TEXT (cho phép nội dung dài)
     * - Optional: Có thể để trống
     */
    @Column(columnDefinition = "TEXT")
    private String candidateDescription;

    /**
     * Giới tính
     * - ENUM: MALE (Nam), FEMALE (Nữ), OTHER (Khác)
     * - Quy tắc: RBGTH - Phải khớp với các giá trị ENUM
     * - Lưu dạng String trong database
     */
    @Enumerated(EnumType.STRING)
    private Gender candidateGender;

    /**
     * Ngày sinh
     * - Validation: @Past (phải là ngày trong quá khứ)
     * - Quy tắc: RBNS - Tuổi phải >= 18 (Working Age)
     * - Tính tuổi: CurrentYear - BirthYear >= 18
     */
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate candidateBirthdate;

    /**
     * Số điện thoại
     * - Validation: @Pattern
     * - Quy tắc: RBSDT - Phải là 10-11 chữ số
     * - Ví dụ: "0912345678", "84912345678"
     */
    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại phải là 10-11 chữ số")
    private String candidatePhone;

    /**
     * Email cá nhân
     * - Validation: @Email
     * - Quy tắc: RBEML - Phải có @ và domain hợp lệ
     * - Ví dụ: "candidate@example.com"
     */
    @Email(message = "Email phải đúng định dạng")
    private String candidateEmail;

    /**
     * Trình độ học vấn
     * - Column Type: TEXT (cho phép nội dung dài)
     * - Ví dụ: "Đại học CNTT - ĐH Bách Khoa"
     */
    @Column(columnDefinition = "TEXT")
    private String candidateEducation;

    /**
     * Kinh nghiệm làm việc
     * - Column Type: TEXT (cho phép nội dung dài)
     * - Ví dụ: "3 năm làm Java Developer tại Công ty ABC"
     */
    @Column(columnDefinition = "TEXT")
    private String candidateExp;

    /**
     * Kỹ năng
     * - Column Type: TEXT (cho phép nội dung dài)
     * - Ví dụ: "Java, Spring Boot, ReactJS, MySQL"
     */
    @Column(columnDefinition = "TEXT")
    private String candidateSkills;

    /**
     * Quan hệ One-to-Many với CV
     * - FetchType.LAZY: Chỉ load CVs khi cần
     * - Một ứng viên có thể có nhiều CV (các phiên bản khác nhau)
     */
    @OneToMany(mappedBy = "candidate", fetch = FetchType.LAZY)
    private List<CV> cvs;

    /**
     * Quan hệ One-to-Many với SavedJob
     * - FetchType.LAZY: Chỉ load saved jobs khi cần
     * - Ứng viên có thể lưu nhiều công việc yêu thích
     */
    @OneToMany(mappedBy = "candidate", fetch = FetchType.LAZY)
    private List<SavedJob> savedJobs;

    /**
     * Thời gian tạo hồ sơ (tự động)
     */
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật gần nhất (tự động)
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
