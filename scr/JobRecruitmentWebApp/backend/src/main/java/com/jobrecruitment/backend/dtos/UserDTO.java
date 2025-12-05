package com.jobrecruitment.backend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {
    private Integer userId;

    @Size(max = 12, message = "Mã người dùng không được vượt quá 12 ký tự")
    private String userCode;

    @NotBlank(message = "Tên người dùng không được để trống")
    @Email(message = "Địa chỉ email không hợp lệ")
    private String userName;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$",
             message = "Mật khẩu phải chứa ít nhất một chữ cái và một chữ số")
    private String password;

    @NotBlank(message = "Vai trò không được để trống")
    private Integer roleId;
}
