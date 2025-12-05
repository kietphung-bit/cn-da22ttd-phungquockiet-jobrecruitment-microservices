package com.jobrecruitment.backend.dtos.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Saved Job Response DTO
 * Section 4.3 - Saved Jobs
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedJobResponse {
    
    private Long sjId;
    private Long candidateId;
    private Long jobId;
    private JobResponse job; // Include job details
    private LocalDateTime savedTime;
}
