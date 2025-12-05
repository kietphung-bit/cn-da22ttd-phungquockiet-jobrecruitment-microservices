package com.jobrecruitment.backend.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Application Submission Request DTO
 * Section 4.3 - Apply for Job
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {
    
    @NotNull(message = "Job ID không được để trống")
    private Long jobId;
    
    @NotNull(message = "CV ID không được để trống")
    private Long cvId; // Candidate selects which CV to use
}
