package com.jobrecruitment.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CVDTO {
    private Integer cvId;

    @Size(max = 12, message = "Mã CV không được vượt quá 12 ký tự")
    private String cvCode;

    @NotBlank(message = "Tên CV không được để trống")
    private String cvName;

    @NotBlank(message = "URL file CV không được để trống")
    private String cvFileUrl;

    @NotBlank(message = "Trạng thái CV không được để trống")
    private String cvStatus;

    @NotBlank(message = "ID ứng viên không được để trống")
    private Integer candidateId;
}
