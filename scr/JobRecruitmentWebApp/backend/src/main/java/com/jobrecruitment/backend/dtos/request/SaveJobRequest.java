package com.jobrecruitment.backend.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SaveJobRequest - DTO nhận dữ liệu lưu tin tuyển dụng
 * 
 * Mô tả:
 * - Sử dụng trong API POST /api/v1/saved-jobs
 * - Ứng viên bookmark tin tuyển dụng để xem lại sau
 * - SavedTime tự động gán = now()
 * 
 * Validation Rules:
 * - @NotNull: Không được null
 * 
 * Business Rules:
 * - Một ứng viên có thể lưu nhiều tin tuyển dụng
 * - Một tin tuyển dụng có thể được nhiều ứng viên lưu
 * - Kiểm tra Job phải ACTIVE (Service layer)
 * 
 * Tham khảo: Section 4.9 - SavedJob Module
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveJobRequest {
    
    /**
     * ID tin tuyển dụng cần lưu
     * - Validation: @NotNull
     * - Tham chiếu đến Job.jobId
     * - Kiểm tra Job phải ACTIVE (Service layer)
     */
    @NotNull(message = "Job ID is required")
    private Long jobId;
}
