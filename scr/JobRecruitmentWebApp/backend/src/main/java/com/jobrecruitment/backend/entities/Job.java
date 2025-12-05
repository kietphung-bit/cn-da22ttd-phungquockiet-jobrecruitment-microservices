package com.jobrecruitment.backend.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.jobrecruitment.backend.enums.JobStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jc_id", nullable = false)
    private JobCategory jobCategory;

    @Column(unique = true, nullable = false, length = 10)
    private String jobCode; 

    @NotBlank(message = "Tiêu đề không được để trống")
    private String jobTitle;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    @Column(columnDefinition = "TEXT")
    private String jobRequirement;

    @Positive(message = "Mức lương phải là số dương") 
    private Double jobSalary;

    private String jobLocation;

    @NotNull
    private LocalDate startDate; 

    @NotNull
    private LocalDate endDate;

    @Min(value = 0, message = "Số lượng tuyển không được nhỏ hơn 0") 
    private Integer maxCandidates;

    @Enumerated(EnumType.STRING)
    private JobStatus jobStatus;

    @OneToMany(mappedBy = "job", fetch = FetchType.LAZY)
    private List<Application> applications;

    @OneToMany(mappedBy = "job", fetch = FetchType.LAZY)
    private List<SavedJob> savedJobs;

    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}