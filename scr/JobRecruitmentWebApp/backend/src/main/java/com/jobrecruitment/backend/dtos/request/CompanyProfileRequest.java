package com.jobrecruitment.backend.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Company Profile Update Request DTO
 * Section 4.2 - Profile Management
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProfileRequest {
    
    @Pattern(regexp = "^[a-zA-Z\\s\\p{L}]+$", message = "Tên công ty chỉ chứa chữ cái và khoảng trắng") // RBHT
    private String companyName;
    
    private String companyDescription;
    
    private String companyAddress;
    
    private String companyWebsite;
    
    @Email(message = "Email công ty phải đúng định dạng") // RBEML
    private String companyEmail;
    
    private String logoURL;
}
