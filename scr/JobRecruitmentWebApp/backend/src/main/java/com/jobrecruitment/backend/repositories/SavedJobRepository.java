package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SavedJobRepository - Repository interface cho SavedJob entity
 * 
 * Mô tả:
 * - Cung cấp các phương thức truy vấn database cho SavedJob
 * - ManyToOne: Candidate và Job
 * - Bookmark feature: Candidate lưu tin tuyển dụng quan tâm
 * 
 * Tham khảo: Section 4.9 - SavedJob Module
 */
@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    
    /**
     * Tìm kiếm tất cả SavedJob của một Candidate
     * - Sử dụng trong: Candidate xem danh sách tin đã lưu
     * - Query: WHERE candidate.candidateId = :candidateId
     * @param candidateId ID ứng viên
     * @return List<SavedJob> - Danh sách tin đã lưu
     */
    List<SavedJob> findByCandidateCandidateId(Long candidateId);
    
    /**
     * Tìm kiếm SavedJob cụ thể
     * - Sử dụng trong: Check xem tin đã lưu chưa, Xóa bookmark
     * - Query: WHERE candidate.candidateId = :candidateId AND job.jobId = :jobId
     * @param candidateId ID ứng viên
     * @param jobId ID tin tuyển dụng
     * @return Optional<SavedJob> - Có thể empty nếu chưa lưu
     */
    Optional<SavedJob> findByCandidateCandidateIdAndJobJobId(Long candidateId, Long jobId);
    
    /**
     * Kiểm tra Job đã được Candidate lưu chưa
     * - Sử dụng trong: UI show icon bookmark, Validation (prevent duplicate)
     * - JPQL: COUNT query
     * @param candidateId ID ứng viên
     * @param jobId ID tin tuyển dụng
     * @return true nếu đã lưu, false nếu chưa
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM SavedJob s WHERE s.candidate.candidateId = :candidateId AND s.job.jobId = :jobId")
    boolean existsByCandidateIdAndJobId(@Param("candidateId") Long candidateId, @Param("jobId") Long jobId);
    
    /**
     * Xóa SavedJob (Unsave/Remove bookmark)
     * - Sử dụng trong: Candidate bỏ lưu tin
     * - Query: DELETE WHERE candidate.candidateId = :candidateId AND job.jobId = :jobId
     * - Lưu ý: Cần @Transactional ở Service layer
     * @param candidateId ID ứng viên
     * @param jobId ID tin tuyển dụng
     */
    void deleteByCandidateCandidateIdAndJobJobId(Long candidateId, Long jobId);
}
