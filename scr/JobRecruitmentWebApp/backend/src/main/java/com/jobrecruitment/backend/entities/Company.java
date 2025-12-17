package com.jobrecruitment.backend.entities;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.jobrecruitment.backend.enums.CompanyStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Company - Entity đại diện cho bảng companies (Hồ sơ nhà tuyển dụng)
 * 
 * Mô tả:
 * - Lưu trữ thông tin chi tiết của doanh nghiệp/nhà tuyển dụng
 * - Quan hệ One-to-One với User (1 User = 1 Company profile)
 * - CompanyCode đồng bộ với UserCode (Section 4.5.C)
 * 
 * Quy tắc nghiệp vụ:
 * - RBHT: CompanyName chỉ chứa chữ cái và khoảng trắng
 * - RBEML: CompanyEmail phải đúng định dạng email
 * - CompanyStatus: PENDING (Chờ duyệt), ACTIVE (Đang hoạt động), BLOCKED (Bị khóa)
 * 
 * Tham khảo: Section 4.2 - Employer Module
 */
@Entity
@Data
@Table(name = "companies")
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    /**
     * Primary Key - ID tự động tăng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    /**
     * Quan hệ One-to-One với User
     * - Cascade: MERGE, REFRESH, REMOVE
     * - Khi xóa Company -> xóa User tương ứng
     */
    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;

    /**
     * Mã doanh nghiệp
     * - Unique constraint: Không trùng lặp
     * - Format: "DN" + 8 chữ số (ví dụ: DN12345678)
     * - Đồng bộ với UserCode (Section 4.5.C)
     * Tham khảo: Section 4.5.B - Prefix Definitions
     */
    @Column(unique = true, nullable = false, length = 10)
    private String companyCode;

    /**
     * Tên công ty
     * - Validation: @NotBlank, @Pattern
     * - Quy tắc: RBHT - Chỉ chứa chữ cái và khoảng trắng
     * - Ví dụ: "Công ty TNHH ABC", "FPT Software"
     */
    @NotBlank(message = "Tên công ty không được để trống")
    @Pattern(regexp = "^[a-zA-Z\\s\\p{L}]+$", message = "Tên công ty chỉ chứa chữ cái và khoảng trắng")
    @Column(nullable = false)
    private String companyName;

    /**
     * Mô tả công ty
     * - Column Type: TEXT (cho phép nội dung dài)
     * - Ví dụ: Giới thiệu về công ty, lĩnh vực hoạt động, quy mô...
     */
    @Column(columnDefinition = "TEXT")
    private String companyDescription;

    /**
     * Địa chỉ công ty
     * - Column Type: TEXT (cho phép địa chỉ dài)
     * - Ví dụ: "123 Nguyễn Văn Linh, Quận 7, TP.HCM"
     */
    @Column(columnDefinition = "TEXT")
    private String companyAddress;

    /**
     * Website công ty
     * - Optional: Có thể để trống
     * - Ví dụ: "https://www.company.com"
     */
    private String companyWebsite;

    /**
     * Email liên hệ công ty
     * - Validation: @Email
     * - Quy tắc: RBEML - Phải có @ và domain hợp lệ
     * - Bắt buộc (nullable = false)
     */
    @Email(message = "Email công ty phải đúng định dạng")
    @Column(nullable = false)
    private String companyEmail;

    /**
     * URL logo công ty
     * - Lưu đường dẫn đến file logo trên server/cloud
     * - Optional: Có thể để trống
     */
    private String logoURL;

    /**
     * Trạng thái công ty
     * - ENUM: PENDING, ACTIVE, BLOCKED
     * - PENDING: Chờ Admin xét duyệt (mới đăng ký)
     * - ACTIVE: Đang hoạt động (được phép đăng tin tuyển dụng)
     * - BLOCKED: Bị khóa (không thể đăng tin)
     * - Lưu dạng String trong database
     * Tham khảo: Section 3.1 - Company Table
     */
    @Enumerated(EnumType.STRING)
    private CompanyStatus companyStatus;

    /**
     * Quan hệ One-to-Many với Job
     * - FetchType.LAZY: Chỉ load jobs khi cần
     * - Một công ty có thể đăng nhiều tin tuyển dụng
     */
    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private List<Job> jobs;

    /**
     * Thời gian tạo hồ sơ (tự động)
     */
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật gần nhất (tự động)
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
