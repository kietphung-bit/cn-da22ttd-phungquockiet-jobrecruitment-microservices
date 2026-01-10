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
 * - Public/Employer tìm kiếm tin đăng (với logic bảo mật/riêng tư)
 * - Admin xóa tin đăng vi phạm (ROLE_ADM)
 * 
 * Logic bảo mật/riêng tư:
 * - Guest/Candidate: Xem masked dữ liệu (name="Nguyen Van ***", no contact)
 * - Employer (ROLE_DN): Xem full dữ liệu (name, phone, email)
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
     * @param username Tên đăng nhập của ứng viên (từ JWT)
     * @param request Dữ liệu tin đăng
     * @return JobSeekPostResponse
     */
    JobSeekPostResponse createPost(String username, JobSeekPostRequest request);

    /**
     * Ứng viên cập nhật tin đăng của mình
     * 
     * Quy tắc nghiệp vụ:
     * - Chỉ owner mới có thể cập nhật
     * - Không thay đổi status (dùng changeStatus)
     * 
     * @param skPostId ID tin đăng
     * @param username Tên đăng nhập của ứng viên (từ JWT)
     * @param request Dữ liệu cập nhật
     * @return JobSeekPostResponse
     */
    JobSeekPostResponse updatePost(Long skPostId, String username, JobSeekPostRequest request);

    /**
     * Ứng viên thay đổi trạng thái tin đăng
     * 
     * Quy tắc nghiệp vụ:
     * - Chỉ owner mới có thể thay đổi
     * - Trạng thái hợp lệ: ACTIVE, HIDDEN, CLOSED
     * 
     * @param skPostId ID tin đăng
     * @param username Tên đăng nhập của ứng viên (từ JWT)
     * @param newStatus Trạng thái mới
     * @return JobSeekPostResponse
     */
    JobSeekPostResponse changeStatus(Long skPostId, String username, SeekingPostStatus newStatus);

    /**
     * Tìm kiếm tin đăng công khai (với filter)
     * 
     * Logic bảo mật/riêng tư:
     * - Nếu username = null (Guest): Trả về masked dữ liệu
     * - Nếu role = DN (Employer): Trả về full dữ liệu
     * - Nếu role = UV (Candidate): Trả về masked dữ liệu
     * 
     * Bộ lọc tìm kiếm:
     * - Địa điểm: Lọc theo địa điểm (LIKE)
     * - Kỹ năng: Lọc theo kỹ năng (LIKE)
     * 
     * @param username Tên đăng nhập của người yêu cầu (null = Guest)
     * @param location Địa điểm (nullable)
     * @param skills Kỹ năng (nullable)
     * @param pageable Phân trang
     * @return Page<JobSeekPostResponse>
     */
    Page<JobSeekPostResponse> searchPosts(String username, String location, String skills, Pageable pageable);

    /**
     * Xem chi tiết một tin đăng
     * 
     * Logic bảo mật/riêng tư: Tương tự searchPosts
     * 
     * @param skPostId ID tin đăng
     * @param username Tên đăng nhập của người yêu cầu (null = Guest)
     * @return JobSeekPostResponse
     */
    JobSeekPostResponse getPostById(Long skPostId, String username);

    /**
     * Admin xóa tin đăng vi phạm
     * 
     * Quy tắc nghiệp vụ:
     * - Chỉ Admin (ROLE_ADM) mới có quyền
     * - Xóa vĩnh viễn (hard delete)
     * 
     * @param skPostId ID tin đăng
     */
    void deletePost(Long skPostId);

    /**
     * Ứng viên xóa tin đăng của chính mình
     * 
     * Quy tắc nghiệp vụ:
     * - Chỉ owner mới có quyền xóa
     * - Xóa vĩnh viễn (hard delete)
     * 
     * @param skPostId ID tin đăng
     * @param username Tên đăng nhập của ứng viên (từ JWT)
     */
    void deleteOwnPost(Long skPostId, String username);

    /**
     * Ứng viên xem danh sách tin đăng của mình
     * 
     * @param username Tên đăng nhập của ứng viên (từ JWT)
     * @return Page<JobSeekPostResponse>
     */
    Page<JobSeekPostResponse> getMyPosts(String username, Pageable pageable);
}
