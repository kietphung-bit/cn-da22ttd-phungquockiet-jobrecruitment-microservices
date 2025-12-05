package com.jobrecruitment.backend.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApplicationDTO {
    private Integer applicationId;

    @Size(max = 12, message = "Mã đơn xin việc không được vượt quá 12 ký tự")
    private String applicationCode;

    private LocalDate applyTime;

    @NotBlank(message = "Trạng thái đơn xin việc không được để trống")
    private String applicationStatus;

    @NotBlank(message = "Mã công việc không được để trống")
    private Integer jobId;

    @NotBlank(message = "Mã CV không được để trống")
    private Integer cvId;
}
