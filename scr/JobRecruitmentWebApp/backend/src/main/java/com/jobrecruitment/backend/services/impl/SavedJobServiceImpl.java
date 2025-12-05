package com.jobrecruitment.backend.services.impl;

import com.jobrecruitment.backend.dtos.response.SavedJobResponse;
import com.jobrecruitment.backend.entities.Candidate;
import com.jobrecruitment.backend.entities.Job;
import com.jobrecruitment.backend.entities.SavedJob;
import com.jobrecruitment.backend.entities.User;
import com.jobrecruitment.backend.exceptions.ResourceNotFoundException;
import com.jobrecruitment.backend.exceptions.ValidationException;
import com.jobrecruitment.backend.mappers.SavedJobMapper;
import com.jobrecruitment.backend.repositories.CandidateRepository;
import com.jobrecruitment.backend.repositories.JobRepository;
import com.jobrecruitment.backend.repositories.SavedJobRepository;
import com.jobrecruitment.backend.repositories.UserRepository;
import com.jobrecruitment.backend.services.SavedJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedJobServiceImpl implements SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final SavedJobMapper savedJobMapper;

    @Override
    @Transactional
    public SavedJobResponse saveJob(Long jobId, String username) {
        // Get authenticated user and their candidate profile
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        // Get job
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Check if already saved
        if (savedJobRepository.existsByCandidateIdAndJobId(candidate.getCandidateId(), jobId)) {
            throw new ValidationException("Job is already saved");
        }

        // Create saved job
        SavedJob savedJob = new SavedJob();
        savedJob.setCandidate(candidate);
        savedJob.setJob(job);
        savedJob.setSavedTime(LocalDateTime.now());

        SavedJob saved = savedJobRepository.save(savedJob);
        return savedJobMapper.toResponse(saved);
    }

    @Override
    public List<SavedJobResponse> getMySavedJobs(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
        
        List<SavedJob> savedJobs = savedJobRepository.findByCandidateCandidateId(candidate.getCandidateId());
        return savedJobs.stream()
                .map(savedJobMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void unsaveJob(Long jobId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        // Check if saved job exists
        SavedJob savedJob = savedJobRepository.findByCandidateCandidateIdAndJobJobId(
                candidate.getCandidateId(), jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved job not found"));

        savedJobRepository.delete(savedJob);
    }

    @Override
    public boolean isJobSaved(Long jobId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        return savedJobRepository.existsByCandidateIdAndJobId(candidate.getCandidateId(), jobId);
    }
}
