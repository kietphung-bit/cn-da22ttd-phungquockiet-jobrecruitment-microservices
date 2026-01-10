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
     * - Flatten nested CV: Lấy cvId + cvCode + cvFile (để download/view CV)
     * - Flatten nested Candidate: Lấy candidateId, candidateName, email, phone từ CV.candidate
     * - Null-safe: Kiểm tra job, cv, candidate != null trước khi lấy field
     * 
     * @param application Application entity
     * @return ApplicationResponse DTO (null nếu input null)
     */
    public ApplicationResponse toResponse(Application application) {
        if (application == null) {
            return null;
        }
        
        // Trích xuất thông tin ứng viên từ CV
        Long candidateId = null;
        String candidateName = null;
        String candidateEmail = null;
        String candidatePhone = null;
        
        if (application.getCv() != null && application.getCv().getCandidate() != null) {
            candidateId = application.getCv().getCandidate().getCandidateId();
            candidateName = application.getCv().getCandidate().getCandidateName();
            candidateEmail = application.getCv().getCandidate().getCandidateEmail();
            candidatePhone = application.getCv().getCandidate().getCandidatePhone();
        }
        
        ApplicationResponse response = new ApplicationResponse();
        response.setApplicationId(application.getApplicationId());
        response.setJobId(application.getJob() != null ? application.getJob().getJobId() : null);
        response.setJobTitle(application.getJob() != null ? application.getJob().getJobTitle() : null);
        response.setCvId(application.getCv() != null ? application.getCv().getCvId() : null);
        response.setCvCode(application.getCv() != null ? application.getCv().getCvCode() : null);
        response.setCvFile(application.getCv() != null ? application.getCv().getCvFile() : null);
        response.setCandidateId(candidateId);
        response.setCandidateName(candidateName);
        response.setCandidateEmail(candidateEmail);
        response.setCandidatePhone(candidatePhone);
        response.setApplicationCode(application.getApplicationCode());
        response.setApplyTime(application.getApplyTime());
        response.setApplicationStatus(application.getApplicationStatus());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        
        return response;
    }
}
