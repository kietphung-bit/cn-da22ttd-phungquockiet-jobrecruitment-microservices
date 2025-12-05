package com.jobrecruitment.backend.dtos.request;

import java.time.LocalDate;

import com.jobrecruitment.backend.enums.Gender;
import com.jobrecruitment.backend.validators.WorkingAge;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Candidate Registration Request DTO
 * Section 4.5.C - Candidate Registration Flow
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateRegisterRequest {
    
    // User credentials
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email phải đúng định dạng")
    private String username; // Registration email
    
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;
    
    // Candidate profile
    @NotBlank(message = "Họ và tên không được để trống")
    @Pattern(regexp = "^[a-zA-Z\\s\\p{L}]+$", message = "Họ và tên chỉ chứa chữ cái và khoảng trắng") // RBHT
    private String candidateName;
    
    private String candidateDescription;
    
    private Gender candidateGender; // RBGTH
    
    @WorkingAge // RBNS - Complete validation (past date + age >= 18)
    private LocalDate candidateBirthdate;
    
    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại phải là 10-11 chữ số") // RBSDT
    private String candidatePhone;
    
    @Email(message = "Email phải đúng định dạng") // RBEML
    private String candidateEmail;
    
    private String candidateEducation;
    
    private String candidateExp;
    
    private String candidateSkills;
}
