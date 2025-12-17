package com.jobrecruitment.backend.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
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
 * SavedJob - Entity đại diện cho bảng saved_jobs (Tin đã lưu)
 * 
 * Mô tả:
 * - Lưu trữ danh sách tin tuyển dụng mà ứng viên đã bookmark
 * - Quan hệ ManyToOne với Candidate (Nhiều tin lưu của 1 ứng viên)
 * - Quan hệ ManyToOne với Job (Nhiều ứng viên có thể lưu cùng 1 tin)
 * 
 * Quy tắc nghiệp vụ:
 * - Một ứng viên có thể lưu nhiều tin tuyển dụng
 * - Một tin tuyển dụng có thể được nhiều ứng viên lưu
 * - Không có unique constraint: Cho phép lưu cùng tin nhiều lần (nếu xóa rồi lưu lại)
 * 
 * Tham khảo: Section 4.9 - SavedJob Module
 */
@Entity
@Table(name = "saved_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedJob {
    /**
     * Primary Key - ID tự động tăng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sjId;

    /**
     * Quan hệ ManyToOne với Candidate
     * - FetchType.LAZY: Load candidate khi cần
     * - Nhiều tin lưu thuộc về 1 ứng viên
     * - NOT NULL: Tin lưu phải thuộc về ứng viên
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    /**
     * Quan hệ ManyToOne với Job
     * - FetchType.LAZY: Load job khi cần
     * - Nhiều ứng viên có thể lưu cùng 1 tin tuyển dụng
     * - NOT NULL: Phải có tin tuyển dụng được lưu
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    /**
     * Thời gian lưu tin
     * - Ghi nhận thời điểm ứng viên bookmark tin tuyển dụng
     */
    private LocalDateTime savedTime;
}