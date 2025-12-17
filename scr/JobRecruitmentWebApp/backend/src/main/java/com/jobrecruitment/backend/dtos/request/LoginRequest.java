package com.jobrecruitment.backend.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LoginRequest - DTO nhận dữ liệu đăng nhập
 * 
 * Mô tả:
 * - Sử dụng trong API POST /api/v1/auth/login
 * - Xác thực người dùng bằng email và mật khẩu
 * - Username thực chất lưu email (theo Section 3.1)
 * 
 * Validation Rules:
 * - @NotBlank: Không được để trống
 * - @Email: Phải đúng định dạng email (có @ và domain)
 * - @Size(min=6): Mật khẩu tối thiểu 6 ký tự
 * 
 * Business Rules:
 * - RBEML: Email phải hợp lệ
 * - Password được hash bằng BCrypt trước khi so sánh (Section 4.1)
 * 
 * Tham khảo: Section 4.5.A - Authentication Flow
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    /**
     * Email đăng nhập
     * - Validation: @NotBlank, @Email
     * - Quy tắc: RBEML - Phải có @ và domain hợp lệ
     * - Lưu trong User.username (Section 3.1)
     */
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email phải đúng định dạng")
    private String username; // Stores email as per Section 3.1
    
    /**
     * Mật khẩu
     * - Validation: @NotBlank, @Size(min=6)
     * - Hash: BCrypt trước khi lưu database (Section 4.1)
     * - Không lưu plain text
     */
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;
}
