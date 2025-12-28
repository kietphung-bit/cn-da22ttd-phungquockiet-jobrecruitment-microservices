package com.jobrecruitment.backend.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ChangePasswordRequest - DTO cho yêu cầu đổi mật khẩu
 * 
 * Endpoint: PATCH /api/v1/auth/change-password
 * 
 * Quy trình:
 * 1. User cung cấp mật khẩu cũ (oldPassword)
 * 2. User nhập mật khẩu mới (newPassword)
 * 3. User xác nhận mật khẩu mới (confirmPassword)
 * 4. Backend verify oldPassword matches BCrypt hash
 * 5. Validate newPassword != oldPassword
 * 6. Validate newPassword == confirmPassword
 * 7. Update password in database (BCrypt hash)
 * 
 * Security Rules:
 * - Old password must be correct
 * - New password must be different from old
 * - New password strength: Min 6 chars
 * - Confirm password must match new password
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    
    /**
     * Mật khẩu hiện tại
     * - Dùng để xác thực identity của user
     * - So sánh với BCrypt hash trong database
     */
    @NotBlank(message = "Mật khẩu cũ không được để trống")
    private String oldPassword;
    
    /**
     * Mật khẩu mới
     * - Phải khác mật khẩu cũ
     * - Tối thiểu 6 ký tự (có thể tăng lên theo security policy)
     */
    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    private String newPassword;
    
    /**
     * Xác nhận mật khẩu mới
     * - Phải khớp với newPassword
     * - Ngăn typo khi nhập mật khẩu
     */
    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
}
