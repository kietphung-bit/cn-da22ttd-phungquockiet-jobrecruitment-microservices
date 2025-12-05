package com.jobrecruitment.backend.dtos.request;

import java.time.LocalDate;

import com.jobrecruitment.backend.enums.JobStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Job Creation/Update Request DTO
 * Section 4.2 - Job Management
 * Section 4.6 - Data Constraints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRequest {
    
    @NotNull(message = "Job Category không được để trống")
    private Integer jcId;
    
    @NotBlank(message = "Tiêu đề công việc không được để trống")
    private String jobTitle;
    
    private String jobDescription;
    
    private String jobRequirement;
    
    @Positive(message = "Mức lương phải lớn hơn 0") // RBGTN
    private Double jobSalary;
    
    private String jobLocation;
    
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate; // RBNT validation in service layer
    
    @Min(value = 0, message = "Số lượng tuyển không được nhỏ hơn 0") // RBSL
    private Integer maxCandidates;
    
    private JobStatus jobStatus;
}
