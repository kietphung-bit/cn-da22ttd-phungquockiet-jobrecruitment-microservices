package com.jobrecruitment.backend.dtos.response;

import java.time.LocalDateTime;

import com.jobrecruitment.backend.enums.CompanyStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Company Response DTO
 * Section 4.2 - Employer Module
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
    
    private Long companyId;
    private Long userId;
    private String companyCode;
    private String companyName;
    private String companyDescription;
    private String companyAddress;
    private String companyWebsite;
    private String companyEmail;
    private String logoURL;
    private CompanyStatus companyStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
