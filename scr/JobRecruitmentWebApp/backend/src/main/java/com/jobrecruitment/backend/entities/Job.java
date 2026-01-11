package com.jobrecruitment.backend.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.jobrecruitment.backend.enums.JobStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Job - Entity đại diện cho bảng jobs (Tin tuyển dụng)
 * 
 * Mô tả:
 * - Lưu trữ thông tin chi tiết về tin tuyển dụng của công ty
 * - Quan hệ ManyToOne với Company (Nhiều tin cùng 1 công ty)
 * - Quan hệ ManyToOne với JobCategory (Nhiều tin cùng 1 ngành nghề)
 * 
 * Quy tắc nghiệp vụ:
 * - RBGTN: JobSalary phải > 0 (dương)
 * - RBSL: MaxCandidates phải >= 0 (không âm)
 * - RBNT: StartDate < EndDate (timeline hợp lệ)
 * - JobStatus: PENDING/WAIT/ACTIVE/CLOSED/HIDDEN
 * 
 * Tham khảo: Section 4.4 - Job Module
 */
@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    /**
     * Primary Key - ID tự động tăng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;

    /**
     * Quan hệ ManyToOne với Company
     * - FetchType.LAZY: Load company khi cần
     * - Nhiều tin tuyển dụng thuộc 1 công ty
     * - NOT NULL: Tin phải thuộc về công ty
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /**
     * Quan hệ ManyToOne với JobCategory
     * - FetchType.LAZY: Load jobCategory khi cần
     * - Nhiều tin cùng 1 ngành nghề (IT, Marketing...)
     * - NOT NULL: Tin phải thuộc ngành nghề
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jc_id", nullable = false)
    private JobCategory jobCategory;

    /**
     * Mã tin tuyển dụng
     * - Unique constraint: Không trùng lặp
     * - Format: "VL" + 8 chữ số (ví dụ: VL00000001)
     * Tham khảo: Section 4.5.B - Prefix Definitions
     */
    @Column(unique = true, nullable = false, length = 10)
    private String jobCode; 

    /**
     * Tiêu đề công việc
     * - Validation: @NotBlank
     * - Ví dụ: "Java Developer", "Marketing Manager"
     */
    @NotBlank(message = "Tiêu đề không được để trống")
    private String jobTitle;

    /**
     * Mô tả công việc
     * - Column Type: TEXT (cho phép nội dung dài)
     * - Ví dụ: Trách nhiệm công việc, môi trường làm việc...
     */
    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    /**
     * Yêu cầu công việc
     * - Column Type: TEXT (cho phép nội dung dài)
     * - Ví dụ: Kinh nghiệm, kỹ năng, bằng cấp...
     */
    @Column(columnDefinition = "TEXT")
    private String jobRequirement;

    /**
     * Trách nhiệm công việc
     * - Column Type: TEXT (cho phép nội dung dài)
     * - Ví dụ: Các công việc chính, trách nhiệm hàng ngày...
     */
    @Column(columnDefinition = "TEXT")
    private String jobResponsibilities;

    /**
     * Quyền lợi
     * - Column Type: TEXT (cho phép nội dung dài)
     * - Ví dụ: Bảo hiểm, thưởng, nghỉ phép...
     */
    @Column(columnDefinition = "TEXT")
    private String jobBenefits;

    /**
     * Mức lương
     * - Validation: @Positive
     * - Quy tắc: RBGTN - Phải > 0
     * - Đơn vị: VND (Vietnamese Dong)
     */
    @Positive(message = "Mức lương phải là số dương") 
    private Double jobSalary;

    /**
     * Địa điểm làm việc
     * - Ví dụ: "Quận 7, TP.HCM", "Hà Nội"
     */
    private String jobLocation;

    /**
     * Ngày bắt đầu tuyển dụng
     * - Validation: @NotNull
     * - Quy tắc: RBNT - StartDate < EndDate
     */
    @NotNull
    private LocalDate startDate; 

    /**
     * Ngày kết thúc tuyển dụng
     * - Validation: @NotNull
     * - Quy tắc: RBNT - EndDate > StartDate
     */
    @NotNull
    private LocalDate endDate;

    /**
     * Số lượng ứng viên tối đa
     * - Validation: @Min(0)
     * - Quy tắc: RBSL - Phải >= 0 (không âm)
     * - 0 = không giới hạn số lượng
     */
    @Min(value = 0, message = "Số lượng tuyển không được nhỏ hơn 0") 
    private Integer maxCandidates;

    /**
     * Trạng thái tin tuyển dụng
     * - ENUM: PENDING, WAIT, ACTIVE, CLOSED, HIDDEN
     * - PENDING: Chờ Admin phê duyệt
     * - WAIT: Đã duyệt, chưa đến StartDate
     * - ACTIVE: Đang tuyển (StartDate <= now <= EndDate)
     * - CLOSED: Đã kết thúc (now > EndDate hoặc đủ ứng viên)
     * - HIDDEN: Bị ẩn bởi Admin
     * - Lưu dạng String trong database
     * Tham khảo: Section 3.3 - Job Table
     */
    @Enumerated(EnumType.STRING)
    private JobStatus jobStatus;

    /**
     * Quan hệ One-to-Many với Application
     * - FetchType.LAZY: Chỉ load applications khi cần
     * - Một tin có nhiều đơn ứng tuyển
     */
    @OneToMany(mappedBy = "job", fetch = FetchType.LAZY)
    private List<Application> applications;

    /**
     * Quan hệ One-to-Many với SavedJob
     * - FetchType.LAZY: Chỉ load savedJobs khi cần
     * - Một tin có thể được nhiều ứng viên lưu
     */
    @OneToMany(mappedBy = "job", fetch = FetchType.LAZY)
    private List<SavedJob> savedJobs;

    /**
     * Thời gian tạo tin (tự động)
     */
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật gần nhất (tự động)
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}