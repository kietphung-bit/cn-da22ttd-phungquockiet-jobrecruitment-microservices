package com.jobrecruitment.backend.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ApplicationRequest - DTO nhận dữ liệu nộp đơn ứng tuyển
 * 
 * Mô tả:
 * - Sử dụng trong API POST /api/v1/applications
 * - Ứng viên nộp đơn ứng tuyển vào tin tuyển dụng
 * - ApplicationCode tự động generate ("DX" + 8 chữ số)
 * - ApplicationStatus mặc định: PENDING (chờ nhà tuyển dụng duyệt)
 * 
 * Validation Rules:
 * - @NotNull: Không được null
 * 
 * Business Rules:
 * - RBNT: ApplyTime phải trong khoảng [Job.StartDate, Job.EndDate]
 * - RBUT: ApplicationStatus - PENDING/APPROVED/REJECTED
 * - Một ứng viên chỉ được nộp 1 đơn cho 1 tin (kiểm tra ở Service layer)
 * 
 * Tham khảo: Section 4.3 - Apply for Job
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {
    
    /**
     * ID tin tuyển dụng
     * - Validation: @NotNull
     * - Tham chiếu đến Job.jobId
     * - Kiểm tra Job phải ACTIVE và chưa hết hạn (Service layer)
     */
    @NotNull(message = "Job ID không được để trống")
    private Long jobId;
    
    /**
     * ID CV sử dụng cho đơn ứng tuyển
     * - Validation: @NotNull
     * - Tham chiếu đến CV.cvId
     * - Ứng viên chọn CV cụ thể để nộp đơn
     * - Kiểm tra CV phải thuộc về ứng viên đang đăng nhập (Service layer)
     */
    @NotNull(message = "CV ID không được để trống")
    private Long cvId; // Ứng viên chọn CV cụ thể để nộp đơn
}
