package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.response.ApplicationResponse;
import com.jobrecruitment.backend.entities.Application;

/**
 * Application Mapper
 * Converts Application Entity <-> DTO
 */
@Component
public class ApplicationMapper {
    
    /**
     * Convert Application entity to ApplicationResponse DTO
     */
    public ApplicationResponse toResponse(Application application) {
        if (application == null) {
            return null;
        }
        
        return new ApplicationResponse(
            application.getApplicationId(),
            application.getJob() != null ? application.getJob().getJobId() : null,
            application.getJob() != null ? application.getJob().getJobTitle() : null,
            application.getCv() != null ? application.getCv().getCvId() : null,
            application.getCv() != null ? application.getCv().getCvCode() : null,
            application.getApplicationCode(),
            application.getApplyTime(),
            application.getApplicationStatus(),
            application.getCreatedAt(),
            application.getUpdatedAt()
        );
    }
}
