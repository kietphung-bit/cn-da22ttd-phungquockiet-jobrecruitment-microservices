package com.jobrecruitment.backend.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DashboardStatsResponse - DTO trả về thống kê Dashboard Admin
 * 
 * Mô tả:
 * - Trả về các chỉ số thống kê cho Dashboard quản trị
 * - Bao gồm: Thống kê User, Company, Job, Application
 * - Chỉ Admin (Role ADM) mới truy cập được
 * 
 * Nhóm thống kê:
 * - User Statistics: Tổng User, Candidate, Employer, trạng thái Company
 * - Job Statistics: Tổng tin tuyển dụng, phân theo trạng thái
 * - Application Statistics: Tổng đơn, đơn hôm nay, tháng này, phân theo trạng thái
 * 
 * Sử dụng:
 * - API GET /api/v1/admin/dashboard/stats
 * 
 * Tham khảo: Section 4.1 - Admin Module - Dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Admin Dashboard Statistics Response")
public class DashboardStatsResponse {
    
    // ==================== User Statistics ====================
    /**
     * Tổng số User trong hệ thống
     * - Bao gồm: Admin, Employer, Candidate
     */
    @Schema(description = "Total number of users in the system", example = "150")
    private Long totalUsers;
    
    @Schema(description = "Total number of candidates (job seekers)", example = "100")
    private Long totalCandidates;
    
    @Schema(description = "Total number of employers (companies)", example = "45")
    private Long totalEmployers;
    
    @Schema(description = "Number of active companies", example = "40")
    private Long activeEmployers;
    
    @Schema(description = "Number of companies pending approval", example = "3")
    private Long pendingEmployers;
    
    @Schema(description = "Number of blocked companies", example = "2")
    private Long blockedEmployers;
    
    // Job Statistics
    @Schema(description = "Total number of job postings", example = "250")
    private Long totalJobs;
    
    @Schema(description = "Number of active job postings", example = "180")
    private Long activeJobs;
    
    @Schema(description = "Number of jobs pending approval", example = "15")
    private Long pendingJobs;
    
    @Schema(description = "Number of closed job postings", example = "50")
    private Long closedJobs;
    
    @Schema(description = "Number of hidden job postings", example = "5")
    private Long hiddenJobs;
    
    // Application Statistics
    @Schema(description = "Total number of applications submitted", example = "500")
    private Long totalApplications;
    
    @Schema(description = "Number of applications submitted today", example = "12")
    private Long applicationsToday;
    
    @Schema(description = "Number of applications submitted this month", example = "85")
    private Long applicationsThisMonth;
    
    @Schema(description = "Number of pending applications", example = "120")
    private Long pendingApplications;
    
    @Schema(description = "Number of approved applications", example = "300")
    private Long approvedApplications;
    
    @Schema(description = "Number of rejected applications", example = "80")
    private Long rejectedApplications;
}
