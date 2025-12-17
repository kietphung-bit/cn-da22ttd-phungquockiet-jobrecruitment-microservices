package com.jobrecruitment.backend.entities;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JobCategory - Entity đại diện cho bảng job_categories (Ngành nghề)
 * 
 * Mô tả:
 * - Master data: Lưu trữ danh mục các ngành nghề tuyển dụng
 * - Sử dụng trong phân loại tin tuyển dụng
 * - Admin quản lý danh sách ngành nghề
 * 
 * Quy tắc nghiệp vụ:
 * - RBGTN: JcBaseSalary phải > 0 (mức lương tham chiếu)
 * - JcName: Unique constraint (không trùng lặp)
 * 
 * Tham khảo: Section 4.6 - JobCategory Module
 */
@Entity
@Table(name = "job_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobCategory {
    /**
     * Primary Key - ID tự động tăng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer jcId;

    /**
     * Tên ngành nghề
     * - Unique constraint: Không trùng lặp
     * - Ví dụ: "Công nghệ thông tin", "Marketing", "Kinh doanh"
     */
    @Column(nullable = false, unique = true)
    private String jcName;

    /**
     * Mô tả ngành nghề
     * - Column Type: TEXT (cho phép nội dung dài)
     * - Ví dụ: Chi tiết về ngành nghề, xu hướng tuyển dụng...
     */
    @Column(columnDefinition = "TEXT")
    private String jcDescription;

    /**
     * Mức lương cơ bản (tham chiếu)
     * - Validation: @Positive
     * - Quy tắc: RBGTN - Phải > 0
     * - Đơn vị: VND (Vietnamese Dong)
     * - Dùng làm tham chiếu cho các tin tuyển dụng
     */
    @Positive(message = "Lương cơ bản phải lớn hơn 0") 
    private Double jcBaseSalary;

    /**
     * Thời gian tạo (tự động)
     */
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật gần nhất (tự động)
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Quan hệ One-to-Many với Job
     * - FetchType.LAZY: Chỉ load jobs khi cần
     * - Một ngành nghề có nhiều tin tuyển dụng
     */
    @OneToMany(mappedBy = "jobCategory", fetch = FetchType.LAZY)
    private List<Job> jobs;
}