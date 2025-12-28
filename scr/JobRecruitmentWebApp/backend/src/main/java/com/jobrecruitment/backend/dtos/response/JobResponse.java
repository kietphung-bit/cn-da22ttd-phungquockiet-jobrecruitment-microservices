package com.jobrecruitment.backend.dtos.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.jobrecruitment.backend.enums.JobStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JobResponse - DTO trả về thông tin tin tuyển dụng
 * 
 * Mô tả:
 * - Trả về thông tin chi tiết tin tuyển dụng
 * - Map từ Job entity
 * - Bao gồm companyName và jcName (không cần nested object)
 * 
 * Sử dụng:
 * - API GET /api/v1/jobs
 * - API GET /api/v1/jobs/{id}
 * - API POST /api/v1/jobs
 * - API PUT /api/v1/jobs/{id}
 * 
 * Tham khảo: Section 4.2 - Job Management
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    
    /**
     * ID tin tuyển dụng
     */
    private Long jobId;
    
    /**
     * ID công ty đăng tin
     * - Tham chiếu đến Company.companyId
     */
    private Long companyId;
    
    /**
     * Tên công ty đăng tin
     * - Flatten data từ Company.companyName
     */
    private String companyName;

    /**
     * Logo công ty
     * - Flatten data từ Company.logoURL
     */
    private String logoURL;
    
    /**
     * ID ngành nghề
     * - Tham chiếu đến JobCategory.jcId
     */
    private Integer jcId;
    
    /**
     * Tên ngành nghề
     * - Flatten data từ JobCategory.jcName
     */
    private String jcName;
    
    /**
     * Mã tin tuyển dụng
     * - Format: "VL" + 8 chữ số
     */
    private String jobCode;
    
    /**
     * Tiêu đề công việc
     */
    private String jobTitle;
    
    /**
     * Mô tả công việc
     */
    private String jobDescription;
    
    /**
     * Yêu cầu công việc
     */
    private String jobRequirement;
    
    /**
     * Mức lương (Đơn vị: VND)
     */
    private Double jobSalary;
    
    /**
     * Địa điểm làm việc
     */
    private String jobLocation;
    
    /**
     * Ngày bắt đầu tuyển dụng
     */
    private LocalDate startDate;
    
    /**
     * Ngày kết thúc tuyển dụng
     */
    private LocalDate endDate;
    
    /**
     * Số lượng ứng viên tối đa
     * - 0 = không giới hạn
     */
    private Integer maxCandidates;
    
    /**
     * Trạng thái tin tuyển dụng
     * - PENDING: Chờ duyệt
     * - WAIT: Đã duyệt, chưa đến ngày
     * - ACTIVE: Đang tuyển
     * - CLOSED: Đã kết thúc
     * - HIDDEN: Đã ẩn
     */
    private JobStatus jobStatus;
    
    /**
     * Thời gian tạo tin
     */
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật gần nhất
     */
    private LocalDateTime updatedAt;
}
