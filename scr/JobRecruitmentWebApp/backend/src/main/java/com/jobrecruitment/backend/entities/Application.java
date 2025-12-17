package com.jobrecruitment.backend.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.jobrecruitment.backend.enums.ApplicationStatus;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Application - Entity đại diện cho bảng applications (Đơn ứng tuyển)
 * 
 * Mô tả:
 * - Lưu trữ thông tin đơn ứng tuyển của ứng viên vào tin tuyển dụng
 * - Quan hệ ManyToOne với Job (Nhiều đơn ứng tuyển cùng 1 tin)
 * - Quan hệ ManyToOne với CV (Nhiều đơn dùng chung 1 CV)
 * 
 * Quy tắc nghiệp vụ:
 * - RBNT: ApplyTime phải trong khoảng [Job.StartDate, Job.EndDate]
 * - RBUT: ApplicationStatus - PENDING/APPROVED/REJECTED
 * - Một ứng viên chỉ được nộp 1 đơn cho 1 tin tuyển dụng
 * 
 * Tham khảo: Section 4.8 - Application Module
 */
@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    /**
     * Primary Key - ID tự động tăng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    /**
     * Quan hệ ManyToOne với Job
     * - FetchType.LAZY: Load job khi cần
     * - Nhiều đơn ứng tuyển vào 1 tin tuyển dụng
     * - NOT NULL: Đơn phải gắn với tin tuyển dụng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    /**
     * Quan hệ ManyToOne với CV
     * - FetchType.LAZY: Load cv khi cần
     * - Nhiều đơn có thể dùng chung 1 CV
     * - NOT NULL: Đơn phải đính kèm CV
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id", nullable = false)
    private CV cv;

    /**
     * Mã đơn ứng tuyển
     * - Unique constraint: Không trùng lặp
     * - Format: "DX" + 8 chữ số (ví dụ: DX00000001)
     * Tham khảo: Section 4.5.B - Prefix Definitions
     */
    @Column(unique = true, nullable = false, length = 10)
    private String applicationCode; 

    /**
     * Thời gian nộp đơn
     * - Quy tắc: RBNT - Phải trong khoảng [Job.StartDate, Job.EndDate]
     * - Validation: Job.StartDate <= applyTime <= Job.EndDate
     */
    private LocalDateTime applyTime; // check (Start <= Now <= End)

    /**
     * Trạng thái đơn ứng tuyển
     * - ENUM: PENDING, APPROVED, REJECTED
     * - PENDING: Chờ nhà tuyển dụng xét duyệt
     * - APPROVED: Đã được chấp nhận
     * - REJECTED: Bị từ chối
     * - Lưu dạng String trong database
     * - Quy tắc: RBUT
     * Tham khảo: Section 3.6 - Application Table
     */
    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus;

    /**
     * Thời gian tạo đơn (tự động)
     */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật gần nhất (tự động)
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}