package com.jobrecruitment.backend.services.impl;

import com.jobrecruitment.backend.dtos.request.JobRequest;
import com.jobrecruitment.backend.dtos.response.JobResponse;
import com.jobrecruitment.backend.entities.Company;
import com.jobrecruitment.backend.entities.Job;
import com.jobrecruitment.backend.entities.JobCategory;
import com.jobrecruitment.backend.entities.User;
import com.jobrecruitment.backend.enums.JobStatus;
import com.jobrecruitment.backend.exceptions.ResourceNotFoundException;
import com.jobrecruitment.backend.exceptions.ValidationException;
import com.jobrecruitment.backend.mappers.JobMapper;
import com.jobrecruitment.backend.repositories.CompanyRepository;
import com.jobrecruitment.backend.repositories.JobCategoryRepository;
import com.jobrecruitment.backend.repositories.JobRepository;
import com.jobrecruitment.backend.repositories.UserRepository;
import com.jobrecruitment.backend.services.JobService;
import com.jobrecruitment.backend.utils.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final UserRepository userRepository;
    private final CodeGenerator codeGenerator;
    private final JobMapper jobMapper;

    @Override
    @Transactional
    public JobResponse createJob(JobRequest request, String username) {
        // Get authenticated user and their company
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Company company = companyRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));

        // Validate StartDate <= EndDate (RBNT rule)
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new ValidationException("Start date cannot be after end date");
        }

        // Get job category
        JobCategory jobCategory = jobCategoryRepository.findById(request.getJcId())
                .orElseThrow(() -> new ResourceNotFoundException("Job category not found"));

        // Generate unique JobCode
        String jobCode = codeGenerator.generateJobCode(code -> jobRepository.existsByJobCode(code));

        // Create job entity
        Job job = new Job();
        job.setCompany(company);
        job.setJobCategory(jobCategory);
        job.setJobCode(jobCode);
        job.setJobTitle(request.getJobTitle());
        job.setJobDescription(request.getJobDescription());
        job.setJobRequirement(request.getJobRequirement());
        job.setJobSalary(request.getJobSalary());
        job.setJobLocation(request.getJobLocation());
        job.setStartDate(request.getStartDate());
        job.setEndDate(request.getEndDate());
        job.setMaxCandidates(request.getMaxCandidates());
        
        // Set job status: WAIT if startDate is in the future, otherwise PENDING
        if (request.getStartDate().isAfter(LocalDate.now())) {
            job.setJobStatus(JobStatus.WAIT);
        } else {
            job.setJobStatus(JobStatus.PENDING);
        }

        Job savedJob = jobRepository.save(job);
        return jobMapper.toResponse(savedJob);
    }

    @Override
    @Transactional
    public JobResponse updateJob(Long jobId, JobRequest request, String username) {
        // Get job and validate ownership
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        
        validateJobOwnership(job, username);

        // Validate StartDate <= EndDate (RBNT rule)
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new ValidationException("Start date cannot be after end date");
        }

        // Get job category if changed
        if (!job.getJobCategory().getJcId().equals(request.getJcId())) {
            JobCategory jobCategory = jobCategoryRepository.findById(request.getJcId())
                    .orElseThrow(() -> new ResourceNotFoundException("Job category not found"));
            job.setJobCategory(jobCategory);
        }

        // Update job fields
        jobMapper.updateEntityFromRequest(job, request);
        
        Job updatedJob = jobRepository.save(job);
        return jobMapper.toResponse(updatedJob);
    }

    @Override
    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return jobMapper.toResponse(job);
    }

    @Override
    public List<JobResponse> getAllJobs(JobStatus status) {
        List<Job> jobs;
        if (status != null) {
            jobs = jobRepository.findByJobStatus(status);
        } else {
            jobs = jobRepository.findAll();
        }
        return jobs.stream()
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> getJobsByCompany(Long companyId) {
        // Validate company exists
        companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        
        List<Job> jobs = jobRepository.findByCompanyCompanyId(companyId);
        return jobs.stream()
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> getMyJobs(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Company company = companyRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
        
        List<Job> jobs = jobRepository.findByCompanyCompanyId(company.getCompanyId());
        return jobs.stream()
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobResponse updateJobStatus(Long jobId, JobStatus newStatus, String username) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        
        validateJobOwnership(job, username);
        
        job.setJobStatus(newStatus);
        Job updatedJob = jobRepository.save(job);
        return jobMapper.toResponse(updatedJob);
    }

    @Override
    @Transactional
    public void deleteJob(Long jobId, String username) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        
        validateJobOwnership(job, username);
        
        // Soft delete: set status to HIDDEN
        job.setJobStatus(JobStatus.HIDDEN);
        jobRepository.save(job);
    }

    @Override
    public List<JobResponse> searchJobs(String keyword) {
        List<Job> jobs = jobRepository.searchJobs(keyword);
        return jobs.stream()
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> filterBySalary(Double minSalary, Double maxSalary) {
        if (minSalary < 0 || maxSalary < 0) {
            throw new ValidationException("Salary values must be non-negative");
        }
        if (minSalary > maxSalary) {
            throw new ValidationException("Minimum salary cannot be greater than maximum salary");
        }
        
        List<Job> jobs = jobRepository.findBySalaryRange(minSalary, maxSalary);
        return jobs.stream()
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> getJobsByCategory(Integer jcid) {
        // Validate category exists
        jobCategoryRepository.findById(jcid)
                .orElseThrow(() -> new ResourceNotFoundException("Job category not found"));
        
        List<Job> jobs = jobRepository.findByJobCategoryJcId(jcid);
        return jobs.stream()
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to validate job ownership
     */
    private void validateJobOwnership(Job job, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Company company = companyRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
        
        if (!job.getCompany().getCompanyId().equals(company.getCompanyId())) {
            throw new AccessDeniedException("You can only modify your own jobs");
        }
    }
}
