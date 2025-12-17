package com.jobrecruitment.backend.dtos.response;

import java.time.LocalDateTime;

import com.jobrecruitment.backend.enums.CompanyStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CompanyResponse - DTO trả về thông tin công ty
 * 
 * Mô tả:
 * - Trả về thông tin hồ sơ công ty/nhà tuyển dụng
 * - Map từ Company entity
 * - Không chứa nested User object (chỉ có userId)
 * 
 * Sử dụng:
 * - API GET /api/v1/companies/profile
 * - API GET /api/v1/companies/{id}
 * - API PUT /api/v1/companies/profile
 * 
 * Tham khảo: Section 4.2 - Employer Module
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
    
    /**
     * ID công ty
     */
    private Long companyId;
    
    /**
     * ID người dùng liên kết
     * - Tham chiếu đến User.userId
     */
    private Long userId;
    
    /**
     * Mã công ty
     * - Format: "DN" + 8 chữ số
     * - Đồng bộ với UserCode
     */
    private String companyCode;
    
    /**
     * Tên công ty
     */
    private String companyName;
    
    /**
     * Mô tả công ty
     */
    private String companyDescription;
    
    /**
     * Địa chỉ công ty
     */
    private String companyAddress;
    
    /**
     * Website công ty
     */
    private String companyWebsite;
    
    /**
     * Email liên hệ công ty
     */
    private String companyEmail;
    
    /**
     * URL logo công ty
     */
    private String logoURL;
    
    /**
     * Trạng thái công ty
     * - PENDING: Chờ duyệt
     * - ACTIVE: Đang hoạt động
     * - BLOCKED: Bị khóa
     */
    private CompanyStatus companyStatus;
    
    /**
     * Thời gian tạo hồ sơ
     */
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật gần nhất
     */
    private LocalDateTime updatedAt;
}
