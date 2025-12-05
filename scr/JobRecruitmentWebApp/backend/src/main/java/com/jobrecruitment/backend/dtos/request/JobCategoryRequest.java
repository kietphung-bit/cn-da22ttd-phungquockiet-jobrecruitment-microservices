package com.jobrecruitment.backend.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Job Category Request DTO
 * Section 4.4 - Admin Module - Category Management
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobCategoryRequest {
    
    @NotBlank(message = "Tên danh mục không được để trống")
    private String jcName;
    
    private String jcDescription;
    
    @Positive(message = "Lương cơ bản phải lớn hơn 0") // RBGTN
    private Double jcBaseSalary;
}
