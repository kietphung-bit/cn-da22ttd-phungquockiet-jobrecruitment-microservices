package com.jobrecruitment.backend.dtos.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.jobrecruitment.backend.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CandidateResponse - DTO trả về thông tin ứng viên
 * 
 * Mô tả:
 * - Trả về thông tin hồ sơ ứng viên
 * - Map từ Candidate entity
 * - Không chứa nested User object (chỉ có userId)
 * 
 * Sử dụng:
 * - API GET /api/v1/candidates/profile
 * - API GET /api/v1/candidates/{id}
 * - API PUT /api/v1/candidates/profile
 * 
 * Tham khảo: Section 4.3 - Candidate Module
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {
    
    /**
     * ID ứng viên
     */
    private Long candidateId;
    
    /**
     * ID người dùng liên kết
     * - Tham chiếu đến User.userId
     */
    private Long userId;
    
    /**
     * Mã ứng viên
     * - Format: "UV" + 8 chữ số
     * - Đồng bộ với UserCode
     */
    private String candidateCode;
    
    /**
     * Họ và tên ứng viên
     */
    private String candidateName;
    
    /**
     * Mô tả bản thân
     */
    private String candidateDescription;
    
    /**
     * Giới tính
     * - ENUM: MALE, FEMALE, OTHER
     */
    private Gender candidateGender;
    
    /**
     * Ngày sinh
     */
    private LocalDate candidateBirthdate;
    
    /**
     * Số điện thoại
     */
    private String candidatePhone;
    
    /**
     * Email liên hệ
     */
    private String candidateEmail;
    
    /**
     * Trình độ học vấn
     */
    private String candidateEducation;
    
    /**
     * Kinh nghiệm làm việc
     */
    private String candidateExp;
    
    /**
     * Kỹ năng
     */
    private String candidateSkills;
    
    /**
     * Thời gian tạo hồ sơ
     */
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật gần nhất
     */
    private LocalDateTime updatedAt;
}
