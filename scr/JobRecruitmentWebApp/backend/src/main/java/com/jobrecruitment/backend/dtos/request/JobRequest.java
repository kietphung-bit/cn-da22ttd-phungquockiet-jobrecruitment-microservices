package com.jobrecruitment.backend.dtos.request;

import java.time.LocalDate;

import com.jobrecruitment.backend.enums.JobStatus;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JobRequest - DTO nhận dữ liệu tạo/cập nhật tin tuyển dụng
 * 
 * Mô tả:
 * - Sử dụng trong API POST /api/v1/jobs (tạo mới)
 * - Sử dụng trong API PUT /api/v1/jobs/{id} (cập nhật)
 * - Chỉ Employer (Role DN) mới được tạo/cập nhật tin
 * - JobCode tự động generate khi tạo mới ("VL" + 8 chữ số)
 * 
 * Validation Rules:
 * - @NotNull: Không được null
 * - @NotBlank: Không được trống
 * - @Positive: Phải là số dương > 0
 * - @Min(0): Phải >= 0
 * - @AssertTrue: termsAgreed phải là true (Post-moderation policy)
 * 
 * Business Rules:
 * - RBGTN: JobSalary phải > 0
 * - RBSL: MaxCandidates phải >= 0
 * - RBNT: StartDate < EndDate (kiểm tra ở Service layer)
 * - JobStatus: ACTIVE (mới tạo) - Post-moderation model (No pre-approval)
 * - Legal: Employer phải đồng ý Terms & Conditions trước khi đăng
 * 
 * Tham khảo: Section 4.2 - Job Management, Section 4.6 - Data Constraints, Section 4.7 - Legal & Compliance
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRequest {
    
    /**
     * ID ngành nghề
     * - Validation: @NotNull
     * - Tham chiếu đến JobCategory.jcId
     * - Ví dụ: 1 (Công nghệ thông tin)
     */
    @NotNull(message = "Danh mục không được để trống")
    private Integer jcId;
    
    /**
     * Tiêu đề công việc
     * - Validation: @NotBlank
     * - Ví dụ: "Java Developer", "Marketing Manager"
     */
    @NotBlank(message = "Tiêu đề công việc không được để trống")
    private String jobTitle;
    
    /**
     * Mô tả công việc
     * - Optional: Có thể null
     * - Ví dụ: Trách nhiệm, môi trường làm việc...
     */
    private String jobDescription;
    
    /**
     * Yêu cầu công việc
     * - Optional: Có thể null
     * - Ví dụ: Kinh nghiệm, kỹ năng, bằng cấp...
     */
    private String jobRequirement;
    
    /**
     * Mức lương
     * - Optional: Có thể null
     * - Validation: @Positive (nếu có giá trị)
     * - Quy tắc: RBGTN - Phải > 0
     * - Đơn vị: VND
     */
    @Positive(message = "Mức lương phải lớn hơn 0") // RBGTN
    private Double jobSalary;
    
    /**
     * Địa điểm làm việc
     * - Optional: Có thể null
     * - Ví dụ: "Quận 7, TP.HCM"
     */
    private String jobLocation;
    
    /**
     * Ngày bắt đầu tuyển dụng
     * - Validation: @NotNull
     * - Quy tắc: RBNT - Phải < endDate (kiểm tra ở Service layer)
     */
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;
    
    /**
     * Ngày kết thúc tuyển dụng
     * - Validation: @NotNull
     * - Quy tắc: RBNT - Phải > startDate (kiểm tra ở Service layer)
     */
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate; // RBNT validation in service layer
    
    /**
     * Số lượng ứng viên tối đa
     * - Optional: Có thể null
     * - Validation: @Min(0)
     * - Quy tắc: RBSL - Phải >= 0 (không âm)
     * - 0 = không giới hạn số lượng
     */
    @Min(value = 0, message = "Số lượng tuyển không được nhỏ hơn 0") // RBSL
    private Integer maxCandidates;
    
    /**
     * Trạng thái tin tuyển dụng
     * - Optional: Có thể null (mặc định ACTIVE - Post-moderation)
     * - ENUM: WAIT, ACTIVE, CLOSED, HIDDEN
     * - Admin có quyền thay đổi trạng thái (delete/block)
     */
    private JobStatus jobStatus;
    
    /**
     * Đồng ý điều khoản sử dụng
     * - Validation: @AssertTrue - Phải là true
     * - Employer phải tick checkbox "Đồng ý Terms & Legal Responsibility" trước khi đăng tin
     * - Post-moderation Policy: System không chịu trách nhiệm về nội dung tin tuyển dụng
     * - User (Employer) hoàn toàn chịu trách nhiệm về tính chính xác và hợp pháp của tin đăng
     * 
     * Tham khảo: Section 4.7 - Legal & Compliance Module
     */
    @AssertTrue(message = "Bạn phải đồng ý với điều khoản sử dụng và cam kết chịu trách nhiệm về nội dung tin đăng")
    private Boolean termsAgreed;
}
