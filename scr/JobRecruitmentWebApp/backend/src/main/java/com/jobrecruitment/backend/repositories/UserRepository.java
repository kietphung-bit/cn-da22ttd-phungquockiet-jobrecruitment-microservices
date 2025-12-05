package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByUserCode(String userCode);
    
    boolean existsByUsername(String username);
    
    boolean existsByUserCode(String userCode);
}
