package com.jobrecruitment.backend.dtos;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SavedJobDTO {
    private Integer sjId;
    
    private LocalDateTime savedTime;

    @NotBlank(message = "Mã ứng viên không được để trống")
    private Integer candidateId;

    @NotBlank(message = "Mã công việc không được để trống")
    private Integer jobId;
}
