package com.jobrecruitment.backend.dtos.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.jobrecruitment.backend.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Candidate Response DTO
 * Section 4.3 - Candidate Module
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {
    
    private Long candidateId;
    private Long userId;
    private String candidateCode;
    private String candidateName;
    private String candidateDescription;
    private Gender candidateGender;
    private LocalDate candidateBirthdate;
    private String candidatePhone;
    private String candidateEmail;
    private String candidateEducation;
    private String candidateExp;
    private String candidateSkills;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
