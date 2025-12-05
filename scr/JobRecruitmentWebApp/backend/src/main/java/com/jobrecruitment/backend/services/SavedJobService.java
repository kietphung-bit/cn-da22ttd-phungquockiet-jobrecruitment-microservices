package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.dtos.response.SavedJobResponse;

import java.util.List;

public interface SavedJobService {
    
    /**
     * Save/Bookmark a job (Candidate only)
     */
    SavedJobResponse saveJob(Long jobId, String username);
    
    /**
     * Get all saved jobs by authenticated candidate
     */
    List<SavedJobResponse> getMySavedJobs(String username);
    
    /**
     * Remove saved job (Candidate only)
     */
    void unsaveJob(Long jobId, String username);
    
    /**
     * Check if job is saved by candidate
     */
    boolean isJobSaved(Long jobId, String username);
}
