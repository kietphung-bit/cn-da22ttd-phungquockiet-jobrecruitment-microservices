package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.dtos.response.CVResponse;
import com.jobrecruitment.backend.enums.CVStatus;

import java.util.List;

public interface CVService {
    
    /**
     * Create a new CV (Candidate only)
     * Generates unique CVCode (CV + 8 digits)
     */
    CVResponse createCV(String cvFile, String username);
    
    /**
     * Get CV by ID
     */
    CVResponse getCVById(Long cvid);
    
    /**
     * Get all CVs by authenticated candidate
     */
    List<CVResponse> getMyCVs(String username);
    
    /**
     * Update CV status (Candidate only - own CVs)
     */
    CVResponse updateCVStatus(Long cvid, CVStatus newStatus, String username);
    
    /**
     * Delete CV (Soft delete - set to HIDDEN)
     */
    void deleteCV(Long cvid, String username);
    
    /**
     * Get active CVs by candidate
     */
    List<CVResponse> getActiveCVs(Long candidateId);
}
