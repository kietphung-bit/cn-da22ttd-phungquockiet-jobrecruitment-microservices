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
 * - Duyệt công ty (phê duyệt/khóa)
 * - Duyệt công việc (phê duyệt/từ chối)
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
     * Thay đổi trạng thái công việc (Duyệt/Từ chối)
     * 
     * @param jobId Job ID
     * @param newStatus New job status (PENDING, ACTIVE, REJECTED, CLOSED, HIDDEN)
     * @return Success message
     */
    String changeJobStatus(Long jobId, JobStatus newStatus);
}
