package com.jobrecruitment.backend.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
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
 * User - Entity đại diện cho bảng users (Tài khoản hệ thống)
 * 
 * Mô tả:
 * - Lưu trữ thông tin xác thực và phân quyền của người dùng
 * - Mỗi User có 1 Role (ADM/DN/UV) xác định quyền truy cập
 * - UserCode đồng bộ với CompanyCode (Role=DN) hoặc CandidateCode (Role=UV)
 * 
 * Tham khảo: Section 4.5.C - UserCode Synchronization Rule
 */
@Entity
@Data
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * Primary Key - ID tự động tăng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    
    /**
     * Mã định danh toàn cục (Global Identifier)
     * - Unique constraint: Không trùng lặp trong toàn hệ thống
     * - Length: 12 ký tự (tối đa)
     * - Quy tắc đồng bộ:
     *   + Admin: "AD00000001" (cố định)
     *   + Employer: Đồng bộ với CompanyCode (ví dụ: "DN12345678")
     *   + Candidate: Đồng bộ với CandidateCode (ví dụ: "UV98765432")
     * Tham khảo: Section 4.5.C - UserCode Synchronization
     */
    @Column(unique = true, nullable = false, length = 12)
    private String userCode;
    
    /**
     * Tên đăng nhập (Registration Email)
     * - Unique constraint: Mỗi email chỉ đăng ký 1 tài khoản
     * - Format: Email hợp lệ (có @ và domain)
     * - Tham khảo: RBEML - Email Format Rule
     */
    @Column(unique = true, nullable = false)
    private String username; 
    
    /**
     * Mật khẩu đã mã hóa
     * - Mã hóa bằng BCrypt (Section 4.1)
     * - Không thể giải mã ngược (one-way hashing)
     * - Ví dụ: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     */
    @Column(nullable = false)
    private String password;
    
    /**
     * Trạng thái khóa tài khoản
     * - true: Tài khoản bị khóa (không thể đăng nhập)
     * - false: Tài khoản hoạt động bình thường
     * - Nullable: Cho phép tương thích ngược với dữ liệu cũ
     * - Mặc định: false
     * Chức năng: Admin có thể lock/unlock tài khoản người dùng
     */
    @Column(nullable = true)
    private Boolean locked = false;

    /**
     * Thời gian tạo tài khoản (tự động)
     */
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật gần nhất (tự động)
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Quan hệ Many-to-One với Role
     * - FetchType.EAGER: Luôn load Role khi query User (cần cho phân quyền)
     * - Mỗi User có 1 Role xác định quyền truy cập
     * - Role codes: ADM (Admin), DN (Employer), UV (Candidate)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}
