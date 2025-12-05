package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    Optional<Company> findByCompanyCode(String companyCode);
    
    Optional<Company> findByUserUserId(Long userId);
    
    boolean existsByCompanyCode(String companyCode);
    
    boolean existsByCompanyEmail(String companyEmail);
}
