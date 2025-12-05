package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.response.CVResponse;
import com.jobrecruitment.backend.entities.CV;

/**
 * CV Mapper
 * Converts CV Entity <-> DTO
 */
@Component
public class CVMapper {
    
    /**
     * Convert CV entity to CVResponse DTO
     */
    public CVResponse toResponse(CV cv) {
        if (cv == null) {
            return null;
        }
        
        return new CVResponse(
            cv.getCvId(),
            cv.getCandidate() != null ? cv.getCandidate().getCandidateId() : null,
            cv.getCvCode(),
            cv.getCvFile(),
            cv.getCvStatus(),
            cv.getCreatedAt(),
            cv.getUpdatedAt()
        );
    }
}
