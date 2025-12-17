package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CandidateRepository - Repository interface cho Candidate entity
 * 
 * Mô tả:
 * - Cung cấp các phương thức truy vấn database cho Candidate
 * - One-to-One với User: Tìm theo userId
 * - Custom methods: Tìm kiếm theo candidateCode, email
 * 
 * Tham khảo: Section 4.3 - Candidate Module
 */
@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    
    /**
     * Tìm kiếm Candidate dựa trên candidateCode
     * - Sử dụng trong: Profile retrieval, CV management
     * - Query: WHERE candidateCode = :candidateCode
     * @param candidateCode Mã ứng viên (UV + 8 số)
     * @return Optional<Candidate> - Có thể empty nếu không tìm thấy
     */
    Optional<Candidate> findByCandidateCode(String candidateCode);
    
    /**
     * Tìm kiếm Candidate dựa trên userId
     * - Sử dụng trong: Profile retrieval sau khi authenticate
     * - Query: JOIN với User WHERE user.userId = :userId
     * - One-to-One relationship
     * @param userId ID của User liên kết
     * @return Optional<Candidate> - Có thể empty nếu không tìm thấy
     */
    Optional<Candidate> findByUserUserId(Long userId);
    
    /**
     * Kiểm tra candidateCode đã tồn tại chưa
     * - Sử dụng trong: Code generation validation
     * - Query: SELECT COUNT(*) > 0 WHERE candidateCode = :candidateCode
     * @param candidateCode Mã cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     */
    boolean existsByCandidateCode(String candidateCode);
    
    /**
     * Kiểm tra email đã tồn tại chưa
     * - Sử dụng trong: Registration/Profile update validation
     * - Query: SELECT COUNT(*) > 0 WHERE candidateEmail = :candidateEmail
     * @param candidateEmail Email cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     */
    boolean existsByCandidateEmail(String candidateEmail);
}
