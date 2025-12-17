package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.response.CVResponse;
import com.jobrecruitment.backend.entities.CV;

/**
 * CVMapper - Mapper cho CV entity
 * 
 * Mô tả:
 * - Chuyển đổi giữa CV entity và CVResponse DTO
 * - Đơn giản: Chỉ có Entity -> DTO (không có update method)
 * - Flatten: Chỉ lấy candidateId từ Candidate
 * 
 * Chiến lược mapping:
 * - toResponse: Map tất cả field từ CV, lấy candidateId từ nested Candidate
 * - cvFile: Path đến file CV trên server (vd: uploads/cv/UV12345678_20241225.pdf)
 */
@Component
public class CVMapper {
    
    /**
     * Chuyển đổi từ CV entity sang CVResponse DTO
     * 
     * Chiến lược:
     * - Map tất cả field của CV (cvFile, cvStatus, timestamps)
     * - Flatten nested Candidate: Chỉ lấy candidateId (không map toàn bộ CandidateResponse)
     * - Null-safe: Kiểm tra candidate != null trước khi lấy candidateId
     * 
     * @param cv CV entity
     * @return CVResponse DTO (null nếu input null)
     */
    public CVResponse toResponse(CV cv) {
        if (cv == null) {
            return null;
        }
        
        return new CVResponse(
            cv.getCvId(),
            cv.getCandidate() != null ? cv.getCandidate().getCandidateId() : null,
            cv.getCvCode(),
            cv.getCvFile(),
            cv.getCvStatus(),
            cv.getCreatedAt(),
            cv.getUpdatedAt()
        );
    }
}
