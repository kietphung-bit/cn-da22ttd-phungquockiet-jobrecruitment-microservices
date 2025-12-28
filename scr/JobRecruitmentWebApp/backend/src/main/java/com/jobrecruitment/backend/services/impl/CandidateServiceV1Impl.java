package com.jobrecruitment.backend.services.impl;

import com.jobrecruitment.backend.dtos.request.CandidateProfileRequest;
import com.jobrecruitment.backend.dtos.response.CandidateResponse;
import com.jobrecruitment.backend.entities.Candidate;
import com.jobrecruitment.backend.entities.User;
import com.jobrecruitment.backend.exceptions.ResourceNotFoundException;
import com.jobrecruitment.backend.mappers.CandidateMapper;
import com.jobrecruitment.backend.repositories.CandidateRepository;
import com.jobrecruitment.backend.repositories.UserRepository;
import com.jobrecruitment.backend.services.CandidateServiceV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CandidateServiceV1Impl - Service triển khai logic nghiệp vụ cho Quản lý Hồ sơ Ứng viên (Version 1).
 * 
 * Chức năng chính:
 * - Xem thông tin hồ sơ ứng viên (Public/Employer/Admin access)
 * - Ứng viên xem hồ sơ của chính mình (Candidate only)
 * - Ứng viên cập nhật hồ sơ cá nhân (Candidate only)
 * 
 * Business Rules (RBHT, RBSDT, RBEML, RBNS):
 * - RBHT: Định dạng tên (chỉ chứa chữ cái và khoảng trắng, không có số hoặc ký tự đặc biệt)
 * - RBSDT: Số điện thoại (10-11 chữ số)
 * - RBEML: Định dạng email hợp lệ (phải chứa @ và domain)
 * - RBNS: Tuổi lao động (ngày sinh phải trong quá khứ, age >= 18)
 * 
 * Dependencies:
 * - CandidateRepository: Truy vấn database cho Candidate entity
 * - UserRepository: Xác định candidate từ username (JWT)
 * - CandidateMapper: Chuyển đổi Entity ↔ DTO
 * 
 * Transaction Management:
 * - @Transactional: Rollback nếu có lỗi
 * - @Transactional(readOnly = true): Query optimization cho read operations
 * 
 * @author JobRecruitment Development Team
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CandidateServiceV1Impl implements CandidateServiceV1 {

    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final CandidateMapper candidateMapper;

    /**
     * Lấy thông tin hồ sơ ứng viên theo ID (với IDOR Protection).
     * 
     * Endpoint có IDOR protection - cho phép Employer và Admin xem thông tin ứng viên
     * khi đánh giá hồ sơ ứng tuyển. Candidate chỉ có thể xem hồ sơ của chính mình.
     * 
     * Security - IDOR Protection:
     * - Admin (ADM): Full access to all candidates
     * - Employer (DN): Can view all candidates (for recruitment evaluation)
     * - Candidate (UV): Can only view own profile (ownership check)
     * - Unauthenticated: Denied (401 Unauthorized)
     * 
     * Quy trình xử lý:
     * 1. Tìm kiếm Candidate entity theo candidateId
     * 2. Nếu không tìm thấy: Throw ResourceNotFoundException
     * 3. Xác minh quyền truy cập (IDOR Protection)
     * 4. Chuyển đổi Entity sang DTO (CandidateResponse)
     * 5. Trả về thông tin hồ sơ ứng viên
     * 
     * @param candidateId ID của ứng viên cần xem
     * @param username Username của user đang authenticate (từ JWT token)
     * @return CandidateResponse chứa thông tin hồ sơ ứng viên
     * @throws ResourceNotFoundException Nếu không tìm thấy ứng viên với ID được cung cấp
     * @throws ValidationException Nếu user không có quyền truy cập (IDOR attempt blocked)
     */
    @Override
    @Transactional(readOnly = true)
    public CandidateResponse getCandidateById(Long candidateId, String username) {
        log.info("Fetching candidate with ID: {} for user: {}", candidateId, username);
        
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with ID: " + candidateId));
        
        // IDOR Protection: Verify user has permission to view this candidate
        verifyCandidateAccess(candidate, username);
        
        return candidateMapper.toResponse(candidate);
    }
    
    /**
     * Xác minh user có quyền truy cập candidate profile này không (IDOR Protection).
     * 
     * Quy tắc phân quyền:
     * 1. Admin (ADM): Có thể xem tất cả candidates (full access)
     * 2. Employer (DN): Có thể xem tất cả candidates (để đánh giá ứng viên)
     * 3. Candidate (UV): Chỉ xem profile của mình (candidate ID ownership check)
     * 
     * @param candidate Candidate entity cần kiểm tra
     * @param username Username của user đang authenticate
     * @throws ResourceNotFoundException Nếu không tìm thấy user/profile
     * @throws ValidationException Nếu user không có quyền truy cập (IDOR blocked)
     */
    private void verifyCandidateAccess(Candidate candidate, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        String roleCode = user.getRole().getRoleCode();
        
        // Admin có thể xem tất cả candidates
        if ("ADM".equals(roleCode)) {
            log.debug("Admin access granted for candidate {}", candidate.getCandidateId());
            return;
        }
        
        // Employer có thể xem tất cả candidates (để đánh giá ứng viên)
        if ("DN".equals(roleCode)) {
            log.debug("Employer access granted for candidate {}", candidate.getCandidateId());
            return;
        }
        
        // Candidate chỉ có thể xem profile của mình
        if ("UV".equals(roleCode)) {
            Candidate authenticatedCandidate = candidateRepository.findByUserUserId(user.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + username));
            
            if (!candidate.getCandidateId().equals(authenticatedCandidate.getCandidateId())) {
                log.warn("IDOR attempt blocked: Candidate {} tried to access candidate {} profile",
                        authenticatedCandidate.getCandidateId(), candidate.getCandidateId());
                throw new com.jobrecruitment.backend.exceptions.ValidationException(
                    "Access denied: You can only view your own profile");
            }
            log.debug("Candidate access granted for own profile {}", candidate.getCandidateId());
            return;
        }
        
        // Unknown role - deny access
        log.warn("Access denied: Unknown role {} for user {}", roleCode, username);
        throw new com.jobrecruitment.backend.exceptions.ValidationException("Access denied: Invalid role");
    }

    /**
     * Lấy thông tin hồ sơ của ứng viên đang đăng nhập.
     * 
     * Candidate-only endpoint - chỉ ứng viên mới có thể xem hồ sơ của chính mình.
     * 
     * Quy trình xử lý:
     * 1. Tìm kiếm User entity theo username (từ JWT token)
     * 2. Nếu không tìm thấy User: Throw ResourceNotFoundException
     * 3. Tìm Candidate entity liên kết với User (One-to-One relationship)
     * 4. Nếu không tìm thấy Candidate: Throw ResourceNotFoundException
     * 5. Chuyển đổi Entity sang DTO và trả về
     * 
     * @param username Username của ứng viên đang đăng nhập (lấy từ JWT)
     * @return CandidateResponse chứa thông tin hồ sơ đầy đủ
     * @throws ResourceNotFoundException Nếu không tìm thấy User hoặc Candidate profile
     */
    @Override
    @Transactional(readOnly = true)
    public CandidateResponse getMyProfile(String username) {
        log.info("Fetching candidate profile for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + username));
        
        return candidateMapper.toResponse(candidate);
    }

    /**
     * Cập nhật thông tin hồ sơ ứng viên.
     * 
     * Candidate-only endpoint - chỉ ứng viên mới có thể cập nhật hồ sơ của chính mình.
     * 
     * Quy trình xử lý:
     * 1. Tìm User entity theo username (từ JWT)
     * 2. Tìm Candidate entity liên kết với User
     * 3. Cập nhật các field được cung cấp (chỉ cập nhật field khác null):
     *    - CandidateName: Tên đầy đủ (validate RBHT: chỉ chữ cái và khoảng trắng)
     *    - CandidateDescription: Mô tả bản thân
     *    - CandidateGender: Giới tính (MALE/FEMALE/OTHER)
     *    - CandidateBirthdate: Ngày sinh (validate RBNS: age >= 18)
     *    - CandidatePhone: Số điện thoại (validate RBSDT: 10-11 chữ số)
     *    - CandidateEmail: Email cá nhân (validate RBEML: định dạng email)
     *    - CandidateEducation: Học vấn
     *    - CandidateExp: Kinh nghiệm làm việc
     *    - CandidateSkills: Kỹ năng
     * 4. Lưu Candidate entity vào database
     * 5. Chuyển đổi sang DTO và trả về
     * 
     * Lưu ý: Chỉ cập nhật các field có giá trị khác null (partial update)
     * 
     * @param request CandidateProfileRequest chứa thông tin cần cập nhật
     * @param username Username của ứng viên đang đăng nhập
     * @return CandidateResponse với thông tin đã cập nhật
     * @throws ResourceNotFoundException Nếu không tìm thấy User hoặc Candidate profile
     * @throws ValidationException Nếu dữ liệu không hợp lệ (RBHT, RBSDT, RBEML, RBNS)
     */
    @Override
    public CandidateResponse updateProfile(CandidateProfileRequest request, String username) {
        log.info("Updating candidate profile for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + username));
        
        // Update candidate fields (only non-null values)
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
        
        Candidate updatedCandidate = candidateRepository.save(candidate);
        log.info("Candidate profile updated successfully - CandidateID: {}", updatedCandidate.getCandidateId());
        
        return candidateMapper.toResponse(updatedCandidate);
    }
}
