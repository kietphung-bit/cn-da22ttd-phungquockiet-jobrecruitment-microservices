package com.jobrecruitment.backend.dtos.request;

import java.time.LocalDate;

import com.jobrecruitment.backend.enums.Gender;
import com.jobrecruitment.backend.validators.WorkingAge;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Candidate Profile Update Request DTO
 * Section 4.3 - Profile Management
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfileRequest {
    
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
