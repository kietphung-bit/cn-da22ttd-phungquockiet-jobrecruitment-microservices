package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.dtos.request.CandidateProfileRequest;
import com.jobrecruitment.backend.dtos.response.CandidateResponse;

/**
 * CandidateServiceV1 - Service interface cho quản lý hồ sơ ứng viên
 * 
 * Chức năng:
 * - Candidate: Xem/cập nhật hồ sơ của mình
 * - Employer/Admin: Xem hồ sơ Candidate (public access)
 * 
 * Quy tắc nghiệp vụ:
 * - Mỗi User chỉ có 1 Candidate profile (One-to-One)
 * - Candidate chỉ cập nhật được hồ sơ của mình
 * 
 * @see CandidateServiceV1Impl
 */
public interface CandidateServiceV1 {
    
    /**
     * Lấy hồ sơ Candidate theo ID (với IDOR Protection)
     * 
     * Sử dụng:
     * - API GET /api/v1/candidates/{candidateId}
     * - Employer xem hồ sơ ứng viên đã nộp đơn
     * 
     * Security - IDOR Protection:
     * - Admin (ADM): Có thể xem tất cả candidates
     * - Employer (DN): Có thể xem tất cả candidates (để đánh giá ứng viên)
     * - Candidate (UV): Chỉ xem hồ sơ của mình
     * - Public/Unauthenticated: Deny (403 Forbidden)
     * 
     * @param candidateId Candidate ID
     * @param username Username của user đang authenticate (từ JWT token, null nếu không đăng nhập)
     * @return CandidateResponse
     * @throws ResourceNotFoundException nếu không tìm thấy Candidate
     * @throws ValidationException nếu user không có quyền truy cập (IDOR attempt)
     */
    CandidateResponse getCandidateById(Long candidateId, String username);
    
    /**
     * Lấy hồ sơ của Candidate đang authenticate (Candidate only)
     * 
     * Sử dụng:
     * - API GET /api/v1/candidates/profile
     * 
     * @param username Username của Candidate đang authenticate
     * @return CandidateResponse
     * @throws ResourceNotFoundException nếu không tìm thấy Candidate
     */
    CandidateResponse getMyProfile(String username);
    
    /**
     * Cập nhật hồ sơ Candidate (Candidate only - own profile)
     * 
     * Sử dụng:
     * - API PUT /api/v1/candidates/profile
     * 
     * Business Logic:
     * - Partial update: Chỉ cập nhật field khác null
     * - Validate: Email không trùng, WorkingAge >= 18 tuổi
     * 
     * @param request CandidateProfileRequest (fullName, birthDate, email, phone, address...)
     * @param username Username của Candidate đang authenticate
     * @return CandidateResponse (đã cập nhật)
     * @throws ValidationException nếu email trùng, tuổi < 18
     */
    CandidateResponse updateProfile(CandidateProfileRequest request, String username);
}
