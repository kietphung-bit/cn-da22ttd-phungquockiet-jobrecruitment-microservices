package com.jobrecruitment.backend.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Company Registration Request DTO
 * Section 4.5.C - Employer Registration Flow
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRegisterRequest {
    
    // User credentials
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email phải đúng định dạng")
    private String username; // Registration email
    
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;
    
    // Company profile
    @NotBlank(message = "Tên công ty không được để trống")
    @Pattern(regexp = "^[a-zA-Z\\s\\p{L}]+$", message = "Tên công ty chỉ chứa chữ cái và khoảng trắng") // RBHT
    private String companyName;
    
    private String companyDescription;
    
    private String companyAddress;
    
    private String companyWebsite;
    
    @Email(message = "Email công ty phải đúng định dạng") // RBEML
    private String companyEmail;
    
    private String logoURL;
}
