package com.jobrecruitment.backend.dtos.request;

import com.jobrecruitment.backend.enums.ApplicationStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Application Status Update Request DTO
 * Section 4.2 - Employer reviews applications
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusRequest {
    
    @NotNull(message = "Trạng thái không được để trống")
    private ApplicationStatus applicationStatus; // RBUT: PENDING, APPROVED, REJECTED
}
