package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.request.JobRequest;
import com.jobrecruitment.backend.dtos.response.JobResponse;
import com.jobrecruitment.backend.entities.Job;

/**
 * JobMapper - Mapper cho Job entity
 * 
 * Mô tả:
 * - Chuyển đổi giữa Job entity và Job DTO
 * - 2 chiều: Entity -> Response DTO, Request DTO -> Entity (update)
 * - Flatten: Lấy companyName từ Company, jcName từ JobCategory
 * 
 * Chiến lược mapping:
 * - toResponse: Map tất cả field từ Job + flatten nested Company, JobCategory
 * - updateEntityFromRequest: Chỉ update các field khác null (partial update)
 * - Nested entities: Extract các field quan trọng (companyId, companyName, jcId, jcName)
 */
@Component
public class JobMapper {
    
    /**
     * Chuyển đổi từ Job entity sang JobResponse DTO
     * 
     * Chiến lược:
     * - Map tất cả field của Job (title, description, salary, location, status...)
     * - Flatten nested Company: Lấy companyId + companyName (để hiển thị tên công ty)
     * - Flatten nested JobCategory: Lấy jcId + jcName (để hiển thị ngành nghề)
     * - Null-safe: Kiểm tra company, jobCategory != null trước khi lấy field
     * - Performance: Tránh N+1 query bằng cách JOIN FETCH trong repository
     * 
     * @param job Job entity
     * @return JobResponse DTO (null nếu input null)
     */
    public JobResponse toResponse(Job job) {
        if (job == null) {
            return null;
        }
        
        return new JobResponse(
            job.getJobId(),
            job.getCompany() != null ? job.getCompany().getCompanyId() : null,
            job.getCompany() != null ? job.getCompany().getCompanyName() : null,
            job.getCompany() != null ? job.getCompany().getLogoURL() : null,
            job.getJobCategory() != null ? job.getJobCategory().getJcId() : null,
            job.getJobCategory() != null ? job.getJobCategory().getJcName() : null,
            job.getJobCode(),
            job.getJobTitle(),
            job.getJobDescription(),
            job.getJobRequirement(),
            job.getJobResponsibilities(),
            job.getJobBenefits(),
            job.getJobSalary(),
            job.getJobLocation(),
            job.getStartDate(),
            job.getEndDate(),
            job.getMaxCandidates(),
            job.getJobStatus(),
            job.getCreatedAt(),
            job.getUpdatedAt()
        );
    }
    
    /**
     * Cập nhật Job entity từ JobRequest DTO
     * 
     * Sử dụng:
     * - API PUT /api/v1/jobs/{jobId} (cập nhật thông tin tin tuyển dụng)
     * 
     * Chiến lược:
     * - Partial update: Chỉ cập nhật các field khác null trong request
     * - Preserve: Field nào null trong request thì giữ nguyên giá trị cũ
     * - Không update: jobId, company, jobCode, jobStatus, createdAt (immutable)
     * - JobCategory: Cập nhật qua jobCategoryId (không phải jobCategory object)
     * 
     * @param job Entity cần cập nhật (modified in-place)
     * @param request DTO chứa dữ liệu mới
     */
    public void updateEntityFromRequest(Job job, JobRequest request) {
        if (request.getJobTitle() != null) {
            job.setJobTitle(request.getJobTitle());
        }
        if (request.getJobDescription() != null) {
            job.setJobDescription(request.getJobDescription());
        }
        if (request.getJobRequirement() != null) {
            job.setJobRequirement(request.getJobRequirement());
        }
        if (request.getJobResponsibilities() != null) {
            job.setJobResponsibilities(request.getJobResponsibilities());
        }
        if (request.getJobBenefits() != null) {
            job.setJobBenefits(request.getJobBenefits());
        }
        if (request.getJobSalary() != null) {
            job.setJobSalary(request.getJobSalary());
        }
        if (request.getJobLocation() != null) {
            job.setJobLocation(request.getJobLocation());
        }
        if (request.getStartDate() != null) {
            job.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            job.setEndDate(request.getEndDate());
        }
        if (request.getMaxCandidates() != null) {
            job.setMaxCandidates(request.getMaxCandidates());
        }
        if (request.getJobStatus() != null) {
            job.setJobStatus(request.getJobStatus());
        }
    }
}
