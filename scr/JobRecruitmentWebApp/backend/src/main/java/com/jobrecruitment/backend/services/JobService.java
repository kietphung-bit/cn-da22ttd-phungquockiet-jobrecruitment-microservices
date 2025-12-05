package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.dtos.request.JobRequest;
import com.jobrecruitment.backend.dtos.response.JobResponse;
import com.jobrecruitment.backend.enums.JobStatus;

import java.util.List;

public interface JobService {
    
    /**
     * Create a new job posting (Employer only)
     * Validates StartDate <= EndDate
     * Generates unique JobCode
     * Status defaults to PENDING or ACTIVE based on business rules
     */
    JobResponse createJob(JobRequest request, String username);
    
    /**
     * Update existing job (Employer only - own jobs)
     * Validates ownership and StartDate <= EndDate
     */
    JobResponse updateJob(Long jobId, JobRequest request, String username);
    
    /**
     * Get job by ID (Public - anyone can view)
     */
    JobResponse getJobById(Long jobId);
    
    /**
     * Get all jobs (Public - with optional status filter)
     */
    List<JobResponse> getAllJobs(JobStatus status);
    
    /**
     * Get jobs by company (Employer can view own jobs)
     */
    List<JobResponse> getJobsByCompany(Long companyId);
    
    /**
     * Get jobs by authenticated employer
     */
    List<JobResponse> getMyJobs(String username);
    
    /**
     * Change job status (Employer only - own jobs)
     * Used to hide/unhide jobs or close them
     */
    JobResponse updateJobStatus(Long jobId, JobStatus newStatus, String username);
    
    /**
     * Delete job (Soft delete - change to HIDDEN)
     */
    void deleteJob(Long jobId, String username);
    
    /**
     * Search jobs by keyword (title or location)
     */
    List<JobResponse> searchJobs(String keyword);
    
    /**
     * Filter jobs by salary range
     */
    List<JobResponse> filterBySalary(Double minSalary, Double maxSalary);
    
    /**
     * Get jobs by category
     */
    List<JobResponse> getJobsByCategory(Integer jcid);
}
