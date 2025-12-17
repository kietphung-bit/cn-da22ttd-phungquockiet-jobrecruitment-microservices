package com.jobrecruitment.backend.dtos.request;

import java.time.LocalDate;

import com.jobrecruitment.backend.enums.Gender;
import com.jobrecruitment.backend.validators.WorkingAge;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CandidateProfileRequest - DTO nhận dữ liệu cập nhật hồ sơ ứng viên
 * 
 * Mô tả:
 * - Sử dụng trong API PUT /api/v1/candidates/profile
 * - Cập nhật thông tin Candidate profile (không cập nhật User)
 * - Tất cả fields đều optional (chỉ cập nhật fields khác null)
 * 
 * Validation Rules:
 * - @Pattern: Kiểm tra format theo regex
 * - @Email: Phải đúng định dạng email
 * - @WorkingAge: Custom validator (past date + tuổi >= 18)
 * 
 * Business Rules:
 * - RBHT: Họ tên chỉ chữ cái và khoảng trắng
 * - RBEML: Email hợp lệ
 * - RBSDT: Số điện thoại 10-11 chữ số
 * - RBNS: Tuổi >= 18
 * - RBGTH: Gender ENUM (MALE/FEMALE/OTHER)
 * 
 * Tham khảo: Section 4.3 - Profile Management
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfileRequest {
    
    /**
     * Họ và tên ứng viên
     * - Optional: Có thể null (không cập nhật)
     * - Validation: @Pattern (nếu có giá trị)
     * - Quy tắc: RBHT - Chỉ chữ cái và khoảng trắng
     */
    @Pattern(regexp = "^[a-zA-Z\\s\\p{L}]+$", message = "Họ và tên chỉ chứa chữ cái và khoảng trắng") // RBHT
    private String candidateName;
    
    /**
     * Mô tả bản thân
     * - Optional: Có thể null
     */
    private String candidateDescription;
    
    /**
     * Giới tính
     * - Optional: Có thể null
     * - Quy tắc: RBGTH - ENUM: MALE, FEMALE, OTHER
     */
    private Gender candidateGender; // RBGTH
    
    /**
     * Ngày sinh
     * - Optional: Có thể null
     * - Validation: @WorkingAge (nếu có giá trị)
     * - Quy tắc: RBNS - Phải là ngày quá khứ + tuổi >= 18
     */
    @WorkingAge // RBNS - Complete validation (past date + age >= 18)
    private LocalDate candidateBirthdate;
    
    /**
     * Số điện thoại
     * - Optional: Có thể null
     * - Validation: @Pattern (nếu có giá trị)
     * - Quy tắc: RBSDT - 10-11 chữ số
     */
    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại phải là 10-11 chữ số") // RBSDT
    private String candidatePhone;
    
    /**
     * Email liên hệ
     * - Optional: Có thể null
     * - Validation: @Email (nếu có giá trị)
     * - Quy tắc: RBEML - Phải có @ và domain hợp lệ
     */
    @Email(message = "Email phải đúng định dạng") // RBEML
    private String candidateEmail;
    
    /**
     * Trình độ học vấn
     * - Optional: Có thể null
     */
    private String candidateEducation;
    
    /**
     * Kinh nghiệm làm việc
     * - Optional: Có thể null
     */
    private String candidateExp;
    
    /**
     * Kỹ năng
     * - Optional: Có thể null
     */
    private String candidateSkills;
}
