package com.jobrecruitment.backend.mappers;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.request.CandidateProfileRequest;
import com.jobrecruitment.backend.dtos.response.CandidateResponse;
import com.jobrecruitment.backend.entities.Candidate;

/**
 * CandidateMapper - Mapper cho Candidate entity
 * 
 * Mô tả:
 * - Chuyển đổi giữa Candidate entity và Candidate DTO
 * - 2 chiều: Entity -> Response DTO, Request DTO -> Entity (update)
 * - Flatten: Chỉ lấy userId từ User (không map toàn bộ User object)
 * 
 * Chiến lược mapping:
 * - toResponse: Map tất cả field từ Candidate, lấy userId từ nested User
 * - updateEntityFromRequest: Chỉ update các field khác null (partial update)
 */
@Component
public class CandidateMapper {
    
    /**
     * Chuyển đổi từ Candidate entity sang CandidateResponse DTO
     * 
     * Chiến lược:
     * - Map tất cả field của Candidate
     * - Flatten nested User: Chỉ lấy userId (không map toàn bộ UserResponse)
     * - Null-safe: Kiểm tra user != null trước khi lấy userId
     * 
     * @param candidate Candidate entity
     * @return CandidateResponse DTO (null nếu input null)
     */
    public CandidateResponse toResponse(Candidate candidate) {
        if (candidate == null) {
            return null;
        }
        
        return new CandidateResponse(
            candidate.getCandidateId(),
            candidate.getUser() != null ? candidate.getUser().getUserId() : null,
            candidate.getCandidateCode(),
            candidate.getCandidateName(),
            candidate.getCandidateDescription(),
            candidate.getCandidateGender(),
            candidate.getCandidateBirthdate(),
            candidate.getCandidatePhone(),
            candidate.getCandidateEmail(),
            candidate.getCandidateEducation(),
            candidate.getCandidateExp(),
            candidate.getCandidateSkills(),
            candidate.getCreatedAt(),
            candidate.getUpdatedAt()
        );
    }
    
    /**
     * Cập nhật Candidate entity từ CandidateProfileRequest DTO
     * 
     * Sử dụng:
     * - API PUT /api/v1/candidates/profile (update hồ sơ)
     * 
     * Chiến lược:
     * - Partial update: Chỉ cập nhật các field khác null trong request
     * - Preserve: Field nào null trong request thì giữ nguyên giá trị cũ
     * - Không update: candidateId, user, candidateCode, createdAt (immutable)
     * 
     * @param candidate Entity cần cập nhật (modified in-place)
     * @param request DTO chứa dữ liệu mới
     */
    public void updateEntityFromRequest(Candidate candidate, CandidateProfileRequest request) {
        if (request.getCandidateName() != null) {
            candidate.setCandidateName(request.getCandidateName());
        }
        if (request.getCandidateDescription() != null) {
            candidate.setCandidateDescription(request.getCandidateDescription());
        }
        if (request.getCandidateGender() != null) {
            candidate.setCandidateGender(request.getCandidateGender());
        }
        if (request.getCandidateBirthdate() != null) {
            candidate.setCandidateBirthdate(request.getCandidateBirthdate());
        }
        if (request.getCandidatePhone() != null) {
            candidate.setCandidatePhone(request.getCandidatePhone());
        }
        if (request.getCandidateEmail() != null) {
            candidate.setCandidateEmail(request.getCandidateEmail());
        }
        if (request.getCandidateEducation() != null) {
            candidate.setCandidateEducation(request.getCandidateEducation());
        }
        if (request.getCandidateExp() != null) {
            candidate.setCandidateExp(request.getCandidateExp());
        }
        if (request.getCandidateSkills() != null) {
            candidate.setCandidateSkills(request.getCandidateSkills());
        }
    }
}
