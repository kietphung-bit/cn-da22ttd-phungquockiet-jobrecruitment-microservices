package com.jobrecruitment.backend.specifications;

import com.jobrecruitment.backend.entities.Job;
import com.jobrecruitment.backend.enums.JobStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JobSpecification - Dynamic Query Builder cho thực thể Job
 * 
 * Implements Spring Data JPA Specification pattern cho phép xây dựng truy vấn động
 * dựa trên nhiều tiêu chí lọc khác nhau.
 * Hỗ trợ tìm kiếm theo chuỗi một phần, lọc theo trạng thái, và tìm kiếm theo địa điểm.
 * 
 * Hướng dẫn sử dụng:
 * Specification<Job> spec = JobSpecification.builder()
 *     .withTitle("Java Developer")
 *     .withStatus(JobStatus.ACTIVE)
 *     .withLocation("Hanoi")
 *     .build();
 * Page<Job> results = jobRepository.findAll(spec, pageable);
 */
public class JobSpecification {

    /**
     * Lọc theo tiêu đề công việc (Tìm kiếm một phần - Không phân biệt hoa thường)
     * 
     * @param jobTitle Từ khóa tìm kiếm tiêu đề công việc
     * @return Specification khớp với các công việc có tiêu đề chứa từ khóa
     */
    public static Specification<Job> hasTitle(String jobTitle) {
        return (root, query, criteriaBuilder) -> {
            if (jobTitle == null || jobTitle.trim().isEmpty()) {
                return criteriaBuilder.conjunction(); // Always true (no filter)
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("jobTitle")),
                "%" + jobTitle.toLowerCase().trim() + "%"
            );
        };
    }

    /**
     * Lọc theo trạng thái công việc (Khớp chính xác)
     * 
     * @param jobStatus Trạng thái công việc (PENDING, ACTIVE, CLOSED, HIDDEN, WAIT)
     * @return Specification khớp với các công việc có trạng thái chính xác
     */
    public static Specification<Job> hasStatus(JobStatus jobStatus) {
        return (root, query, criteriaBuilder) -> {
            if (jobStatus == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("jobStatus"), jobStatus);
        };
    }

    /**
     * Lọc theo địa điểm công việc (Tìm kiếm một phần - Không phân biệt hoa thường)
     * 
     * @param jobLocation Từ khóa tìm kiếm địa điểm
     * @return Specification khớp với các công việc có địa điểm chứa từ khóa
     */
    public static Specification<Job> hasLocation(String jobLocation) {
        return (root, query, criteriaBuilder) -> {
            if (jobLocation == null || jobLocation.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("jobLocation")),
                "%" + jobLocation.toLowerCase().trim() + "%"
            );
        };
    }

    /**
     * Lọc theo Company ID (Khớp chính xác)
     * 
     * @param companyId Company ID để lọc công việc
     * @return Specification khớp với các công việc được đăng bởi công ty cụ thể
     */
    public static Specification<Job> hasCompanyId(Long companyId) {
        return (root, query, criteriaBuilder) -> {
            if (companyId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("company").get("companyId"), companyId);
        };
    }

    /**
     * Lọc theo Job Category ID (Khớp chính xác)
     * 
     * @param jcId Job Category ID
     * @return Specification khớp với các công việc thuộc danh mục cụ thể
     */
    public static Specification<Job> hasCategoryId(Integer jcId) {
        return (root, query, criteriaBuilder) -> {
            if (jcId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("jobCategory").get("jcId"), jcId);
        };
    }

    /**
     * Lọc theo khoảng lương (Tối thiểu và Tối đa)
     * 
     * @param minSalary Lương tối thiểu (bao gồm)
     * @param maxSalary Lương tối đa (bao gồm)
     * @return Specification khớp với các công việc trong khoảng lương cho trước
     */
    public static Specification<Job> hasSalaryBetween(Double minSalary, Double maxSalary) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (minSalary != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("jobSalary"), minSalary));
            }
            
            if (maxSalary != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("jobSalary"), maxSalary));
            }
            
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Lọc  - Kết hợp nhiều tiêu chí
     * 
     * Xây dựng một specification động bằng cách kết hợp tất cả các bộ lọc được cung cấp sử dụng logic AND.
     * 
     * @param jobTitle Từ khóa tiêu đề công việc (có thể null)
     * @param jobStatus Trạng thái công việc (có thể null)
     * @param jobLocation Từ khóa địa điểm công việc (có thể null)
     * @param companyId Company ID (có thể null)
     * @param jcId Job Category ID (có thể null)
     * @param minSalary Lương tối thiểu (có thể null)
     * @param maxSalary Lương tối đa (có thể null)
     * @return Kết hợp specification với tất cả các bộ lọc đang hoạt động
     */
    public static Specification<Job> withFilters(
            String jobTitle,
            JobStatus jobStatus,
            String jobLocation,
            Long companyId,
            Integer jcId,
            Double minSalary,
            Double maxSalary
    ) {
        return Specification.where(hasTitle(jobTitle))
                .and(hasStatus(jobStatus))
                .and(hasLocation(jobLocation))
                .and(hasCompanyId(companyId))
                .and(hasCategoryId(jcId))
                .and(hasSalaryBetween(minSalary, maxSalary));
    }
}
