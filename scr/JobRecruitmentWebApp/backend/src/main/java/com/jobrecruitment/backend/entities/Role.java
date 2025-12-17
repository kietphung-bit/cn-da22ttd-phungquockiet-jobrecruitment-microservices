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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role - Entity đại diện cho bảng roles (Nhóm quyền)
 * 
 * Mô tả:
 * - Lưu trữ các vai trò trong hệ thống (Admin, Employer, Candidate)
 * - Dùng cho RBAC (Role-Based Access Control)
 * - Dữ liệu seed: ADM, DN, UV (Section 6)
 * 
 * Tham khảo: Section 4.1 - RBAC
 */
@Entity
@Data
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    /**
     * Primary Key - ID tự động tăng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;
    
    /**
     * Mã vai trò
     * - Unique constraint: Không trùng lặp
     * - Giá trị seed (Section 6):
     *   + "ADM": Quản trị viên (Administrator)
     *   + "DN": Nhà tuyển dụng (Doanh nghiệp / Employer)
     *   + "UV": Ứng viên (Candidate / Job Seeker)
     */
    @Column(unique = true, nullable = false, length = 10)
    private String roleCode;
    
    /**
     * Tên hiển thị của vai trò
     * - Ví dụ: "Admin", "Doanh nghiệp", "Ứng viên"
     */
    @Column(nullable = false)
    private String roleName;

    /**
     * Thời gian tạo (tự động)
     */
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật (tự động)
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Quan hệ One-to-Many với User
     * - FetchType.LAZY: Chỉ load users khi cần (tối ưu performance)
     * - Một role có thể có nhiều users
     */
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private List<User> users;
}
