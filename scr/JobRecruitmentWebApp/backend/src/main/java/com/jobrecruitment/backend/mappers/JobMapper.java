package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.request.JobRequest;
import com.jobrecruitment.backend.dtos.response.JobResponse;
import com.jobrecruitment.backend.entities.Job;

/**
 * Job Mapper
 * Converts Job Entity <-> DTO
 */
@Component
public class JobMapper {
    
    /**
     * Convert Job entity to JobResponse DTO
     */
    public JobResponse toResponse(Job job) {
        if (job == null) {
            return null;
        }
        
        return new JobResponse(
            job.getJobId(),
            job.getCompany() != null ? job.getCompany().getCompanyId() : null,
            job.getCompany() != null ? job.getCompany().getCompanyName() : null,
            job.getJobCategory() != null ? job.getJobCategory().getJcId() : null,
            job.getJobCategory() != null ? job.getJobCategory().getJcName() : null,
            job.getJobCode(),
            job.getJobTitle(),
            job.getJobDescription(),
            job.getJobRequirement(),
            job.getJobSalary(),
            job.getJobLocation(),
            job.getStartDate(),
            job.getEndDate(),
            job.getMaxCandidates(),
            job.getJobStatus(),
            job.getCreatedAt(),
            job.getUpdatedAt()
        );
    }
    
    /**
     * Update Job entity from JobRequest
     * Used for job updates
     */
    public void updateEntityFromRequest(Job job, JobRequest request) {
        if (request.getJobTitle() != null) {
            job.setJobTitle(request.getJobTitle());
        }
        if (request.getJobDescription() != null) {
            job.setJobDescription(request.getJobDescription());
        }
        if (request.getJobRequirement() != null) {
            job.setJobRequirement(request.getJobRequirement());
        }
        if (request.getJobSalary() != null) {
            job.setJobSalary(request.getJobSalary());
        }
        if (request.getJobLocation() != null) {
            job.setJobLocation(request.getJobLocation());
        }
        if (request.getStartDate() != null) {
            job.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            job.setEndDate(request.getEndDate());
        }
        if (request.getMaxCandidates() != null) {
            job.setMaxCandidates(request.getMaxCandidates());
        }
        if (request.getJobStatus() != null) {
            job.setJobStatus(request.getJobStatus());
        }
    }
}
