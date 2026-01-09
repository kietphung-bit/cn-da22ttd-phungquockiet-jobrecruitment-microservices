package com.jobrecruitment.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jobrecruitment.backend.dtos.request.JobSeekPostRequest;
import com.jobrecruitment.backend.dtos.response.JobSeekPostResponse;
import com.jobrecruitment.backend.enums.SeekingPostStatus;

/**
 * SeekingPostService - Service interface cho Quản lý Tin tìm việc
 * 
 * Chức năng chính:
 * - Ứng viên tạo tin đăng tìm việc (ROLE_UV)
 * - Ứng viên cập nhật tin đăng của mình (Owner only)
 * - Ứng viên thay đổi trạng thái tin đăng (Owner only)
 * - Public/Employer tìm kiếm tin đăng (với privacy logic)
 * - Admin xóa tin đăng vi phạm (ROLE_ADM)
 * 
 * Privacy Logic:
 * - Guest/Candidate: Xem masked data (name="Nguyen Van ***", no contact)
 * - Employer (ROLE_DN): Xem full data (name, phone, email)
 * 
 * Tham khảo:
 * - Section 4.2 - Employer Module (Talent Search)
 * - Section 4.3 - Candidate Module
 * - Section 4.4 - Admin Module (Post-moderation)
 */
public interface SeekingPostService {

    /**
     * Ứng viên tạo tin đăng tìm việc mới
     * 
     * Business Rules:
     * - Tin đăng mới tự động có status=ACTIVE
     * - Nếu ứng viên đã có tin ACTIVE, tự động chuyển sang HIDDEN
     * - Chỉ ứng viên (ROLE_UV) mới có thể tạo tin
     * 
     * @param username Username của ứng viên (từ JWT)
     * @param request Dữ liệu tin đăng
     * @return JobSeekPostResponse
     */
    JobSeekPostResponse createPost(String username, JobSeekPostRequest request);

    /**
     * Ứng viên cập nhật tin đăng của mình
     * 
     * Business Rules:
     * - Chỉ owner mới có thể cập nhật
     * - Không thay đổi status (dùng changeStatus)
     * 
     * @param skPostId ID tin đăng
     * @param username Username của ứng viên (từ JWT)
     * @param request Dữ liệu cập nhật
     * @return JobSeekPostResponse
     */
    JobSeekPostResponse updatePost(Long skPostId, String username, JobSeekPostRequest request);

    /**
     * Ứng viên thay đổi trạng thái tin đăng
     * 
     * Business Rules:
     * - Chỉ owner mới có thể thay đổi
     * - Trạng thái hợp lệ: ACTIVE, HIDDEN, CLOSED
     * 
     * @param skPostId ID tin đăng
     * @param username Username của ứng viên (từ JWT)
     * @param newStatus Trạng thái mới
     * @return JobSeekPostResponse
     */
    JobSeekPostResponse changeStatus(Long skPostId, String username, SeekingPostStatus newStatus);

    /**
     * Tìm kiếm tin đăng công khai (với filter)
     * 
     * Privacy Logic:
     * - Nếu username = null (Guest): Trả về masked data
     * - Nếu role = DN (Employer): Trả về full data
     * - Nếu role = UV (Candidate): Trả về masked data
     * 
     * Filter:
     * - location: Lọc theo địa điểm (LIKE)
     * - skills: Lọc theo kỹ năng (LIKE)
     * 
     * @param username Username của người yêu cầu (null = Guest)
     * @param location Địa điểm (nullable)
     * @param skills Kỹ năng (nullable)
     * @param pageable Phân trang
     * @return Page<JobSeekPostResponse>
     */
    Page<JobSeekPostResponse> searchPosts(String username, String location, String skills, Pageable pageable);

    /**
     * Xem chi tiết một tin đăng
     * 
     * Privacy Logic: Tương tự searchPosts
     * 
     * @param skPostId ID tin đăng
     * @param username Username của người yêu cầu (null = Guest)
     * @return JobSeekPostResponse
     */
    JobSeekPostResponse getPostById(Long skPostId, String username);

    /**
     * Admin xóa tin đăng vi phạm
     * 
     * Business Rules:
     * - Chỉ Admin (ROLE_ADM) mới có quyền
     * - Xóa vĩnh viễn (hard delete)
     * 
     * @param skPostId ID tin đăng
     */
    void deletePost(Long skPostId);

    /**
     * Ứng viên xem danh sách tin đăng của mình
     * 
     * @param username Username của ứng viên (từ JWT)
     * @return Page<JobSeekPostResponse>
     */
    Page<JobSeekPostResponse> getMyPosts(String username, Pageable pageable);
}
