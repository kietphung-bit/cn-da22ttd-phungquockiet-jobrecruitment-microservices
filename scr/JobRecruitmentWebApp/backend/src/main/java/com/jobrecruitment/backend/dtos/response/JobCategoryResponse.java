package com.jobrecruitment.backend.dtos.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Job Category Response DTO
 * Section 4.4 - Admin Module - Category Management
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobCategoryResponse {
    
    private Integer jcId;
    private String jcName;
    private String jcDescription;
    private Double jcBaseSalary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
