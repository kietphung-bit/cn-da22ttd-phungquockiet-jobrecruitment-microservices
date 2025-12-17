package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.response.ApplicationResponse;
import com.jobrecruitment.backend.entities.Application;

/**
 * ApplicationMapper - Mapper cho Application entity
 * 
 * Mô tả:
 * - Chuyển đổi giữa Application entity và ApplicationResponse DTO
 * - Đơn giản: Chỉ có Entity -> DTO (không có update method)
 * - Flatten: Lấy jobId + jobTitle từ Job, cvId + cvCode từ CV
 * 
 * Chiến lược mapping:
 * - toResponse: Map tất cả field từ Application + flatten nested Job, CV
 * - Nested entities: Extract các field quan trọng từ Job và CV (không map toàn bộ)
 */
@Component
public class ApplicationMapper {
    
    /**
     * Chuyển đổi từ Application entity sang ApplicationResponse DTO
     * 
     * Chiến lược:
     * - Map tất cả field của Application (applicationCode, applyTime, status)
     * - Flatten nested Job: Lấy jobId + jobTitle (để hiển thị vị trí đã ứng tuyển)
     * - Flatten nested CV: Lấy cvId + cvCode (để biết CV nào được sử dụng)
     * - Null-safe: Kiểm tra job, cv != null trước khi lấy field
     * 
     * @param application Application entity
     * @return ApplicationResponse DTO (null nếu input null)
     */
    public ApplicationResponse toResponse(Application application) {
        if (application == null) {
            return null;
        }
        
        return new ApplicationResponse(
            application.getApplicationId(),
            application.getJob() != null ? application.getJob().getJobId() : null,
            application.getJob() != null ? application.getJob().getJobTitle() : null,
            application.getCv() != null ? application.getCv().getCvId() : null,
            application.getCv() != null ? application.getCv().getCvCode() : null,
            application.getApplicationCode(),
            application.getApplyTime(),
            application.getApplicationStatus(),
            application.getCreatedAt(),
            application.getUpdatedAt()
        );
    }
}
