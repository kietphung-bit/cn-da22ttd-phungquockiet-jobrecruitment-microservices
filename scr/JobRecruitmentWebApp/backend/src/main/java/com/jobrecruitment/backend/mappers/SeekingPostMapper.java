package com.jobrecruitment.backend.mappers;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.jobrecruitment.backend.dtos.response.JobSeekPostResponse;
import com.jobrecruitment.backend.entities.SeekingPost;

/**
 * SeekingPostMapper - Mapper cho SeekingPost entity
 * 
 * Mô tả:
 * - Chuyển đổi giữa SeekingPost entity và JobSeekPostResponse DTO
 * - Hỗ trợ 2 chế độ: Full data (Employer) và Masked data (Guest/Candidate)
 * 
 * Chiến lược mapping:
 * - toFullResponse: Map đầy đủ thông tin (cho Employer)
 * - toMaskedResponse: Map với thông tin bị che (cho Guest/Candidate)
 * 
 * Privacy Rules:
 * - Full: candidateName, candidateEmail, candidatePhone (từ Candidate entity)
 * - Masked: candidateName="Nguyễn Văn ***", candidateEmail=null, candidatePhone=null
 */
@Component
public class SeekingPostMapper {
    
    /**
     * Chuyển đổi từ SeekingPost entity sang JobSeekPostResponse DTO (Full Data)
     * 
     * Dành cho: Employer (ROLE_DN)
     * 
     * Chiến lược:
     * - Map tất cả field của SeekingPost
     * - Map thông tin ứng viên đầy đủ (name, email, phone)
     * - skills: Chuyển từ comma-separated string sang List<String>
     * 
     * @param post SeekingPost entity
     * @return JobSeekPostResponse DTO với đầy đủ thông tin
     */
    public JobSeekPostResponse toFullResponse(SeekingPost post) {
        if (post == null) {
            return null;
        }
        
        // Chuyển skills từ chuỗi phân tách bằng dấu phẩy sang List
        List<String> skillsList = post.getSkPostSkills() != null 
            ? Arrays.asList(post.getSkPostSkills().split(","))
            : List.of();
        
        return JobSeekPostResponse.builder()
            .id(post.getSkPostId())
            .title(post.getSkPostTitle())
            .desiredSalary(post.getDesiredSalary())
            .location(post.getDesiredLocation())
            .skills(skillsList)
            .introduction(post.getSkPostIntro())
            .expiryDate(post.getExpiryDate())
            // Hiển thị thông tin ứng viên đầy đủ (cho Employer)
            .candidateName(post.getCandidate() != null ? post.getCandidate().getCandidateName() : null)
            .candidateAvatar(null) 
            .createdDate(post.getCreatedAt())
            .status(post.getSkPostStatus() != null ? post.getSkPostStatus().name() : null)
            .build();
    }
    
    /**
     * Chuyển đổi từ SeekingPost entity sang JobSeekPostResponse DTO (Masked Data)
     * 
     * Dành cho: Guest (không đăng nhập) hoặc Candidate (ROLE_UV)
     * 
     * Privacy Logic:
     * - candidateName: Nguyễn Văn A → Nguyễn Văn ***
     * - candidateEmail: null
     * - candidatePhone: null
     * 
     * @param post SeekingPost entity
     * @return JobSeekPostResponse DTO với thông tin bị che
     */
    public JobSeekPostResponse toMaskedResponse(SeekingPost post) {
        if (post == null) {
            return null;
        }
        
        // Chuyển skills từ chuỗi phân tách bằng dấu phẩy sang List
        List<String> skillsList = post.getSkPostSkills() != null 
            ? Arrays.asList(post.getSkPostSkills().split(","))
            : List.of();
        
        // Che tên ứng viên: "Nguyễn Văn An" -> "Nguyễn Văn ***"
        String maskedName = maskCandidateName(
            post.getCandidate() != null ? post.getCandidate().getCandidateName() : null
        );
        
        return JobSeekPostResponse.builder()
            .id(post.getSkPostId())
            .title(post.getSkPostTitle())
            .desiredSalary(post.getDesiredSalary())
            .location(post.getDesiredLocation())
            .skills(skillsList)
            .introduction(post.getSkPostIntro())
            .expiryDate(post.getExpiryDate())
            // Thông tin ứng viên bị che (cho Guest/Candidate)
            .candidateName(maskedName)
            .candidateAvatar(null)
            .createdDate(post.getCreatedAt())
            .status(null) // Ẩn trạng thái để bảo mật
            .build();
    }
    
    /**
     * Mask candidate name for privacy
     * 
     * Logic:
     * - "Nguyễn Văn An" → "Nguyễn Văn ***"
     * - "Trần Thị B" → "Trần Thị ***"
     * - "John Doe" → "John ***"
     * 
     * @param fullName Tên đầy đủ
     * @return Tên đã được che
     */
    private String maskCandidateName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Ứng viên ***";
        }
        
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length <= 1) {
            return parts[0] + " ***";
        }
        
        // Giữ lại tất cả các phần trừ phần cuối cùng
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            masked.append(parts[i]).append(" ");
        }
        masked.append("***");
        
        return masked.toString();
    }
}
