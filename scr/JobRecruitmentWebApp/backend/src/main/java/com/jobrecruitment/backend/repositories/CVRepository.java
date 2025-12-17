package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.CV;
import com.jobrecruitment.backend.enums.CVStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CVRepository - Repository interface cho CV entity
 * 
 * Mô tả:
 * - Cung cấp các phương thức truy vấn database cho CV
 * - ManyToOne với Candidate: Tìm theo candidateId
 * - Filter: Tìm theo status (ACTIVE/HIDDEN)
 * 
 * Tham khảo: Section 4.7 - CV Module
 */
@Repository
public interface CVRepository extends JpaRepository<CV, Long> {
    
    /**
     * Tìm kiếm CV dựa trên cvCode
     * - Query: WHERE cvCode = :cvCode
     * @param cvCode Mã CV (CV + 8 số)
     * @return Optional<CV> - Có thể empty nếu không tìm thấy
     */
    Optional<CV> findByCvCode(String cvCode);
    
    /**
     * Kiểm tra cvCode đã tồn tại chưa
     * - Query: SELECT COUNT(*) > 0 WHERE cvCode = :cvCode
     * @param cvCode Mã cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     */
    boolean existsByCvCode(String cvCode);
    
    /**
     * Tìm kiếm tất cả CV của một Candidate
     * - Sử dụng trong: Candidate xem danh sách CV của mình
     * - Query: WHERE candidate.candidateId = :candidateId
     * @param candidateId ID ứng viên
     * @return List<CV> - Danh sách CV (bao gồm ACTIVE và HIDDEN)
     */
    List<CV> findByCandidateCandidateId(Long candidateId);
    
    /**
     * Tìm kiếm CV của Candidate theo status
     * - Sử dụng trong: Lấy chỉ CV ACTIVE để ứng tuyển
     * - Query: WHERE candidate.candidateId = :candidateId AND cvStatus = :cvStatus
     * @param candidateId ID ứng viên
     * @param cvStatus Trạng thái (ACTIVE/HIDDEN)
     * @return List<CV> - Danh sách CV theo trạng thái
     */
    List<CV> findByCandidateCandidateIdAndCvStatus(Long candidateId, CVStatus cvStatus);
}
