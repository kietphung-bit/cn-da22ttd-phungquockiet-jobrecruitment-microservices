package com.jobrecruitment.backend.dtos.response;

import java.time.LocalDateTime;

import com.jobrecruitment.backend.enums.CVStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CVResponse - DTO trả về thông tin CV
 * 
 * Mô tả:
 * - Trả về thông tin file CV của ứng viên
 * - Map từ CV entity
 * - Chứa đường dẫn file và trạng thái
 * 
 * Sử dụng:
 * - API GET /api/v1/cvs
 * - API GET /api/v1/cvs/{id}
 * - API POST /api/v1/cvs (upload CV)
 * - API PUT /api/v1/cvs/{id}/status
 * 
 * Tham khảo: Section 4.3 - CV Management
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CVResponse {
    
    /**
     * ID CV
     */
    private Long cvId;
    
    /**
     * ID ứng viên sở hữu
     * - Tham chiếu đến Candidate.candidateId
     */
    private Long candidateId;
    
    /**
     * Mã CV
     * - Format: "CV" + 8 chữ số
     */
    private String cvCode;
    
    /**
     * Đường dẫn file CV
     * - Lưu path hoặc URL đến file trên server/cloud
     * - Ví dụ: "/uploads/cvs/cv_12345.pdf"
     */
    private String cvFile;
    
    /**
     * Trạng thái CV
     * - ACTIVE: Đang sử dụng
     * - HIDDEN: Đã ẩn
     */
    private CVStatus cvStatus;
    
    /**
     * Thời gian tạo CV
     */
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật gần nhất
     */
    private LocalDateTime updatedAt;
}
