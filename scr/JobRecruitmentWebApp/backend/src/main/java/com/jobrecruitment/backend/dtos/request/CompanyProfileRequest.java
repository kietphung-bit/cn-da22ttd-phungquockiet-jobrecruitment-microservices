package com.jobrecruitment.backend.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CompanyProfileRequest - DTO nhận dữ liệu cập nhật hồ sơ công ty
 * 
 * Mô tả:
 * - Sử dụng trong API PUT /api/v1/companies/profile
 * - Cập nhật thông tin Company profile (không cập nhật User)
 * - Tất cả fields đều optional (chỉ cập nhật fields khác null)
 * 
 * Validation Rules:
 * - @Pattern: Kiểm tra format theo regex
 * - @Email: Phải đúng định dạng email
 * 
 * Business Rules:
 * - RBHT: Tên công ty chỉ chữ cái và khoảng trắng
 * - RBEML: Email hợp lệ
 * 
 * Tham khảo: Section 4.2 - Profile Management
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProfileRequest {
    
    /**
     * Tên công ty
     * - Optional: Có thể null (không cập nhật)
     * - Validation: @Pattern (nếu có giá trị)
     * - Quy tắc: RBHT - Chỉ chữ cái và khoảng trắng
     */
    @Pattern(regexp = "^[a-zA-Z\\s\\p{L}]+$", message = "Tên công ty chỉ chứa chữ cái và khoảng trắng") // RBHT
    private String companyName;
    
    /**
     * Mô tả công ty
     * - Optional: Có thể null
     */
    private String companyDescription;
    
    /**
     * Địa chỉ công ty
     * - Optional: Có thể null
     */
    private String companyAddress;
    
    /**
     * Website công ty
     * - Optional: Có thể null
     */
    private String companyWebsite;
    
    /**
     * Email liên hệ công ty
     * - Optional: Có thể null
     * - Validation: @Email (nếu có giá trị)
     * - Quy tắc: RBEML - Phải có @ và domain hợp lệ
     */
    @Email(message = "Email công ty phải đúng định dạng") // RBEML
    private String companyEmail;
    
    /**
     * URL logo công ty
     * - Optional: Có thể null
     */
    private String logoURL;
}
