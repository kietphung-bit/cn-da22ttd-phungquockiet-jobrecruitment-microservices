package com.jobrecruitment.backend.dtos.response;

import java.time.LocalDateTime;

import com.jobrecruitment.backend.enums.CVStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CV Response DTO
 * Section 4.3 - CV Management
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CVResponse {
    
    private Long cvId;
    private Long candidateId;
    private String cvCode;
    private String cvFile;
    private CVStatus cvStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
