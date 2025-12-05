package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.dtos.request.ApplicationRequest;
import com.jobrecruitment.backend.dtos.request.ApplicationStatusRequest;
import com.jobrecruitment.backend.dtos.response.ApplicationResponse;
import com.jobrecruitment.backend.enums.ApplicationStatus;

import java.util.List;

public interface ApplicationService {
    
    /**
     * Apply to a job (Candidate only)
     * Validates:
     * - Job is ACTIVE
     * - Current date is within job date range (RBNT rule)
     * - CV is ACTIVE
     * - Candidate hasn't already applied
     * Generates unique ApplicationCode (DX + 8 digits)
     */
    ApplicationResponse applyToJob(ApplicationRequest request, String username);
    
    /**
     * Get application by ID
     */
    ApplicationResponse getApplicationById(Long applicationId);
    
    /**
     * Get all applications for a job (Employer only - own jobs)
     */
    List<ApplicationResponse> getApplicationsByJob(Long jobId, String username);
    
    /**
     * Get applications by job and status (Employer only)
     */
    List<ApplicationResponse> getApplicationsByJobAndStatus(Long jobId, ApplicationStatus status, String username);
    
    /**
     * Get my applications (Candidate only)
     */
    List<ApplicationResponse> getMyApplications(String username);
    
    /**
     * Update application status (Employer only - own jobs)
     * Used to approve or reject applications
     */
    ApplicationResponse updateApplicationStatus(Long applicationId, ApplicationStatusRequest request, String username);
}
