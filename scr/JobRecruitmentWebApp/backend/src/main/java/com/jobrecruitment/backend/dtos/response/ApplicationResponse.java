package com.jobrecruitment.backend.dtos.response;

import java.time.LocalDateTime;

import com.jobrecruitment.backend.enums.ApplicationStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ApplicationResponse - DTO trả về thông tin đơn ứng tuyển
 * 
 * Mô tả:
 * - Trả về thông tin đơn ứng tuyển của ứng viên
 * - Map từ Application entity
 * - Bao gồm jobTitle và cvCode (flatten data)
 * 
 * Sử dụng:
 * - API GET /api/v1/applications
 * - API GET /api/v1/applications/{id}
 * - API POST /api/v1/applications
 * - API PUT /api/v1/applications/{id}/status
 * 
 * Tham khảo: Section 4.3 - Apply for Job
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    
    /**
     * ID đơn ứng tuyển
     */
    private Long applicationId;
    
    /**
     * ID tin tuyển dụng
     * - Tham chiếu đến Job.jobId
     */
    private Long jobId;
    
    /**
     * Tiêu đề tin tuyển dụng
     * - Flatten data từ Job.jobTitle
     */
    private String jobTitle;
    
    /**
     * ID CV sử dụng
     * - Tham chiếu đến CV.cvId
     */
    private Long cvId;
    
    /**
     * Mã CV sử dụng
     * - Flatten data từ CV.cvCode
     * - Format: "CV" + 8 chữ số
     */
    private String cvCode;
    
    /**
     * Đường dẫn file CV
     * - Flatten data từ CV.cvFile
     * - Dùng để download/view CV
     */
    private String cvFile;
    
    /**
     * ID ứng viên
     * - Tham chiếu đến Candidate.candidateId
     * - Lấy từ CV.candidate
     */
    private Long candidateId;
    
    /**
     * Tên ứng viên
     * - Flatten data từ Candidate.candidateName
     * - Lấy từ CV.candidate.candidateName
     */
    private String candidateName;
    
    /**
     * Email ứng viên
     * - Flatten data từ Candidate.candidateEmail
     * - Lấy từ CV.candidate.candidateEmail
     */
    private String candidateEmail;
    
    /**
     * Số điện thoại ứng viên
     * - Flatten data từ Candidate.candidatePhone
     * - Lấy từ CV.candidate.candidatePhone
     */
    private String candidatePhone;
    
    /**
     * Mã đơn ứng tuyển
     * - Format: "DX" + 8 chữ số
     */
    private String applicationCode;
    
    /**
     * Thời gian nộp đơn
     */
    private LocalDateTime applyTime;
    
    /**
     * Trạng thái đơn ứng tuyển
     * - PENDING: Chờ duyệt
     * - APPROVED: Đã chấp nhận
     * - REJECTED: Bị từ chối
     */
    private ApplicationStatus applicationStatus;
    
    /**
     * Thời gian tạo đơn
     */
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật gần nhất
     */
    private LocalDateTime updatedAt;
}
