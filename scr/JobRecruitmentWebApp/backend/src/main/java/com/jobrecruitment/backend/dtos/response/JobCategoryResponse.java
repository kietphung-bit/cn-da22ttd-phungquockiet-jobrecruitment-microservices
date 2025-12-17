package com.jobrecruitment.backend.dtos.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JobCategoryResponse - DTO trả về thông tin ngành nghề
 * 
 * Mô tả:
 * - Trả về thông tin danh mục ngành nghề
 * - Map từ JobCategory entity
 * - Master data: Dùng cho dropdown, filter...
 * 
 * Sử dụng:
 * - API GET /api/v1/job-categories
 * - API GET /api/v1/job-categories/{id}
 * - API POST /api/v1/admin/job-categories
 * - API PUT /api/v1/admin/job-categories/{id}
 * 
 * Tham khảo: Section 4.4 - Admin Module - Category Management
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobCategoryResponse {
    
    /**
     * ID ngành nghề
     */
    private Integer jcId;
    
    /**
     * Tên ngành nghề
     * - Ví dụ: "Công nghệ thông tin", "Marketing"
     */
    private String jcName;
    
    /**
     * Mô tả ngành nghề
     */
    private String jcDescription;
    
    /**
     * Mức lương cơ bản (tham chiếu)
     * - Đơn vị: VND
     */
    private Double jcBaseSalary;
    
    /**
     * Thời gian tạo ngành nghề
     */
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật gần nhất
     */
    private LocalDateTime updatedAt;
}
