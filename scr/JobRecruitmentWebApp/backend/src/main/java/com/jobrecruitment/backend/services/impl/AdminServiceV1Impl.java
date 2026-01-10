package com.jobrecruitment.backend.services.impl;

import com.jobrecruitment.backend.dtos.response.DashboardStatsResponse;
import com.jobrecruitment.backend.dtos.response.JobSeekPostResponse;
import com.jobrecruitment.backend.dtos.response.UserResponse;
import com.jobrecruitment.backend.entities.Company;
import com.jobrecruitment.backend.entities.Job;
import com.jobrecruitment.backend.entities.SeekingPost;
import com.jobrecruitment.backend.entities.User;
import com.jobrecruitment.backend.enums.CompanyStatus;
import com.jobrecruitment.backend.enums.JobStatus;
import com.jobrecruitment.backend.enums.SeekingPostStatus;
import com.jobrecruitment.backend.exceptions.ResourceNotFoundException;
import com.jobrecruitment.backend.mappers.SeekingPostMapper;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AdminServiceV1Impl - Service triển khai chức năng quản trị hệ thống
 * 
 * Chức năng chính:
 * - Thống kê dashboard: Tổng hợp số liệu người dùng, công việc, đơn ứng tuyển
 * - Quản lý người dùng: Phân trang, lọc theo vai trò, khóa/mở khóa tài khoản
 * - Quản lý doanh nghiệp: Thay đổi trạng thái (PENDING/ACTIVE/BLOCKED)
 * - Quản lý công việc (Post-moderation): DELETE/BLOCK vi phạm (NOT pre-approve)
 * - Quản lý SeekingPost (Post-moderation): DELETE vi phạm
 * 
 * Post-moderation Policy:
 * - Admin does NOT approve content before publication
 * - Admin ONLY removes/blocks violations after publication
 * - Users are fully responsible for content accuracy and legality
 * 
 * Ràng buộc kỹ thuật:
 * - Không sử dụng Native SQL
 * - Pattern thao tác Entity: Find -> Validate -> Set -> Save
 * - Tất cả endpoint yêu cầu vai trò ADM (Admin)
 * 
 * Phụ thuộc:
 * - CompanyRepository: Truy vấn và cập nhật doanh nghiệp
 * - JobRepository: Truy vấn và cập nhật công việc
 * - SeekingPostRepository: Truy vấn và cập nhật tin đăng tìm việc
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
    private final SeekingPostRepository seekingPostRepository;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final ApplicationRepository applicationRepository;
    private final UserMapper userMapper;
    private final SeekingPostMapper seekingPostMapper;

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
        
        // Generate chart data for last 6 months
        List<DashboardStatsResponse.ChartDataPoint> newUsersChart = generateUserGrowthChart();
        List<DashboardStatsResponse.CategoryDataPoint> jobsByCategory = generateJobsByCategoryChart();
        List<DashboardStatsResponse.ChartDataPoint> applicationsChart = generateApplicationsChart();
        
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
                
                // Chart Data
                .newUsersChart(newUsersChart)
                .jobsByCategory(jobsByCategory)
                .applicationsChart(applicationsChart)
                .build();
        
        log.info("Dashboard stats retrieved - Total Users: {}, Total Jobs: {}", stats.getTotalUsers(), stats.getTotalJobs());
        return stats;
    }
    
    /**
     * Tạo dữ liệu biểu đồ tăng trưởng người dùng (6 tháng gần nhất)
     * 
     * @return List<ChartDataPoint> - Danh sách {date, count}
     */
    private List<DashboardStatsResponse.ChartDataPoint> generateUserGrowthChart() {
        List<DashboardStatsResponse.ChartDataPoint> chartData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        // Get all users
        List<User> allUsers = userRepository.findAll();
        
        // Generate data for last 6 months
        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = YearMonth.now().minusMonths(i);
            LocalDateTime startOfMonth = targetMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = targetMonth.atEndOfMonth().atTime(23, 59, 59);
            
            // Count users created in this month
            long count = allUsers.stream()
                    .filter(user -> user.getCreatedAt() != null)
                    .filter(user -> {
                        LocalDateTime createdAt = user.getCreatedAt();
                        return !createdAt.isBefore(startOfMonth) && !createdAt.isAfter(endOfMonth);
                    })
                    .filter(user -> !"ADM".equals(user.getRole().getRoleCode())) // Exclude admins
                    .count();
            
            chartData.add(DashboardStatsResponse.ChartDataPoint.builder()
                    .date(targetMonth.format(formatter))
                    .count(count)
                    .build());
        }
        
        return chartData;
    }
    
    /**
     * Tạo dữ liệu biểu đồ phân bố công việc theo danh mục
     * 
     * @return List<CategoryDataPoint> - Danh sách {name, value}
     */
    private List<DashboardStatsResponse.CategoryDataPoint> generateJobsByCategoryChart() {
        List<DashboardStatsResponse.CategoryDataPoint> chartData = new ArrayList<>();
        
        // Get all jobs
        List<Job> allJobs = jobRepository.findAll();
        
        // Group by category and count
        Map<String, Long> categoryMap = new LinkedHashMap<>();
        for (Job job : allJobs) {
            String categoryName = job.getJobCategory() != null ? job.getJobCategory().getJcName() : "Khác";
            categoryMap.put(categoryName, categoryMap.getOrDefault(categoryName, 0L) + 1);
        }
        
        // Convert to chart data
        for (Map.Entry<String, Long> entry : categoryMap.entrySet()) {
            chartData.add(DashboardStatsResponse.CategoryDataPoint.builder()
                    .name(entry.getKey())
                    .value(entry.getValue())
                    .build());
        }
        
        // Sort by value descending and limit to top 10
        return chartData.stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .collect(Collectors.toList());
    }
    
    /**
     * Tạo dữ liệu biểu đồ xu hướng đơn ứng tuyển (6 tháng gần nhất)
     * 
     * @return List<ChartDataPoint> - Danh sách {date, count}
     */
    private List<DashboardStatsResponse.ChartDataPoint> generateApplicationsChart() {
        List<DashboardStatsResponse.ChartDataPoint> chartData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        // Generate data for last 6 months
        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = YearMonth.now().minusMonths(i);
            LocalDateTime startOfMonth = targetMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = targetMonth.atEndOfMonth().atTime(23, 59, 59);
            
            // Count applications in this month
            long count = applicationRepository.countByApplyTimeBetween(startOfMonth, endOfMonth);
            
            chartData.add(DashboardStatsResponse.ChartDataPoint.builder()
                    .date(targetMonth.format(formatter))
                    .count(count)
                    .build());
        }
        
        return chartData;
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
            // Get all users EXCLUDING admins (ROLE_ADM)
            allUsers = userRepository.findAll().stream()
                    .filter(user -> !"ADM".equals(user.getRole().getRoleCode()))
                    .collect(Collectors.toList());
            log.info("Retrieved all users (excluding admins): {} total", allUsers.size());
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
     * Khóa/Mở khóa tài khoản người dùng (toggle lock status)
     * 
     * Chức năng:
     * - Toggle User.locked: true <-> false
     * - Nếu đang ACTIVE -> đặt BLOCKED (locked = true)
     * - Nếu đang BLOCKED -> đặt ACTIVE (locked = false)
     * 
     * Quy trình:
     * 1. Tìm User theo userId: userRepository.findById(userId)
     * 2. Nếu không tồn tại: Ném ResourceNotFoundException
     * 3. Toggle: user.setLocked(!user.getLocked())
     * 4. Lưu: userRepository.save(user)
     * 5. Trả về thông báo thành công
     * 
     * @param userId ID người dùng cần khóa/mở
     * @return String - Thông báo khóa/mở thành công
     * @throws ResourceNotFoundException Nếu không tìm thấy User
     */
    @Override
    @Transactional
    public String lockUser(Long userId) {
        log.info("Toggling lock status for user - UserId: {}", userId);
        
        // Entity manipulation pattern: Find -> Toggle -> Save
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Toggle lock status
        boolean newLockStatus = !user.getLocked();
        user.setLocked(newLockStatus);
        userRepository.save(user);
        
        String action = newLockStatus ? "locked" : "unlocked";
        log.info("User {} successfully - Username: {}", action, user.getUsername());
        return String.format("User '%s' has been %s", user.getUsername(), action);
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
     * Thay đổi trạng thái công việc (Post-moderation: Chỉ DELETE/BLOCK vi phạm)
     * 
     * Chức năng:
     * - Thay đổi JobStatus để quản lý tin tuyển dụng
     * - Post-moderation Model: Admin KHÔNG pre-approve, chỉ DELETE/BLOCK violations
     * - Trạng thái: WAIT (Chưa mở), ACTIVE (Đang mở), CLOSED (Đã đóng), HIDDEN (Tạm ẩn/Bị khóa)
     * - Job HIDDEN = Bị Admin chặn do vi phạm
     * 
     * Quy trình:
     * 1. Tìm Job theo jobId: jobRepository.findById(jobId)
     * 2. Nếu không tồn tại: Ném ResourceNotFoundException
     * 3. Lưu trạng thái cũ: oldStatus = job.getJobStatus()
     * 4. Đặt trạng thái mới: job.setJobStatus(newStatus)
     * 5. Lưu: jobRepository.save(job)
     * 6. Log và trả về thông báo thay đổi (Old -> New)
     * 
     * Post-moderation Notes:
     * - KHÔNG có PENDING status (no pre-approval workflow)
     * - Recommend: ACTIVE -> HIDDEN (block violations) or use deleteJob() for permanent removal
     * 
     * @param jobId ID công việc cần thay đổi trạng thái
     * @param newStatus Trạng thái mới (WAIT/ACTIVE/CLOSED/HIDDEN - NOT PENDING/APPROVED)
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
    
    /**
     * Xóa tin tuyển dụng (Admin - Post-moderation)
     * 
     * Chức năng:
     * - Soft delete: Thay đổi JobStatus thành HIDDEN để ẩn tin khỏi công khai
     * - Sử dụng khi tin tuyển dụng vi phạm chính sách (scam, gambling, offensive content)
     * - Admin có quyền xóa bất kỳ tin tuyển dụng nào mà không cần thông báo trước
     * 
     * Post-moderation Policy:
     * - Admin removes content AFTER publication when violations are detected
     * - No pre-approval process, immediate action on violations
     * - Employer is fully responsible for content legality
     * 
     * Quy trình:
     * 1. Tìm Job theo jobId: jobRepository.findById(jobId)
     * 2. Nếu không tồn tại: Ném ResourceNotFoundException
     * 3. Đặt trạng thái: job.setJobStatus(JobStatus.HIDDEN)
     * 4. Lưu: jobRepository.save(job)
     * 5. Log và trả về thông báo xóa thành công
     * 
     * @param jobId ID tin tuyển dụng cần xóa
     * @return String - Thông báo xóa thành công
     * @throws ResourceNotFoundException Nếu không tìm thấy Job
     */
    @Override
    @Transactional
    public String deleteJob(Long jobId) {
        log.info("Admin deleting job (Post-moderation) - JobId: {}", jobId);
        
        // Entity manipulation pattern: Find -> Validate -> Set -> Save
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));
        
        String jobTitle = job.getJobTitle();
        String jobCode = job.getJobCode();
        
        // Soft delete - change status to HIDDEN (blocked by admin)
        job.setJobStatus(JobStatus.HIDDEN);
        jobRepository.save(job);
        
        log.info("Job deleted by admin - JobCode: {}, Title: {}", jobCode, jobTitle);
        
        return String.format("Job '%s' (Code: %s) has been removed due to policy violation", jobTitle, jobCode);
    }
    
    /**
     * Xóa tin đăng tìm việc (Admin - Post-moderation)
     * 
     * Chức năng:
     * - Soft delete: Thay đổi SKPostStatus thành CLOSED để ẩn tin khỏi công khai
     * - Sử dụng khi tin đăng vi phạm chính sách (fake profile, inappropriate content)
     * - Admin có quyền xóa bất kỳ SeekingPost nào mà không cần thông báo trước
     * 
     * Post-moderation Policy:
     * - Admin removes content AFTER publication when violations are detected
     * - No pre-approval process for candidate seeking posts
     * - Candidate is fully responsible for profile authenticity
     * 
     * Quy trình:
     * 1. Tìm SeekingPost theo seekingPostId: seekingPostRepository.findById(seekingPostId)
     * 2. Nếu không tồn tại: Ném ResourceNotFoundException
     * 3. Đặt trạng thái: seekingPost.setSkPostStatus(SeekingPostStatus.CLOSED)
     * 4. Lưu: seekingPostRepository.save(seekingPost)
     * 5. Log và trả về thông báo xóa thành công
     * 
     * @param seekingPostId ID tin đăng tìm việc cần xóa
     * @return String - Thông báo xóa thành công
     * @throws ResourceNotFoundException Nếu không tìm thấy SeekingPost
     */
    @Override
    @Transactional
    public String deleteSeekingPost(Long seekingPostId) {
        log.info("Admin deleting seeking post (Post-moderation) - SeekingPostId: {}", seekingPostId);
        
        // Entity manipulation pattern: Find -> Validate -> Set -> Save
        SeekingPost seekingPost = seekingPostRepository.findById(seekingPostId)
                .orElseThrow(() -> new ResourceNotFoundException("SeekingPost not found with ID: " + seekingPostId));
        
        String postTitle = seekingPost.getSkPostTitle();
        String postCode = seekingPost.getSkPostCode();
        
        // Soft delete - change status to CLOSED (blocked by admin)
        seekingPost.setSkPostStatus(SeekingPostStatus.CLOSED);
        seekingPostRepository.save(seekingPost);
        
        log.info("SeekingPost deleted by admin - PostCode: {}, Title: {}", postCode, postTitle);
        
        return String.format("Seeking post '%s' (Code: %s) has been removed due to policy violation", postTitle, postCode);
    }
    
    /**
     * Toggle trạng thái công việc (ACTIVE <-> HIDDEN)
     * 
     * Chức năng:
     * - Toggle JobStatus: Nếu ACTIVE thì chuyển thành HIDDEN, nếu HIDDEN thì chuyển thành ACTIVE
     * - Dùng để ẩn/hiện tin tuyển dụng nhanh chóng
     * - Admin có thể toggle để quản lý nội dung vi phạm tạm thời
     * 
     * Quy trình:
     * 1. Tìm Job theo jobId
     * 2. Kiểm tra trạng thái hiện tại
     * 3. Toggle: ACTIVE -> HIDDEN hoặc HIDDEN -> ACTIVE
     * 4. Lưu và trả về thông báo
     * 
     * @param jobId ID công việc cần toggle
     * @return String - Thông báo toggle thành công với trạng thái mới
     * @throws ResourceNotFoundException Nếu không tìm thấy Job
     */
    @Override
    @Transactional
    public String toggleJobStatus(Long jobId) {
        log.info("Toggling job status - JobId: {}", jobId);
        
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));
        
        JobStatus currentStatus = job.getJobStatus();
        JobStatus newStatus;
        
        // Toggle logic: ACTIVE <-> HIDDEN
        if (currentStatus == JobStatus.ACTIVE) {
            newStatus = JobStatus.HIDDEN;
        } else {
            newStatus = JobStatus.ACTIVE;
        }
        
        job.setJobStatus(newStatus);
        jobRepository.save(job);
        
        log.info("Job status toggled - JobCode: {}, Old: {}, New: {}", 
                job.getJobCode(), currentStatus, newStatus);
        
        String action = (newStatus == JobStatus.HIDDEN) ? "hide" : "show";
        return String.format("Job was %s post '%s' successfully", action, job.getJobId());
    }
    
    /**
     * Lấy tất cả tin đăng tìm việc (Admin)
     * 
     * Chức năng:
     * - Lấy tất cả SeekingPost bao gồm ACTIVE, HIDDEN, CLOSED
     * - Phân trang và sắp xếp theo thời gian tạo mới nhất
     * - Chỉ dành cho Admin để quản lý nội dung
     * - Convert entities sang DTOs sử dụng SeekingPostMapper
     * 
     * Quy trình:
     * 1. Gọi repository.findAllByOrderByCreatedAtDesc(pageable)
     * 2. Convert Page<SeekingPost> -> Page<JobSeekPostResponse>
     * 3. Trả về page result
     * 
     * @param pageable Tham số phân trang
     * @return Page<JobSeekPostResponse> - Danh sách tin đăng tìm việc phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public Page<JobSeekPostResponse> getAllSeekingPosts(Pageable pageable) {
        log.info("Admin fetching all seeking posts - Page: {}, Size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<SeekingPost> seekingPosts = seekingPostRepository.findAllByOrderByCreatedAtDesc(pageable);
        
        log.info("Found {} seeking posts", seekingPosts.getTotalElements());
        
        return seekingPosts.map(seekingPostMapper::toFullResponse);
    }
    
    /**
     * Toggle trạng thái tin đăng tìm việc (ACTIVE <-> HIDDEN)
     * 
     * Chức năng:
     * - Toggle SKPostStatus: Nếu ACTIVE thì chuyển thành HIDDEN, nếu HIDDEN thì chuyển thành ACTIVE
     * - Dùng để ẩn/hiện tin tìm việc nhanh chóng
     * - Admin có thể toggle để quản lý nội dung vi phạm tạm thời
     * 
     * Quy trình:
     * 1. Tìm SeekingPost theo seekingPostId
     * 2. Kiểm tra trạng thái hiện tại
     * 3. Toggle: ACTIVE -> HIDDEN hoặc HIDDEN -> ACTIVE
     * 4. Lưu và trả về thông báo
     * 
     * @param seekingPostId ID tin đăng tìm việc cần toggle
     * @return String - Thông báo toggle thành công với trạng thái mới
     * @throws ResourceNotFoundException Nếu không tìm thấy SeekingPost
     */
    @Override
    @Transactional
    public String toggleSeekingPostStatus(Long seekingPostId) {
        log.info("Toggling seeking post status - SeekingPostId: {}", seekingPostId);
        
        SeekingPost seekingPost = seekingPostRepository.findById(seekingPostId)
                .orElseThrow(() -> new ResourceNotFoundException("SeekingPost not found with ID: " + seekingPostId));
        
        SeekingPostStatus currentStatus = seekingPost.getSkPostStatus();
        SeekingPostStatus newStatus;
        
        // Toggle logic: ACTIVE <-> HIDDEN
        if (currentStatus == SeekingPostStatus.ACTIVE) {
            newStatus = SeekingPostStatus.HIDDEN;
        } else {
            newStatus = SeekingPostStatus.ACTIVE;
        }
        
        seekingPost.setSkPostStatus(newStatus);
        seekingPostRepository.save(seekingPost);
        
        log.info("Seeking post status toggled - PostCode: {}, Old: {}, New: {}", 
                seekingPost.getSkPostCode(), currentStatus, newStatus);
        
        String action = (newStatus == SeekingPostStatus.HIDDEN) ? "hide" : "show";
        return String.format("Seeking post was %s post '%s' successfully", action, seekingPost.getSkPostId());
    }
}
