package com.jobrecruitment.backend.services.impl;

import com.jobrecruitment.backend.dtos.request.ApplicationRequest;
import com.jobrecruitment.backend.dtos.request.ApplicationStatusRequest;
import com.jobrecruitment.backend.dtos.response.ApplicationResponse;
import com.jobrecruitment.backend.entities.Application;
import com.jobrecruitment.backend.entities.CV;
import com.jobrecruitment.backend.entities.Candidate;
import com.jobrecruitment.backend.entities.Company;
import com.jobrecruitment.backend.entities.Job;
import com.jobrecruitment.backend.entities.User;
import com.jobrecruitment.backend.enums.ApplicationStatus;
import com.jobrecruitment.backend.enums.CVStatus;
import com.jobrecruitment.backend.enums.JobStatus;
import com.jobrecruitment.backend.exceptions.ResourceNotFoundException;
import com.jobrecruitment.backend.exceptions.ValidationException;
import com.jobrecruitment.backend.mappers.ApplicationMapper;
import com.jobrecruitment.backend.repositories.ApplicationRepository;
import com.jobrecruitment.backend.repositories.CVRepository;
import com.jobrecruitment.backend.repositories.CandidateRepository;
import com.jobrecruitment.backend.repositories.CompanyRepository;
import com.jobrecruitment.backend.repositories.JobRepository;
import com.jobrecruitment.backend.repositories.UserRepository;
import com.jobrecruitment.backend.services.ApplicationService;
import com.jobrecruitment.backend.utils.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final CVRepository cvRepository;
    private final CandidateRepository candidateRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CodeGenerator codeGenerator;
    private final ApplicationMapper applicationMapper;

    @Override
    @Transactional
    public ApplicationResponse applyToJob(ApplicationRequest request, String username) {
        // Get authenticated user and their candidate profile
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        // Get job
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Validation 1: Check if job is ACTIVE (RBNT rule)
        if (job.getJobStatus() != JobStatus.ACTIVE) {
            throw new ValidationException("Job is not active. Cannot apply to this job.");
        }

        // Validation 2: Check if current date is within job date range (RBNT rule)
        LocalDate currentDate = LocalDate.now();
        if (currentDate.isBefore(job.getStartDate()) || currentDate.isAfter(job.getEndDate())) {
            throw new ValidationException(
                "Application date must be within job posting period (" + 
                job.getStartDate() + " to " + job.getEndDate() + ")"
            );
        }

        // Get CV
        CV cv = cvRepository.findById(request.getCvId())
                .orElseThrow(() -> new ResourceNotFoundException("CV not found"));

        // Validation 3: Check if CV belongs to candidate
        if (!cv.getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            throw new ValidationException("CV does not belong to you");
        }

        // Validation 4: Check if CV is ACTIVE (RBCV rule)
        if (cv.getCvStatus() != CVStatus.ACTIVE) {
            throw new ValidationException("CV is not active. Please activate your CV before applying.");
        }

        // Validation 5: Check if candidate has already applied to this job
        if (applicationRepository.existsByJobIdAndCandidateId(job.getJobId(), candidate.getCandidateId())) {
            throw new ValidationException("You have already applied to this job");
        }

        // Generate unique ApplicationCode
        String applicationCode = codeGenerator.generateApplicationCode(code -> applicationRepository.existsByApplicationCode(code));

        // Create application entity
        Application application = new Application();
        application.setJob(job);
        application.setCv(cv);
        application.setApplicationCode(applicationCode);
        application.setApplyTime(LocalDateTime.now());
        application.setApplicationStatus(ApplicationStatus.PENDING);

        Application savedApplication = applicationRepository.save(application);
        return applicationMapper.toResponse(savedApplication);
    }

    @Override
    public ApplicationResponse getApplicationById(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        return applicationMapper.toResponse(application);
    }

    @Override
    public List<ApplicationResponse> getApplicationsByJob(Long jobId, String username) {
        // Get job and validate ownership
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        
        validateJobOwnership(job, username);
        
        List<Application> applications = applicationRepository.findByJobJobId(jobId);
        return applications.stream()
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationResponse> getApplicationsByJobAndStatus(Long jobId, ApplicationStatus status, String username) {
        // Get job and validate ownership
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        
        validateJobOwnership(job, username);
        
        List<Application> applications = applicationRepository.findByJobJobIdAndApplicationStatus(jobId, status);
        return applications.stream()
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationResponse> getMyApplications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
        
        List<Application> applications = applicationRepository.findByCandidateId(candidate.getCandidateId());
        return applications.stream()
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, ApplicationStatusRequest request, String username) {
        // Get application
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        
        // Validate job ownership (only job owner can update application status)
        validateJobOwnership(application.getJob(), username);
        
        // Update status
        application.setApplicationStatus(request.getApplicationStatus());
        
        Application updatedApplication = applicationRepository.save(application);
        return applicationMapper.toResponse(updatedApplication);
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
            throw new AccessDeniedException("You can only manage applications for your own jobs");
        }
    }
}
