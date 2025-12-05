package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.Job;
import com.jobrecruitment.backend.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    Optional<Job> findByJobCode(String jobCode);
    
    boolean existsByJobCode(String jobCode);
    
    // Find jobs by company
    List<Job> findByCompanyCompanyId(Long companyId);
    
    // Find jobs by company and status
    List<Job> findByCompanyCompanyIdAndJobStatus(Long companyId, JobStatus jobStatus);
    
    // Find active jobs (for candidates to view)
    List<Job> findByJobStatus(JobStatus jobStatus);
    
    // Find jobs by category
    List<Job> findByJobCategoryJcId(Integer jcId);
    
    // Search jobs by title or location
    @Query("SELECT j FROM Job j WHERE " +
           "LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.jobLocation) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Job> searchJobs(@Param("keyword") String keyword);
    
    // Find jobs by salary range
    @Query("SELECT j FROM Job j WHERE j.jobSalary BETWEEN :minSalary AND :maxSalary")
    List<Job> findBySalaryRange(@Param("minSalary") Double minSalary, @Param("maxSalary") Double maxSalary);
}
