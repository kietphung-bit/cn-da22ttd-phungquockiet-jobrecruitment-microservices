package com.jobrecruitment.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CandidateDTO {
    private Integer candidateId;

    @Size(max = 12, message = "Mã ứng viên không được vượt quá 12 ký tự")
    private String candidateCode;

    @NotBlank(message = "Tên ứng viên không được để trống")
    @Size(max = 100, message = "Tên ứng viên không được vượt quá 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "Tên ứng viên chỉ được chứa chữ cái, chữ số và khoảng trắng")
    private String candidateName;

    @NotBlank(message = "Giới tính không được để trống")
    private String candidateGender;

    @NotBlank(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private String candidateBirthdate;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(\\+84|0)\\d{9,10}$", message = "Số điện thoại không đúng định dạng")
    private String candidatePhone;

    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Email phải đúng định dạng")
    private String candidateEmail;

    private String candidateEducation;
    private String candidateExp;
    private String candidateSkills;

    private UserDTO user;
}
