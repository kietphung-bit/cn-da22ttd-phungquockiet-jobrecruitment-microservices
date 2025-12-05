package com.jobrecruitment.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleDTO {
    private Integer roleId;
    @Size(max = 10, message = "Mã vai trò không được vượt quá 10 ký tự")
    private String roleCode;
    @NotBlank(message = "Tên vai trò không được để trống")
    @Size(max = 50, message = "Tên vai trò không được vượt quá 50 ký tự")
    private String roleName;
}
