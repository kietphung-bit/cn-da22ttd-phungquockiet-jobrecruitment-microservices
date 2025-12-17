package com.jobrecruitment.backend.specifications;

import com.jobrecruitment.backend.entities.Application;
import com.jobrecruitment.backend.enums.ApplicationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * ApplicationSpecification - JPA Specification cho lọc động đơn ứng tuyển
 * 
 * Cung cấp xây dựng truy vấn an toàn kiểu cho thực thể Application với nhiều tiêu chí lọc.
 * Mỗi phương thức trả về một Specification có thể kết hợp sử dụng logic AND/OR.
 */
public class ApplicationSpecification {

    /**
     * Lọc đơn ứng tuyển theo trạng thái
     * 
     * @param status Trạng thái đơn ứng tuyển (PENDING, APPROVED, REJECTED)
     * @return Specification cho bộ lọc trạng thái
     */
    public static Specification<Application> hasStatus(ApplicationStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction(); // Always true
            }
            return criteriaBuilder.equal(root.get("applicationStatus"), status);
        };
    }

    /**
     * Lọc đơn ứng tuyển theo candidate ID
     * 
     * @param candidateId Candidate ID
     * @return Specification cho bộ lọc candidate
     */
    public static Specification<Application> hasCandidateId(Long candidateId) {
        return (root, query, criteriaBuilder) -> {
            if (candidateId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.join("cv").join("candidate").get("candidateId"), candidateId);
        };
    }

    /**
     * Lọc đơn ứng tuyển theo job ID
     * 
     * @param jobId Job ID
     * @return Specification cho bộ lọc công việc
     */
    public static Specification<Application> hasJobId(Long jobId) {
        return (root, query, criteriaBuilder) -> {
            if (jobId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("job").get("jobId"), jobId);
        };
    }

    /**
     * Lọc đơn ứng tuyển theo company ID (qua quan hệ Job)
     * 
     * @param companyId Company ID
     * @return Specification cho bộ lọc công ty
     */
    public static Specification<Application> hasCompanyId(Long companyId) {
        return (root, query, criteriaBuilder) -> {
            if (companyId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.join("job").join("company").get("companyId"), companyId);
        };
    }

    /**
     * Lọc đơn ứng tuyển theo khoảng thời gian nộp đơn
     * 
     * @param startTime Thời gian bắt đầu (bao gồm)
     * @param endTime Thời gian kết thúc (bao gồm)
     * @return Specification cho bộ lọc khoảng thời gian nộp đơn
     */
    public static Specification<Application> hasApplyTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return (root, query, criteriaBuilder) -> {
            if (startTime == null && endTime == null) {
                return criteriaBuilder.conjunction();
            }
            
            if (startTime != null && endTime != null) {
                return criteriaBuilder.between(root.get("applyTime"), startTime, endTime);
            } else if (startTime != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("applyTime"), startTime);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("applyTime"), endTime);
            }
        };
    }

    /**
     * Lọc đơn ứng tuyển kết hợp tất cả các tiêu chí
     * 
     * @param status Trạng thái đơn ứng tuyển
     * @param candidateId Candidate ID
     * @param jobId Job ID
     * @param companyId Company ID
     * @param startTime Start of apply time range
     * @param endTime End of apply time range
     * @return Combined specification using AND logic
     */
    public static Specification<Application> withFilters(
            ApplicationStatus status,
            Long candidateId,
            Long jobId,
            Long companyId,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        
        return Specification
                .where(hasStatus(status))
                .and(hasCandidateId(candidateId))
                .and(hasJobId(jobId))
                .and(hasCompanyId(companyId))
                .and(hasApplyTimeBetween(startTime, endTime));
    }
}
