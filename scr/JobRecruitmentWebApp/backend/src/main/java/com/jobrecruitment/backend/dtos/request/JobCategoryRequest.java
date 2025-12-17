package com.jobrecruitment.backend.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JobCategoryRequest - DTO nhận dữ liệu tạo/cập nhật ngành nghề
 * 
 * Mô tả:
 * - Sử dụng trong API POST /api/v1/admin/job-categories (tạo mới)
 * - Sử dụng trong API PUT /api/v1/admin/job-categories/{id} (cập nhật)
 * - Chỉ Admin (Role ADM) mới được tạo/cập nhật ngành nghề
 * - Master data: Dùng để phân loại các tin tuyển dụng
 * 
 * Validation Rules:
 * - @NotBlank: Không được trống
 * - @Positive: Phải là số dương > 0
 * 
 * Business Rules:
 * - RBGTN: JcBaseSalary phải > 0 (mức lương tham chiếu)
 * - JcName: Unique constraint (không trùng lặp)
 * 
 * Tham khảo: Section 4.4 - Admin Module - Category Management
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobCategoryRequest {
    
    /**
     * Tên ngành nghề
     * - Validation: @NotBlank
     * - Unique constraint: Không trùng lặp
     * - Ví dụ: "Công nghệ thông tin", "Marketing", "Kinh doanh"
     */
    @NotBlank(message = "Tên danh mục không được để trống")
    private String jcName;
    
    /**
     * Mô tả ngành nghề
     * - Optional: Có thể null
     * - Ví dụ: Chi tiết về ngành nghề, xu hướng...
     */
    private String jcDescription;
    
    /**
     * Mức lương cơ bản (tham chiếu)
     * - Optional: Có thể null
     * - Validation: @Positive (nếu có giá trị)
     * - Quy tắc: RBGTN - Phải > 0
     * - Đơn vị: VND
     * - Dùng làm tham chiếu cho các tin tuyển dụng
     */
    @Positive(message = "Lương cơ bản phải lớn hơn 0") // RBGTN
    private Double jcBaseSalary;
}
