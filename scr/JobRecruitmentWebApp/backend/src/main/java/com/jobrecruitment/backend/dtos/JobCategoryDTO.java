package com.jobrecruitment.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobCategoryDTO {
    private Integer jcId;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "Tên danh mục chỉ được chứa chữ cái, số và khoảng trắng")
    private String jcName;

    @Size(max = 500, message = "Mô tả danh mục không được vượt quá 500 ký tự")
    private String jcDescription;

    @NotBlank(message = "Lương cơ bản không được để trống")
    @Positive(message = "Lương cơ bản phải lớn hơn 0")
    private Double jcBaseSalary;
}
