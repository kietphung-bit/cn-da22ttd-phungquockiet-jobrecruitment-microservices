package com.jobrecruitment.backend.entities;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.jobrecruitment.backend.enums.CVStatus;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CV - Entity đại diện cho bảng cvs (Hồ sơ xin việc)
 * 
 * Mô tả:
 * - Lưu trữ file CV của ứng viên
 * - Một ứng viên có thể có nhiều phiên bản CV
 * - Quan hệ ManyToOne với Candidate
 * 
 * Quy tắc nghiệp vụ:
 * - RBCV: CVStatus - ACTIVE (đang sử dụng), HIDDEN (đã ẩn)
 * - Mỗi đơn ứng tuyển gắn với 1 CV cụ thể
 * 
 * Tham khảo: Section 4.7 - CV Module
 */
@Entity
@Table(name = "cvs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CV {
    /**
     * Primary Key - ID tự động tăng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cvId;

    /**
     * Quan hệ ManyToOne với Candidate
     * - FetchType.LAZY: Load candidate khi cần
     * - Nhiều CV thuộc về 1 ứng viên
     * - NOT NULL: CV phải thuộc về ứng viên
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    /**
     * Mã CV
     * - Unique constraint: Không trùng lặp
     * - Format: "CV" + 8 chữ số (ví dụ: CV00000001)
     * Tham khảo: Section 4.5.B - Prefix Definitions
     */
    @Column(unique = true, nullable = false, length = 10)
    private String cvCode;

    /**
     * Đường dẫn file CV
     * - Lưu path hoặc URL đến file CV trên server/cloud
     * - Ví dụ: "/uploads/cvs/cv_12345.pdf"
     */
    private String cvFile;

    /**
     * Trạng thái CV
     * - ENUM: ACTIVE, HIDDEN
     * - ACTIVE: Đang sử dụng (có thể ứng tuyển)
     * - HIDDEN: Đã ẩn (không sử dụng nữa)
     * - Lưu dạng String trong database
     * - Quy tắc: RBCV
     * Tham khảo: Section 3.5 - CV Table
     */
    @Enumerated(EnumType.STRING)
    private CVStatus cvStatus;

    /**
     * Quan hệ One-to-Many với Application
     * - FetchType.LAZY: Chỉ load applications khi cần
     * - Một CV có thể được dùng cho nhiều đơn ứng tuyển
     */
    @OneToMany(mappedBy = "cv", fetch = FetchType.LAZY)
    private List<Application> applications;

    /**
     * Thời gian tạo CV (tự động)
     */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật gần nhất (tự động)
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}