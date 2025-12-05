package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.Application;
import com.jobrecruitment.backend.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    Optional<Application> findByApplicationCode(String applicationCode);
    
    boolean existsByApplicationCode(String applicationCode);
    
    // Find applications by job
    List<Application> findByJobJobId(Long jobId);
    
    // Find applications by job and status
    List<Application> findByJobJobIdAndApplicationStatus(Long jobId, ApplicationStatus status);
    
    // Find applications by candidate (via CV)
    @Query("SELECT a FROM Application a WHERE a.cv.candidate.candidateId = :candidateId")
    List<Application> findByCandidateId(@Param("candidateId") Long candidateId);
    
    // Check if candidate already applied to job
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM Application a WHERE a.job.jobId = :jobId AND a.cv.candidate.candidateId = :candidateId")
    boolean existsByJobIdAndCandidateId(@Param("jobId") Long jobId, @Param("candidateId") Long candidateId);
    
    // Find application by job and CV
    Optional<Application> findByJobJobIdAndCvCvId(Long jobId, Long cvId);
}
