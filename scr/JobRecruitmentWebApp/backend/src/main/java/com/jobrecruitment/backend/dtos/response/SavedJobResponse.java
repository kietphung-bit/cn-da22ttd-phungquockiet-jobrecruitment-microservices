package com.jobrecruitment.backend.dtos.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SavedJobResponse - DTO trả về thông tin tin đã lưu
 * 
 * Mô tả:
 * - Trả về danh sách tin tuyển dụng mà ứng viên đã bookmark
 * - Map từ SavedJob entity
 * - Bao gồm nested JobResponse (chi tiết tin tuyển dụng)
 * 
 * Sử dụng:
 * - API GET /api/v1/saved-jobs
 * - API POST /api/v1/saved-jobs
 * - API DELETE /api/v1/saved-jobs/{id}
 * 
 * Tham khảo: Section 4.3 - Saved Jobs
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedJobResponse {
    
    /**
     * ID tin lưu
     */
    private Long sjId;
    
    /**
     * ID ứng viên lưu tin
     * - Tham chiếu đến Candidate.candidateId
     */
    private Long candidateId;
    
    /**
     * ID tin tuyển dụng được lưu
     * - Tham chiếu đến Job.jobId
     */
    private Long jobId;
    
    /**
     * Chi tiết tin tuyển dụng
     * - Nested object: JobResponse
     * - Bao gồm tất cả thông tin tin tuyển dụng
     */
    private JobResponse job; // Include job details
    
    /**
     * Thời gian lưu tin
     */
    private LocalDateTime savedTime;
}
