package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.request.JobCategoryRequest;
import com.jobrecruitment.backend.dtos.response.JobCategoryResponse;
import com.jobrecruitment.backend.entities.JobCategory;

/**
 * JobCategoryMapper - Mapper cho JobCategory entity
 * 
 * Mô tả:
 * - Chuyển đổi giữa JobCategory entity và JobCategory DTO
 * - 2 chiều: Entity -> Response DTO, Request DTO -> Entity (tạo mới)
 * - Master data: JobCategory là dữ liệu danh mục (IT, Marketing, Finance...)
 * 
 * Chiến lược mapping:
 * - toResponse: Map tất cả field từ JobCategory
 * - toEntity: Tạo JobCategory mới từ request (chỉ dùng khi tạo mới)
 */
@Component
public class JobCategoryMapper {
    
    /**
     * Chuyển đổi từ JobCategory entity sang JobCategoryResponse DTO
     * 
     * Chiến lược:
     * - Map tất cả field của JobCategory (jcName, jcDescription, jcBaseSalary)
     * - jcBaseSalary: Lương cơ sở trung bình của ngành (dùng để gợi ý)
     * - Không map jobs: Tránh over-fetching danh sách Job (có thể hàng nghìn)
     * 
     * @param jobCategory JobCategory entity
     * @return JobCategoryResponse DTO (null nếu input null)
     */
    public JobCategoryResponse toResponse(JobCategory jobCategory) {
        if (jobCategory == null) {
            return null;
        }
        
        return new JobCategoryResponse(
            jobCategory.getJcId(),
            jobCategory.getJcName(),
            jobCategory.getJcDescription(),
            jobCategory.getJcBaseSalary(),
            jobCategory.getCreatedAt(),
            jobCategory.getUpdatedAt()
        );
    }
    
    /**
     * Tạo JobCategory entity mới từ JobCategoryRequest DTO
     * 
     * Sử dụng:
     * - API POST /api/v1/admin/categories (tạo ngành nghề mới)
     * 
     * Chiến lược:
     * - Map tất cả field từ request sang entity mới
     * - Auto-generated: jcId (MySQL AUTO_INCREMENT), timestamps (JPA @PrePersist)
     * - Validation: jcName unique (đã check trong service)
     * 
     * @param request DTO chứa dữ liệu ngành nghề mới
     * @return JobCategory entity mới (null nếu input null)
     */
    public JobCategory toEntity(JobCategoryRequest request) {
        if (request == null) {
            return null;
        }
        
        JobCategory jobCategory = new JobCategory();
        jobCategory.setJcName(request.getJcName());
        jobCategory.setJcDescription(request.getJcDescription());
        jobCategory.setJcBaseSalary(request.getJcBaseSalary());
        
        return jobCategory;
    }
    
    /**
     * Cập nhật JobCategory entity từ JobCategoryRequest
     */
    public void updateEntityFromRequest(JobCategory jobCategory, JobCategoryRequest request) {
        if (request.getJcName() != null) {
            jobCategory.setJcName(request.getJcName());
        }
        if (request.getJcDescription() != null) {
            jobCategory.setJcDescription(request.getJcDescription());
        }
        if (request.getJcBaseSalary() != null) {
            jobCategory.setJcBaseSalary(request.getJcBaseSalary());
        }
    }
}
