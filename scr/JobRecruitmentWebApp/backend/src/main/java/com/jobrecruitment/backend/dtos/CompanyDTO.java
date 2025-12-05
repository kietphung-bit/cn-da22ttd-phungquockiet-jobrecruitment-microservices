package com.jobrecruitment.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyDTO {
    private Integer companyId;

    @Size(max = 12, message = "Mã công ty không được vượt quá 12 ký tự")
    private String companyCode;

    @NotBlank(message = "Tên công ty không được để trống")
    @Size(max = 100, message = "Tên công ty không được vượt quá 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "Tên công ty chỉ được chứa chữ cái, chữ số và khoảng trắng")
    private String companyName;

    private String companyDescription;

    private String companyAddress;
    private String companyWebsite;

    @NotBlank(message = "Email công ty không được để trống")
    @Pattern(regexp = "^[\\w.-]+@[\\w.-]+\\.\\w{2,}$", message = "Email công ty phải đúng định dạng")
    private String companyEmail;

    private String logoURL;

    @NotBlank(message = "Trạng thái công ty không được để trống")
    private String companyStatus;

    private UserDTO user;
}
