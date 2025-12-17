package com.jobrecruitment.backend.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CompanyRegisterRequest - DTO nhận dữ liệu đăng ký nhà tuyển dụng
 * 
 * Mô tả:
 * - Sử dụng trong API POST /api/v1/auth/company/register
 * - Tạo User + Company profile cùng lúc
 * - CompanyCode tự động generate ("DN" + 8 chữ số)
 * - CompanyStatus mặc định: PENDING (chờ Admin duyệt)
 * 
 * Validation Rules:
 * - @NotBlank: Trường bắt buộc không được trống
 * - @Email: Phải đúng định dạng email
 * - @Pattern: Kiểm tra format theo regex
 * - @Size(min=6): Mật khẩu tối thiểu 6 ký tự
 * 
 * Business Rules:
 * - RBHT: Tên công ty chỉ chữ cái và khoảng trắng
 * - RBEML: Email hợp lệ
 * - CompanyStatus: PENDING -> ACTIVE (sau khi Admin duyệt)
 * 
 * Tham khảo: Section 4.5.C - Employer Registration Flow
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRegisterRequest {
    
    // ==================== USER CREDENTIALS ====================
    /**
     * Email đăng ký (Username)
     * - Validation: @NotBlank, @Email
     * - Quy tắc: RBEML - Phải có @ và domain hợp lệ
     * - Lưu vào User.username
     */
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email phải đúng định dạng")
    private String username; // Registration email
    
    /**
     * Mật khẩu
     * - Validation: @NotBlank, @Size(min=6)
     * - Hash: BCrypt trước khi lưu (Section 4.1)
     * - Lưu vào User.password
     */
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;
    
    // ==================== COMPANY PROFILE ====================
    /**
     * Tên công ty
     * - Validation: @NotBlank, @Pattern
     * - Quy tắc: RBHT - Chỉ chữ cái và khoảng trắng
     * - Ví dụ: "Công ty TNHH ABC", "FPT Software"
     */
    @NotBlank(message = "Tên công ty không được để trống")
    @Pattern(regexp = "^[a-zA-Z\\s\\p{L}]+$", message = "Tên công ty chỉ chứa chữ cái và khoảng trắng") // RBHT
    private String companyName;
    
    /**
     * Mô tả công ty
     * - Optional: Có thể để trống
     * - Ví dụ: Giới thiệu, lĩnh vực hoạt động, quy mô...
     */
    private String companyDescription;
    
    /**
     * Địa chỉ công ty
     * - Optional: Có thể để trống
     * - Ví dụ: "123 Nguyễn Văn Linh, Quận 7, TP.HCM"
     */
    private String companyAddress;
    
    /**
     * Website công ty
     * - Optional: Có thể để trống
     * - Ví dụ: "https://www.company.com"
     */
    private String companyWebsite;
    
    /**
     * Email liên hệ công ty
     * - Validation: @Email
     * - Quy tắc: RBEML - Phải có @ và domain hợp lệ
     * - Optional: Có thể khác email đăng ký
     */
    @Email(message = "Email công ty phải đúng định dạng") // RBEML
    private String companyEmail;
    
    /**
     * URL logo công ty
     * - Optional: Có thể để trống
     * - Lưu đường dẫn đến file logo trên server/cloud
     */
    private String logoURL;
}
