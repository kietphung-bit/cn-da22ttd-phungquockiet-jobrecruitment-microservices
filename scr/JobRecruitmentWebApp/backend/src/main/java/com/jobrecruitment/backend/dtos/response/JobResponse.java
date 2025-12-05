package com.jobrecruitment.backend.dtos.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.jobrecruitment.backend.enums.JobStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Job Response DTO
 * Section 4.2 - Job Management
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    
    private Long jobId;
    private Long companyId;
    private String companyName;
    private Integer jcId;
    private String jcName;
    private String jobCode;
    private String jobTitle;
    private String jobDescription;
    private String jobRequirement;
    private Double jobSalary;
    private String jobLocation;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxCandidates;
    private JobStatus jobStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
