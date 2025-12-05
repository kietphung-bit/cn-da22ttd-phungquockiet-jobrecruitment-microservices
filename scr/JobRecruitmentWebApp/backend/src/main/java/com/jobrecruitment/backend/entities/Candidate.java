package com.jobrecruitment.backend.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.jobrecruitment.backend.enums.Gender;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "candidates")
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long candidateId;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;

    @Column(unique = true, nullable = false, length = 10)
    private String candidateCode;

    @NotBlank(message = "Họ và tên không được để trống")
    @Pattern(regexp = "^[a-zA-Z\\s\\p{L}]+$", message = "Họ và tên không chứa ký tự đặc biệt và số")
    private String candidateName;

    @Column(columnDefinition = "TEXT")
    private String candidateDescription;

    @Enumerated(EnumType.STRING)
    private Gender candidateGender;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate candidateBirthdate;

    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại phải là 10-11 chữ số")
    private String candidatePhone;

    @Email(message = "Email phải đúng định dạng")
    private String candidateEmail;

    @Column(columnDefinition = "TEXT")
    private String candidateEducation;

    @Column(columnDefinition = "TEXT")
    private String candidateExp;

    @Column(columnDefinition = "TEXT")
    private String candidateSkills;

    @OneToMany(mappedBy = "candidate", fetch = FetchType.LAZY)
    private List<CV> cvs;

    @OneToMany(mappedBy = "candidate", fetch = FetchType.LAZY)
    private List<SavedJob> savedJobs;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
