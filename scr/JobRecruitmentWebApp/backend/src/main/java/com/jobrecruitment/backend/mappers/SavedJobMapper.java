package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.response.SavedJobResponse;
import com.jobrecruitment.backend.entities.SavedJob;

import lombok.RequiredArgsConstructor;

/**
 * SavedJob Mapper
 * Converts SavedJob Entity <-> DTO
 */
@Component
@RequiredArgsConstructor
public class SavedJobMapper {
    
    private final JobMapper jobMapper;
    
    /**
     * Convert SavedJob entity to SavedJobResponse DTO
     */
    public SavedJobResponse toResponse(SavedJob savedJob) {
        if (savedJob == null) {
            return null;
        }
        
        return new SavedJobResponse(
            savedJob.getSjId(),
            savedJob.getCandidate() != null ? savedJob.getCandidate().getCandidateId() : null,
            savedJob.getJob() != null ? savedJob.getJob().getJobId() : null,
            jobMapper.toResponse(savedJob.getJob()),
            savedJob.getSavedTime()
        );
    }
}
