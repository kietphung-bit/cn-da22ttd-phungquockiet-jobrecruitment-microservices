package com.jobrecruitment.backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jobrecruitment.backend.entities.SeekingPost;
import com.jobrecruitment.backend.enums.SeekingPostStatus;

/**
 * SeekingPostRepository - Repository cho Tin tìm việc
 * 
 * Chức năng:
 * - CRUD operations cho SeekingPost
 * - Tìm kiếm theo location, skills
 * - Lọc theo status
 * - Kiểm tra tin đăng ACTIVE của ứng viên
 * 
 * Tham khảo: Section 4.2 - Employer Module (Talent Search)
 */
@Repository
public interface SeekingPostRepository extends JpaRepository<SeekingPost, Long> {

    /**
     * Tìm tin đăng theo mã
     * 
     * @param skPostCode Mã tin đăng (BVxxxxxxxx)
     * @return Optional<SeekingPost>
     */
    Optional<SeekingPost> findBySkPostCode(String skPostCode);

    /**
     * Tìm tất cả tin đăng của một ứng viên
     * 
     * @param candidateId ID ứng viên
     * @return List<SeekingPost>
     */
    List<SeekingPost> findByCandidateCandidateId(Long candidateId);

    /**
     * Tìm tin đăng của ứng viên theo status
     * 
     * @param candidateId ID ứng viên
     * @param status Trạng thái tin đăng
     * @return Optional<SeekingPost>
     */
    Optional<SeekingPost> findByCandidateCandidateIdAndSkPostStatus(Long candidateId, SeekingPostStatus status);

    /**
     * Kiểm tra ứng viên có tin đăng ACTIVE không
     * 
     * Business Rule: Một ứng viên chỉ có 1 tin ACTIVE tại một thời điểm
     * 
     * @param candidateId ID ứng viên
     * @return boolean
     */
    boolean existsByCandidateCandidateIdAndSkPostStatus(Long candidateId, SeekingPostStatus status);

    /**
     * Tìm kiếm tin đăng công khai (ACTIVE) với filter
     * 
     * Search Logic:
     * - Chỉ lấy tin ACTIVE
     * - Filter theo location (LIKE)
     * - Filter theo skills (LIKE)
     * - Sắp xếp theo thời gian tạo mới nhất
     * 
     * @param location Địa điểm (null = không filter)
     * @param skills Kỹ năng (null = không filter)
     * @param pageable Phân trang
     * @return Page<SeekingPost>
     */
    @Query("""
        SELECT sp FROM SeekingPost sp
        WHERE sp.skPostStatus = 'ACTIVE'
        AND (:location IS NULL OR sp.desiredLocation LIKE %:location%)
        AND (:skills IS NULL OR sp.skPostSkills LIKE %:skills%)
        ORDER BY sp.createdAt DESC
        """)
    Page<SeekingPost> searchActivePosts(
        @Param("location") String location,
        @Param("skills") String skills,
        Pageable pageable
    );

    /**
     * Tìm kiếm tất cả tin đăng (Admin)
     * 
     * Admin có thể xem tất cả tin đăng bao gồm HIDDEN và CLOSED
     * 
     * @param pageable Phân trang
     * @return Page<SeekingPost>
     */
    Page<SeekingPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Đếm số tin đăng theo status
     * 
     * @param status Trạng thái
     * @return long
     */
    long countBySkPostStatus(SeekingPostStatus status);
}
