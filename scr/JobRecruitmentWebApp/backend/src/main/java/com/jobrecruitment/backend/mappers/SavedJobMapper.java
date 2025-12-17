package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.response.SavedJobResponse;
import com.jobrecruitment.backend.entities.SavedJob;

import lombok.RequiredArgsConstructor;

/**
 * SavedJobMapper - Mapper cho SavedJob entity
 * 
 * Mô tả:
 * - Chuyển đổi giữa SavedJob entity và SavedJobResponse DTO
 * - Đơn giản: Chỉ có Entity -> DTO (không có update method)
 * - Nested mapping: Sử dụng JobMapper cho toàn bộ Job object
 * 
 * Chiến lược mapping:
 * - toResponse: Map tất cả field từ SavedJob + map toàn bộ Job qua JobMapper
 * - Flatten: Lấy candidateId, jobId (ID only)
 * - Nested full: Map toàn bộ JobResponse (để hiển thị thông tin Job trong danh sách lưu)
 */
@Component
@RequiredArgsConstructor
public class SavedJobMapper {
    
    /**
     * JobMapper: Để map toàn bộ Job object
     */
    private final JobMapper jobMapper;
    
    /**
     * Chuyển đổi từ SavedJob entity sang SavedJobResponse DTO
     * 
     * Chiến lược:
     * - Map tất cả field của SavedJob (sjId, candidateId, jobId, savedTime)
     * - Flatten nested: Lấy candidateId, jobId (ID only)
     * - Nested full: Map toàn bộ Job qua JobMapper (jobResponse field)
     * - Hiệu suất: Tránh N+1 query bằng cách JOIN FETCH Job trong repository
     * - Null-safe: Kiểm tra candidate, job != null trước khi lấy field
     * 
     * @param savedJob SavedJob entity
     * @return SavedJobResponse DTO (null nếu input null)
     */
    public SavedJobResponse toResponse(SavedJob savedJob) {
        if (savedJob == null) {
            return null;
        }
        
        return new SavedJobResponse(
            savedJob.getSjId(),
            savedJob.getCandidate() != null ? savedJob.getCandidate().getCandidateId() : null,
            savedJob.getJob() != null ? savedJob.getJob().getJobId() : null,
            jobMapper.toResponse(savedJob.getJob()),
            savedJob.getSavedTime()
        );
    }
}
