package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.request.CandidateProfileRequest;
import com.jobrecruitment.backend.dtos.response.CandidateResponse;
import com.jobrecruitment.backend.entities.Candidate;

/**
 * Candidate Mapper
 * Converts Candidate Entity <-> DTO
 */
@Component
public class CandidateMapper {
    
    /**
     * Convert Candidate entity to CandidateResponse DTO
     */
    public CandidateResponse toResponse(Candidate candidate) {
        if (candidate == null) {
            return null;
        }
        
        return new CandidateResponse(
            candidate.getCandidateId(),
            candidate.getUser() != null ? candidate.getUser().getUserId() : null,
            candidate.getCandidateCode(),
            candidate.getCandidateName(),
            candidate.getCandidateDescription(),
            candidate.getCandidateGender(),
            candidate.getCandidateBirthdate(),
            candidate.getCandidatePhone(),
            candidate.getCandidateEmail(),
            candidate.getCandidateEducation(),
            candidate.getCandidateExp(),
            candidate.getCandidateSkills(),
            candidate.getCreatedAt(),
            candidate.getUpdatedAt()
        );
    }
    
    /**
     * Update Candidate entity from CandidateProfileRequest
     * Used for profile updates
     */
    public void updateEntityFromRequest(Candidate candidate, CandidateProfileRequest request) {
        if (request.getCandidateName() != null) {
            candidate.setCandidateName(request.getCandidateName());
        }
        if (request.getCandidateDescription() != null) {
            candidate.setCandidateDescription(request.getCandidateDescription());
        }
        if (request.getCandidateGender() != null) {
            candidate.setCandidateGender(request.getCandidateGender());
        }
        if (request.getCandidateBirthdate() != null) {
            candidate.setCandidateBirthdate(request.getCandidateBirthdate());
        }
        if (request.getCandidatePhone() != null) {
            candidate.setCandidatePhone(request.getCandidatePhone());
        }
        if (request.getCandidateEmail() != null) {
            candidate.setCandidateEmail(request.getCandidateEmail());
        }
        if (request.getCandidateEducation() != null) {
            candidate.setCandidateEducation(request.getCandidateEducation());
        }
        if (request.getCandidateExp() != null) {
            candidate.setCandidateExp(request.getCandidateExp());
        }
        if (request.getCandidateSkills() != null) {
            candidate.setCandidateSkills(request.getCandidateSkills());
        }
    }
}
