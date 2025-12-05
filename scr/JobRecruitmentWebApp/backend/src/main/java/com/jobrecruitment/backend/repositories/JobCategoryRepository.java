package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobCategoryRepository extends JpaRepository<JobCategory, Integer> {
    
    Optional<JobCategory> findByJcName(String jcName);
    
    boolean existsByJcName(String jcName);
}
