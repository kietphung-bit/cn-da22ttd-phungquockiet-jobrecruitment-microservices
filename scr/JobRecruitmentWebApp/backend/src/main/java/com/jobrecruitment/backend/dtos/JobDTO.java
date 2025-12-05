package com.jobrecruitment.backend.dtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobDTO {
    private Integer jobId;

    @Size(max = 12, message = "Mã công việc không được vượt quá 12 ký tự")
    private String jobCode;

    @NotBlank(message = "Tiêu đề công việc không được để trống")
    @Size(max = 255, message = "Tiêu đề công việc không được vượt quá 255 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9\\s.,'-]+$", message = "Tiêu đề công việc chứa ký tự không hợp lệ")
    private String jobTitle;

    private String jobDescription;

    private String jobRequirement;

    @NotBlank(message = "Mức lương không được để trống")
    @Positive(message = "Mức lương phải là số dương")
    private Double jobSalary;

    private String jobLocation;

    @NotBlank(message = "Ngày bắt đầu không được để trống")
    private String startDate;

    @NotBlank(message = "Ngày kết thúc không được để trống")
    @Future(message = "Ngày kết thúc phải ở tương lai")
    private String endDate;

    @NotBlank(message = "Trạng thái công việc không được để trống")
    private String jobStatus;

    @NotBlank(message = "ID danh mục công việc không được để trống")
    private Integer jcId;

    @NotBlank(message = "ID công ty không được để trống")
    private Integer companyId;
}
