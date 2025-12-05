package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    
    Optional<Candidate> findByCandidateCode(String candidateCode);
    
    Optional<Candidate> findByUserUserId(Long userId);
    
    boolean existsByCandidateCode(String candidateCode);
    
    boolean existsByCandidateEmail(String candidateEmail);
}
