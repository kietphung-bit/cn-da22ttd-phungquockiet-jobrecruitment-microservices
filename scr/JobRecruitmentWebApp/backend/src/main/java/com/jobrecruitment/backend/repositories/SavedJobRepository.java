package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    
    // Find saved jobs by candidate
    List<SavedJob> findByCandidateCandidateId(Long candidateId);
    
    // Find specific saved job
    Optional<SavedJob> findByCandidateCandidateIdAndJobJobId(Long candidateId, Long jobId);
    
    // Check if job is already saved by candidate
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM SavedJob s WHERE s.candidate.candidateId = :candidateId AND s.job.jobId = :jobId")
    boolean existsByCandidateIdAndJobId(@Param("candidateId") Long candidateId, @Param("jobId") Long jobId);
    
    // Delete saved job
    void deleteByCandidateCandidateIdAndJobJobId(Long candidateId, Long jobId);
}
