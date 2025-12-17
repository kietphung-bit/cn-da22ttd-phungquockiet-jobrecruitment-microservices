package com.jobrecruitment.backend.services.impl;

import com.jobrecruitment.backend.dtos.response.DashboardStatsResponse;
import com.jobrecruitment.backend.dtos.response.UserResponse;
import com.jobrecruitment.backend.entities.Company;
import com.jobrecruitment.backend.entities.Job;
import com.jobrecruitment.backend.entities.User;
import com.jobrecruitment.backend.enums.CompanyStatus;
import com.jobrecruitment.backend.enums.JobStatus;
import com.jobrecruitment.backend.exceptions.ResourceNotFoundException;
import com.jobrecruitment.backend.mappers.UserMapper;
import com.jobrecruitment.backend.repositories.*;
import com.jobrecruitment.backend.services.AdminServiceV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminServiceV1Impl - Service triển khai chức năng quản trị hệ thống
 * 
 * Chức năng chính:
 * - Thống kê dashboard: Tổng hợp số liệu người dùng, công việc, đơn ứng tuyển
 * - Quản lý người dùng: Phân trang, lọc theo vai trò, khóa/mở khóa tài khoản
 * - Quản lý doanh nghiệp: Thay đổi trạng thái (PENDING/ACTIVE/BLOCKED)
 * - Quản lý công việc: Thay đổi trạng thái (PENDING/ACTIVE/REJECTED/CLOSED/HIDDEN)
 * 
 * Ràng buộc kỹ thuật:
 * - Không sử dụng Native SQL
 * - Pattern thao tác Entity: Find -> Validate -> Set -> Save
 * - Tất cả endpoint yêu cầu vai trò ADM (Admin)
 * 
 * Phụ thuộc:
 * - CompanyRepository: Truy vấn và cập nhật doanh nghiệp
 * - JobRepository: Truy vấn và cập nhật công việc
 * - UserRepository: Truy vấn và cập nhật người dùng
 * - CandidateRepository: Đếm ứng viên
 * - ApplicationRepository: Đếm đơn ứng tuyển theo thời gian
 * - UserMapper: Chuyển đổi Entity -> DTO
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceV1Impl implements AdminServiceV1 {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final ApplicationRepository applicationRepository;
    private final UserMapper userMapper;

    /**
     * Lấy thống kê dashboard cho Admin
     * 
     * Chức năng:
     * - Tính toán ranh giới thời gian: Hôm nay (startOfToday), Tháng này (startOfMonth)
     * - Thống kê người dùng: Tổng số, ứng viên, nhà tuyển dụng (theo trạng thái)
     * - Thống kê công việc: Tổng số, theo trạng thái (ACTIVE/PENDING/CLOSED/HIDDEN)
     * - Thống kê đơn ứng tuyển: Tổng số, hôm nay, tháng này
     * 
     * Quy trình:
     * 1. Tính toán startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
     * 2. Tính toán startOfMonth = LocalDate.now().withDayOfMonth(1)
     * 3. Đếm tổng số User, Candidate, Company từ các Repository
     * 4. Đếm Company theo trạng thái: companyRepository.countByCompanyStatus()
     * 5. Đếm Job theo trạng thái: jobRepository.countByJobStatus()
     * 6. Đếm Application theo thời gian: applicationRepository.countByApplyTimeBetween()
     * 7. Build và trả về DashboardStatsResponse
     * 
     * @return DashboardStatsResponse - Chứa tất cả thống kê hệ thống
     */
    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        log.info("Fetching dashboard statistics");
        
        // Calculate time boundaries for today and this month
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime startOfMonth = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime now = LocalDateTime.now();
        
        // Build comprehensive statistics
        DashboardStatsResponse stats = DashboardStatsResponse.builder()
                // User Statistics
                .totalUsers(userRepository.count())
                .totalCandidates(candidateRepository.count())
                .totalEmployers(companyRepository.count())
                .activeEmployers(companyRepository.countByCompanyStatus(CompanyStatus.ACTIVE))
                .pendingEmployers(companyRepository.countByCompanyStatus(CompanyStatus.PENDING))
                .blockedEmployers(companyRepository.countByCompanyStatus(CompanyStatus.BLOCKED))
                
                // Job Statistics
                .totalJobs(jobRepository.count())
                .activeJobs(jobRepository.countByJobStatus(JobStatus.ACTIVE))
                .pendingJobs(jobRepository.countByJobStatus(JobStatus.PENDING))
                .closedJobs(jobRepository.countByJobStatus(JobStatus.CLOSED))
                .hiddenJobs(jobRepository.countByJobStatus(JobStatus.HIDDEN))
                
                // Application Statistics (Time-based)
                .totalApplications(applicationRepository.count())
                .applicationsToday(applicationRepository.countByApplyTimeBetween(startOfToday, now))
                .applicationsThisMonth(applicationRepository.countByApplyTimeBetween(startOfMonth, now))
                .build();
        
        log.info("Dashboard stats retrieved - Total Users: {}, Total Jobs: {}", stats.getTotalUsers(), stats.getTotalJobs());
        return stats;
    }

    /**
     * Lấy danh sách người dùng (phân trang, lọc theo vai trò)
     * 
     * Chức năng:
     * - Lấy tất cả người dùng hoặc lọc theo roleCode (ADM/DN/UV)
     * - Phân trang thủ công (manual pagination) vì lọc bằng Stream
     * - Chuyển đổi Entity -> DTO bằng UserMapper
     * 
     * Quy trình:
     * 1. Lấy tất cả User từ userRepository.findAll()
     * 2. Nếu có roleCode: Lọc bằng Stream filter theo user.getRole().getRoleCode()
     * 3. Convert Entity -> DTO: allUsers.stream().map(userMapper::toResponse)
     * 4. Tính toán phân trang: start = pageable.getOffset(), end = start + pageSize
     * 5. Cắt danh sách: allResponses.subList(start, end)
     * 6. Trả về PageImpl(pageContent, pageable, totalSize)
     * 
     * @param pageable Tham số phân trang (page, size, sort)
     * @param roleCode Mã vai trò lọc (ADM, DN, UV) - Tùy chọn
     * @return Page<UserResponse> - Danh sách người dùng phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable, String roleCode) {
        log.info("Fetching users - Page: {}, Size: {}, RoleFilter: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), roleCode);
        
        List<User> allUsers;
        
        if (roleCode != null && !roleCode.isEmpty()) {
            // Filter by role code
            allUsers = userRepository.findAll().stream()
                    .filter(user -> user.getRole().getRoleCode().equals(roleCode))
                    .collect(Collectors.toList());
            log.info("Filtered users by role '{}': {} users found", roleCode, allUsers.size());
        } else {
            // Get all users
            allUsers = userRepository.findAll();
            log.info("Retrieved all users: {} total", allUsers.size());
        }
        
        // Convert to responses
        List<UserResponse> allResponses = allUsers.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
        
        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allResponses.size());
        
        List<UserResponse> pageContent = allResponses.subList(
            Math.min(start, allResponses.size()), 
            end
        );
        
        return new PageImpl<>(pageContent, pageable, allResponses.size());
    }

    /**
     * Khóa tài khoản người dùng (chặn đăng nhập)
     * 
     * Chức năng:
     * - Đặt User.locked = true để chặn đăng nhập
     * - Kiểm tra trạng thái hiện tại để tránh khóa lại tài khoản đã khóa
     * 
     * Quy trình:
     * 1. Tìm User theo userId: userRepository.findById(userId)
     * 2. Nếu không tồn tại: Ném ResourceNotFoundException
     * 3. Kiểm tra user.getLocked() == true: Ném IllegalStateException
     * 4. Đặt user.setLocked(true)
     * 5. Lưu: userRepository.save(user)
     * 6. Trả về thông báo thành công
     * 
     * @param userId ID người dùng cần khóa
     * @return String - Thông báo khóa thành công
     * @throws ResourceNotFoundException Nếu không tìm thấy User
     * @throws IllegalStateException Nếu User đã bị khóa trước đó
     */
    @Override
    @Transactional
    public String lockUser(Long userId) {
        log.info("Locking user - UserId: {}", userId);
        
        // Entity manipulation pattern: Find -> Validate -> Set -> Save
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        if (user.getLocked()) {
            throw new IllegalStateException("User is already locked");
        }
        
        user.setLocked(true);
        userRepository.save(user);
        
        log.info("User locked successfully - Username: {}", user.getUsername());
        return String.format("User '%s' has been locked", user.getUsername());
    }

    /**
     * Mở khóa tài khoản người dùng (cho phép đăng nhập)
     * 
     * Chức năng:
     * - Đặt User.locked = false để cho phép đăng nhập lại
     * - Kiểm tra trạng thái hiện tại để tránh mở khóa tài khoản chưa bị khóa
     * 
     * Quy trình:
     * 1. Tìm User theo userId: userRepository.findById(userId)
     * 2. Nếu không tồn tại: Ném ResourceNotFoundException
     * 3. Kiểm tra user.getLocked() == false: Ném IllegalStateException
     * 4. Đặt user.setLocked(false)
     * 5. Lưu: userRepository.save(user)
     * 6. Trả về thông báo thành công
     * 
     * @param userId ID người dùng cần mở khóa
     * @return String - Thông báo mở khóa thành công
     * @throws ResourceNotFoundException Nếu không tìm thấy User
     * @throws IllegalStateException Nếu User chưa bị khóa
     */
    @Override
    @Transactional
    public String unlockUser(Long userId) {
        log.info("Unlocking user - UserId: {}", userId);
        
        // Entity manipulation pattern: Find -> Validate -> Set -> Save
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        if (!user.getLocked()) {
            throw new IllegalStateException("User is not locked");
        }
        
        user.setLocked(false);
        userRepository.save(user);
        
        log.info("User unlocked successfully - Username: {}", user.getUsername());
        return String.format("User '%s' has been unlocked", user.getUsername());
    }

    /**
     * Thay đổi trạng thái doanh nghiệp (kiểm duyệt)
     * 
     * Chức năng:
     * - Thay đổi CompanyStatus để kiểm duyệt doanh nghiệp
     * - Trạng thái: PENDING (Chờ duyệt), ACTIVE (Đang hoạt động), BLOCKED (Bị khóa)
     * - Doanh nghiệp BLOCKED không thể đăng tin tuyển dụng
     * 
     * Quy trình:
     * 1. Tìm Company theo companyId: companyRepository.findById(companyId)
     * 2. Nếu không tồn tại: Ném ResourceNotFoundException
     * 3. Lưu trạng thái cũ: oldStatus = company.getCompanyStatus()
     * 4. Đặt trạng thái mới: company.setCompanyStatus(newStatus)
     * 5. Lưu: companyRepository.save(company)
     * 6. Log và trả về thông báo thay đổi (Old -> New)
     * 
     * @param companyId ID doanh nghiệp cần thay đổi trạng thái
     * @param newStatus Trạng thái mới (PENDING/ACTIVE/BLOCKED)
     * @return String - Thông báo thay đổi trạng thái thành công
     * @throws ResourceNotFoundException Nếu không tìm thấy Company
     */
    @Override
    @Transactional
    public String changeCompanyStatus(Long companyId, CompanyStatus newStatus) {
        log.info("Changing company status - CompanyId: {}, NewStatus: {}", companyId, newStatus);
        
        // Entity manipulation pattern: Find -> Validate -> Set -> Save
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));
        
        CompanyStatus oldStatus = company.getCompanyStatus();
        company.setCompanyStatus(newStatus);
        companyRepository.save(company);
        
        log.info("Company status changed - CompanyCode: {}, Old: {}, New: {}", 
                company.getCompanyCode(), oldStatus, newStatus);
        
        return String.format("Company '%s' status changed from %s to %s", 
                company.getCompanyName(), oldStatus, newStatus);
    }

    /**
     * Thay đổi trạng thái công việc (kiểm duyệt)
     * 
     * Chức năng:
     * - Thay đổi JobStatus để kiểm duyệt tin tuyển dụng
     * - Trạng thái: PENDING (Chờ duyệt), ACTIVE (Đang mở), REJECTED (Bị từ chối), CLOSED (Đã đóng), HIDDEN (Tạm ẩn)
     * - Job PENDING cần Admin duyệt trước khi hiển thị công khai
     * 
     * Quy trình:
     * 1. Tìm Job theo jobId: jobRepository.findById(jobId)
     * 2. Nếu không tồn tại: Ném ResourceNotFoundException
     * 3. Lưu trạng thái cũ: oldStatus = job.getJobStatus()
     * 4. Đặt trạng thái mới: job.setJobStatus(newStatus)
     * 5. Lưu: jobRepository.save(job)
     * 6. Log và trả về thông báo thay đổi (Old -> New)
     * 
     * @param jobId ID công việc cần thay đổi trạng thái
     * @param newStatus Trạng thái mới (PENDING/ACTIVE/REJECTED/CLOSED/HIDDEN)
     * @return String - Thông báo thay đổi trạng thái thành công
     * @throws ResourceNotFoundException Nếu không tìm thấy Job
     */
    @Override
    @Transactional
    public String changeJobStatus(Long jobId, JobStatus newStatus) {
        log.info("Changing job status - JobId: {}, NewStatus: {}", jobId, newStatus);
        
        // Entity manipulation pattern: Find -> Validate -> Set -> Save
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));
        
        JobStatus oldStatus = job.getJobStatus();
        job.setJobStatus(newStatus);
        jobRepository.save(job);
        
        log.info("Job status changed - JobCode: {}, Old: {}, New: {}", 
                job.getJobCode(), oldStatus, newStatus);
        
        return String.format("Job '%s' status changed from %s to %s", 
                job.getJobTitle(), oldStatus, newStatus);
    }
}
