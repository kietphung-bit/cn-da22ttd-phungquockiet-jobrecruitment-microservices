package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.CV;
import com.jobrecruitment.backend.enums.CVStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CVRepository extends JpaRepository<CV, Long> {
    
    Optional<CV> findByCvCode(String cvCode);
    
    boolean existsByCvCode(String cvCode);
    
    // Find CVs by candidate
    List<CV> findByCandidateCandidateId(Long candidateId);
    
    // Find active CVs by candidate
    List<CV> findByCandidateCandidateIdAndCvStatus(Long candidateId, CVStatus cvStatus);
}
