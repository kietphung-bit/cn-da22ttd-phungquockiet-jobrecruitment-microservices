package com.jobrecruitment.backend.dtos.request;

import java.time.LocalDate;

import com.jobrecruitment.backend.enums.Gender;
import com.jobrecruitment.backend.validators.WorkingAge;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CandidateRegisterRequest - DTO nhận dữ liệu đăng ký ứng viên
 * 
 * Mô tả:
 * - Sử dụng trong API POST /api/v1/auth/candidate/register
 * - Tạo User + Candidate profile cùng lúc
 * - CandidateCode tự động generate ("UV" + 8 chữ số)
 * 
 * Validation Rules:
 * - @NotBlank: Trường bắt buộc không được trống
 * - @Email: Phải đúng định dạng email
 * - @Pattern: Kiểm tra format theo regex
 * - @Size(min=6): Mật khẩu tối thiểu 6 ký tự
 * - @WorkingAge: Custom validator (past date + tuổi >= 18)
 * 
 * Business Rules:
 * - RBHT: Họ tên chỉ chữ cái và khoảng trắng
 * - RBEML: Email hợp lệ
 * - RBSDT: Số điện thoại 10-11 chữ số
 * - RBNS: Tuổi >= 18
 * - RBGTH: Gender ENUM (MALE/FEMALE/OTHER)
 * 
 * Tham khảo: Section 4.5.C - Candidate Registration Flow
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateRegisterRequest {
    
    // ==================== USER CREDENTIALS ====================
    /**
     * Email đăng ký (Username)
     * - Validation: @NotBlank, @Email
     * - Quy tắc: RBEML - Phải có @ và domain hợp lệ
     * - Lưu vào User.username
     */
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email phải đúng định dạng")
    private String username; // Registration email
    
    /**
     * Mật khẩu
     * - Validation: @NotBlank, @Size(min=6)
     * - Hash: BCrypt trước khi lưu (Section 4.1)
     * - Lưu vào User.password
     */
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;
    
    // ==================== CANDIDATE PROFILE ====================
    /**
     * Họ và tên ứng viên
     * - Validation: @NotBlank, @Pattern
     * - Quy tắc: RBHT - Chỉ chữ cái và khoảng trắng
     * - Ví dụ: "Nguyễn Văn A", "John Doe"
     */
    @NotBlank(message = "Họ và tên không được để trống")
    @Pattern(regexp = "^[a-zA-Z\\s\\p{L}]+$", message = "Họ và tên chỉ chứa chữ cái và khoảng trắng") // RBHT
    private String candidateName;
    
    /**
     * Mô tả bản thân
     * - Optional: Có thể để trống
     * - Ví dụ: Giới thiệu bản thân, mục tiêu nghề nghiệp...
     */
    private String candidateDescription;
    
    /**
     * Giới tính
     * - Optional: Có thể để trống
     * - Quy tắc: RBGTH - ENUM: MALE, FEMALE, OTHER
     */
    private Gender candidateGender; // RBGTH
    
    /**
     * Ngày sinh
     * - Validation: @WorkingAge (Custom Validator)
     * - Quy tắc: RBNS - Phải là ngày quá khứ + tuổi >= 18
     * - Ví dụ: 1990-01-01
     */
    @WorkingAge // RBNS - Complete validation (past date + age >= 18)
    private LocalDate candidateBirthdate;
    
    /**
     * Số điện thoại
     * - Validation: @Pattern
     * - Quy tắc: RBSDT - 10-11 chữ số
     * - Ví dụ: "0901234567", "84901234567"
     */
    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại phải là 10-11 chữ số") // RBSDT
    private String candidatePhone;
    
    /**
     * Email liên hệ
     * - Validation: @Email
     * - Quy tắc: RBEML - Phải có @ và domain hợp lệ
     * - Optional: Có thể khác email đăng ký
     */
    @Email(message = "Email phải đúng định dạng") // RBEML
    private String candidateEmail;
    
    /**
     * Trình độ học vấn
     * - Optional: Có thể để trống
     * - Ví dụ: "Đại học CNTT", "Thạc sĩ Kinh tế"
     */
    private String candidateEducation;
    
    /**
     * Kinh nghiệm làm việc
     * - Optional: Có thể để trống
     * - Ví dụ: "3 năm Java Developer"
     */
    private String candidateExp;
    
    /**
     * Kỹ năng
     * - Optional: Có thể để trống
     * - Ví dụ: "Java, Spring Boot, MySQL"
     */
    private String candidateSkills;
}
