package com.jobrecruitment.backend.dtos.request;

import com.jobrecruitment.backend.enums.ApplicationStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ApplicationStatusRequest - DTO nhận dữ liệu cập nhật trạng thái đơn ứng tuyển
 * 
 * Mô tả:
 * - Sử dụng trong API PUT /api/v1/applications/{id}/status
 * - Nhà tuyển dụng (Employer) duyệt đơn ứng tuyển
 * - Chỉ Employer sở hữu tin tuyển dụng mới được cập nhật
 * 
 * Validation Rules:
 * - @NotNull: Không được null
 * 
 * Business Rules:
 * - RBUT: ApplicationStatus - PENDING/APPROVED/REJECTED
 * - PENDING: Đơn mới nộp, chờ duyệt
 * - APPROVED: Đã chấp nhận (ứng viên qua vòng sơ tuyển)
 * - REJECTED: Bị từ chối (không đạt yêu cầu)
 * 
 * Tham khảo: Section 4.2 - Employer reviews applications
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusRequest {
    
    /**
     * Trạng thái đơn ứng tuyển
     * - Validation: @NotNull
     * - Quy tắc: RBUT - ENUM: PENDING, APPROVED, REJECTED
     * - PENDING: Chờ duyệt
     * - APPROVED: Đã chấp nhận
     * - REJECTED: Bị từ chối
     * - Lưu dạng String trong database
     */
    @NotNull(message = "Trạng thái không được để trống")
    private ApplicationStatus applicationStatus; // RBUT: PENDING, APPROVED, REJECTED
}
