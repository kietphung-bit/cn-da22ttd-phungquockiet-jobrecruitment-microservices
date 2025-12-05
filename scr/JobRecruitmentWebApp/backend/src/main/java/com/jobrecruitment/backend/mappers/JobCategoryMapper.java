package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.request.JobCategoryRequest;
import com.jobrecruitment.backend.dtos.response.JobCategoryResponse;
import com.jobrecruitment.backend.entities.JobCategory;

/**
 * JobCategory Mapper
 * Converts JobCategory Entity <-> DTO
 */
@Component
public class JobCategoryMapper {
    
    /**
     * Convert JobCategory entity to JobCategoryResponse DTO
     */
    public JobCategoryResponse toResponse(JobCategory jobCategory) {
        if (jobCategory == null) {
            return null;
        }
        
        return new JobCategoryResponse(
            jobCategory.getJcId(),
            jobCategory.getJcName(),
            jobCategory.getJcDescription(),
            jobCategory.getJcBaseSalary(),
            jobCategory.getCreatedAt(),
            jobCategory.getUpdatedAt()
        );
    }
    
    /**
     * Create JobCategory entity from JobCategoryRequest
     */
    public JobCategory toEntity(JobCategoryRequest request) {
        if (request == null) {
            return null;
        }
        
        JobCategory jobCategory = new JobCategory();
        jobCategory.setJcName(request.getJcName());
        jobCategory.setJcDescription(request.getJcDescription());
        jobCategory.setJcBaseSalary(request.getJcBaseSalary());
        
        return jobCategory;
    }
    
    /**
     * Update JobCategory entity from JobCategoryRequest
     */
    public void updateEntityFromRequest(JobCategory jobCategory, JobCategoryRequest request) {
        if (request.getJcName() != null) {
            jobCategory.setJcName(request.getJcName());
        }
        if (request.getJcDescription() != null) {
            jobCategory.setJcDescription(request.getJcDescription());
        }
        if (request.getJcBaseSalary() != null) {
            jobCategory.setJcBaseSalary(request.getJcBaseSalary());
        }
    }
}
