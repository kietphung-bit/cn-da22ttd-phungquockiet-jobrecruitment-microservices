package com.jobrecruitment.backend.services.impl;

import com.jobrecruitment.backend.dtos.response.CVResponse;
import com.jobrecruitment.backend.entities.CV;
import com.jobrecruitment.backend.entities.Candidate;
import com.jobrecruitment.backend.entities.User;
import com.jobrecruitment.backend.enums.CVStatus;
import com.jobrecruitment.backend.exceptions.ResourceNotFoundException;
import com.jobrecruitment.backend.mappers.CVMapper;
import com.jobrecruitment.backend.repositories.CVRepository;
import com.jobrecruitment.backend.repositories.CandidateRepository;
import com.jobrecruitment.backend.repositories.UserRepository;
import com.jobrecruitment.backend.services.CVService;
import com.jobrecruitment.backend.utils.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CVServiceImpl implements CVService {

    private final CVRepository cvRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final CodeGenerator codeGenerator;
    private final CVMapper cvMapper;

    @Override
    @Transactional
    public CVResponse createCV(String cvFile, String username) {
        // Get authenticated user and their candidate profile
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        // Generate unique CVCode
        String cvCode = codeGenerator.generateCVCode(code -> cvRepository.existsByCvCode(code));

        // Create CV entity
        CV cv = new CV();
        cv.setCandidate(candidate);
        cv.setCvCode(cvCode);
        cv.setCvFile(cvFile);
        cv.setCvStatus(CVStatus.ACTIVE);

        CV savedCV = cvRepository.save(cv);
        return cvMapper.toResponse(savedCV);
    }

    @Override
    public CVResponse getCVById(Long cvid) {
        CV cv = cvRepository.findById(cvid)
                .orElseThrow(() -> new ResourceNotFoundException("CV not found"));
        return cvMapper.toResponse(cv);
    }

    @Override
    public List<CVResponse> getMyCVs(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
        
        List<CV> cvs = cvRepository.findByCandidateCandidateId(candidate.getCandidateId());
        return cvs.stream()
                .map(cvMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CVResponse updateCVStatus(Long cvid, CVStatus newStatus, String username) {
        CV cv = cvRepository.findById(cvid)
                .orElseThrow(() -> new ResourceNotFoundException("CV not found"));
        
        validateCVOwnership(cv, username);
        
        cv.setCvStatus(newStatus);
        CV updatedCV = cvRepository.save(cv);
        return cvMapper.toResponse(updatedCV);
    }

    @Override
    @Transactional
    public void deleteCV(Long cvid, String username) {
        CV cv = cvRepository.findById(cvid)
                .orElseThrow(() -> new ResourceNotFoundException("CV not found"));
        
        validateCVOwnership(cv, username);
        
        // Soft delete: set status to HIDDEN
        cv.setCvStatus(CVStatus.HIDDEN);
        cvRepository.save(cv);
    }

    @Override
    public List<CVResponse> getActiveCVs(Long candidateId) {
        candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
        
        List<CV> cvs = cvRepository.findByCandidateCandidateIdAndCvStatus(candidateId, CVStatus.ACTIVE);
        return cvs.stream()
                .map(cvMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to validate CV ownership
     */
    private void validateCVOwnership(CV cv, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
        
        if (!cv.getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            throw new AccessDeniedException("You can only modify your own CVs");
        }
    }
}
