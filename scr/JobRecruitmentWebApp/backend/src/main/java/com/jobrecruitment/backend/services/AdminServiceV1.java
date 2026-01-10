package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.dtos.response.DashboardStatsResponse;
import com.jobrecruitment.backend.dtos.response.UserResponse;
import com.jobrecruitment.backend.enums.CompanyStatus;
import com.jobrecruitment.backend.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * AdminServiceV1 - RESTful Service Interface cho chức năng quản trị hệ thống
 * 
 * Chức năng chính:
 * - Thống kê dashboard
 * - Quản lý người dùng (danh sách, khóa/mở khóa)
 * - Quản lý công ty (duyệt/khóa)
 * - Quản lý công việc (Post-moderation: DELETE/BLOCK vi phạm, KHÔNG pre-approve)
 * - Quản lý SeekingPost (DELETE vi phạm)
 * 
 * Quy tắc nghiệp vụ:
 * - Admin không duyệt trước nội dung (công việc, tin tìm việc)
 * - Admin chỉ xóa/khóa nội dung sau khi có báo cáo hoặc phát hiện vi phạm
 * - Người dùng (Nhà tuyển dụng, Ứng viên) chịu trách nhiệm về độ chính xác và hợp pháp của nội dung
 */
public interface AdminServiceV1 {
    
    /**
     * Lấy thống kê tổng quan cho dashboard
     * 
     * @return DashboardStatsResponse với các chỉ số hệ thống
     */
    DashboardStatsResponse getDashboardStats();
    
    /**
     * Lấy tất cả người dùng (phân trang và lọc theo vai trò)
     * 
     * @param pageable Tham số phân trang
     * @param roleCode Mã vai trò tùy chọn (ADM, DN, UV)
     * @return Trang chứa UserResponse
     */
    Page<UserResponse> getAllUsers(Pageable pageable, String roleCode);
    
    /**
     * Khóa/Tạm khóa tài khoản người dùng
     * 
     * @param userId User ID
     * @return Success message
     */
    String lockUser(Long userId);
    
    /**
     * Mở khóa/Mở tạm khóa tài khoản người dùng
     * 
     * @param userId User ID
     * @return Success message
     */
    String unlockUser(Long userId);
    
    /**
     * Thay đổi trạng thái công ty (Duyệt)
     * 
     * @param companyId Company ID
     * @param newStatus New company status (PENDING, ACTIVE, BLOCKED)
     * @return Success message
     */
    String changeCompanyStatus(Long companyId, CompanyStatus newStatus);
    
    /**
     * Thay đổi trạng thái công việc (Post-moderation: Chỉ DELETE/BLOCK vi phạm)
     * 
     * Post-moderation Model:
     * - Admin KHÔNG duyệt trước (no pre-approval)
     * - Admin CHỈ thay đổi trạng thái để BLOCK/DELETE nội dung vi phạm
     * - Recommended status changes: ACTIVE -> HIDDEN (block), or use deleteJob() for permanent removal
     * 
     * @param jobId Job ID
     * @param newStatus New job status (ACTIVE, CLOSED, HIDDEN - NOT PENDING/APPROVED)
     * @return Success message
     */
    String changeJobStatus(Long jobId, JobStatus newStatus);
    
    /**
     * Toggle trạng thái công việc (ACTIVE <-> HIDDEN)
     * 
     * Chức năng:
     * - Toggle JobStatus: ACTIVE <-> HIDDEN
     * - Dùng để ẩn/hiện tin tuyển dụng nhanh chóng
     * - Admin có thể toggle để quản lý nội dung vi phạm tạm thời
     * 
     * @param jobId Job ID cần toggle
     * @return Success message với trạng thái mới
     */
    String toggleJobStatus(Long jobId);
    
    /**
     * Xóa tin tuyển dụng (Admin - Post-moderation)
     * 
     * Chức năng:
     * - Soft delete: Thay đổi JobStatus thành HIDDEN hoặc CLOSED
     * - Sử dụng khi tin tuyển dụng vi phạm chính sách (scam, offensive content)
     * - Admin có quyền xóa bất kỳ tin tuyển dụng nào
     * 
     * @param jobId Job ID cần xóa
     * @return Success message
     */
    String deleteJob(Long jobId);
    
    /**
     * Xóa tin đăng tìm việc (Admin - Post-moderation)
     * 
     * Chức năng:
     * - Soft delete: Thay đổi SKPostStatus thành HIDDEN hoặc CLOSED
     * - Sử dụng khi tin đăng vi phạm chính sách (fake profile, inappropriate content)
     * - Admin có quyền xóa bất kỳ SeekingPost nào
     * 
     * @param seekingPostId SeekingPost ID cần xóa
     * @return Success message
     */
    String deleteSeekingPost(Long seekingPostId);
    
    /**
     * Lấy tất cả tin đăng tìm việc (Admin)
     * 
     * Chức năng:
     * - Lấy tất cả SeekingPost bao gồm ACTIVE, HIDDEN, CLOSED
     * - Phân trang và sắp xếp theo thời gian tạo mới nhất
     * - Chỉ dành cho Admin để quản lý nội dung
     * 
     * @param pageable Tham số phân trang
     * @return Page chứa JobSeekPostResponse
     */
    Page<com.jobrecruitment.backend.dtos.response.JobSeekPostResponse> getAllSeekingPosts(Pageable pageable);
    
    /**
     * Toggle trạng thái tin đăng tìm việc (ACTIVE <-> HIDDEN)
     * 
     * Chức năng:
     * - Toggle SKPostStatus: ACTIVE <-> HIDDEN
     * - Dùng để ẩn/hiện tin tìm việc nhanh chóng
     * - Admin có thể toggle để quản lý nội dung vi phạm tạm thời
     * 
     * @param seekingPostId SeekingPost ID cần toggle
     * @return Success message với trạng thái mới
     */
    String toggleSeekingPostStatus(Long seekingPostId);
}
