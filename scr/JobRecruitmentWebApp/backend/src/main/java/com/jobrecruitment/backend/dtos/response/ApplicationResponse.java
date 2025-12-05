package com.jobrecruitment.backend.dtos.response;

import java.time.LocalDateTime;

import com.jobrecruitment.backend.enums.ApplicationStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Application Response DTO
 * Section 4.3 - Apply for Job
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    
    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private Long cvId;
    private String cvCode;
    private String applicationCode;
    private LocalDateTime applyTime;
    private ApplicationStatus applicationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
