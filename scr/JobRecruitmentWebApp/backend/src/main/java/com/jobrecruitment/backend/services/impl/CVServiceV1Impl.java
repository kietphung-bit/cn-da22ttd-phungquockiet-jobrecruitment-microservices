package com.jobrecruitment.backend.services.impl;

import com.jobrecruitment.backend.dtos.response.CVResponse;
import com.jobrecruitment.backend.entities.CV;
import com.jobrecruitment.backend.entities.Candidate;
import com.jobrecruitment.backend.entities.User;
import com.jobrecruitment.backend.enums.CVStatus;
import com.jobrecruitment.backend.exceptions.ResourceNotFoundException;
import com.jobrecruitment.backend.mappers.CVMapper;
import com.jobrecruitment.backend.repositories.CVRepository;
import com.jobrecruitment.backend.repositories.CandidateRepository;
import com.jobrecruitment.backend.repositories.UserRepository;
import com.jobrecruitment.backend.services.CVServiceV1;
import com.jobrecruitment.backend.services.FileStorageService;
import com.jobrecruitment.backend.utils.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CVServiceV1Impl - Service triển khai logic nghiệp vụ cho Quản lý CV (Version 1).
 * 
 * Chức năng chính:
 * - Ứng viên tải lên CV (Candidate only)
 * - Ứng viên xem danh sách CV của mình (Candidate only)
 * - Ứng viên cập nhật trạng thái CV (ACTIVE/HIDDEN) (Candidate only)
 * - Ứng viên xóa CV (Soft delete) (Candidate only)
 * 
 * Business Rules (RBCV):
 * - RBCV: CV Status phải là ACTIVE hoặc HIDDEN
 * - CVCode: Auto-generated theo format "CV" + 8 chữ số (unique)
 * - Ownership: Ứng viên chỉ quản lý CV của chính mình
 * - Soft Delete: Không xóa vĩnh viễn, chỉ set CVStatus = HIDDEN
 * 
 * Dependencies:
 * - CVRepository: Truy vấn database cho CV entity
 * - CandidateRepository: Xác định candidate từ user
 * - UserRepository: Xác định user từ username (JWT)
 * - CodeGenerator: Tạo CVCode unique
 * - CVMapper: Chuyển đổi Entity ↔ DTO
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
public class CVServiceV1Impl implements CVServiceV1 {

    private final CVRepository cvRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final CodeGenerator codeGenerator;
    private final CVMapper cvMapper;
    private final FileStorageService fileStorageService;

    /**
     * Tạo mới CV cho ứng viên.
     * 
     * Candidate-only endpoint - chỉ ứng viên mới có thể tải lên CV.
     * 
     * Quy trình xử lý:
     * 1. Tìm User entity theo username (từ JWT)
     * 2. Tìm Candidate entity liên kết với User
     * 3. Upload file CV vào uploads/cvs/ bằng FileStorageService
     * 4. Tạo CVCode unique theo format "CV" + 8 chữ số ngẫu nhiên
     * 5. Kiểm tra uniqueness trong database (retry nếu trùng)
     * 6. Tạo CV entity:
     *    - Candidate: Liên kết với ứng viên
     *    - CVCode: Mã được tạo tự động
     *    - CVFile: Đường dẫn file CV (ví dụ: uploads/cvs/uuid-resume.pdf)
     *    - CVStatus: ACTIVE (mặc định)
     * 7. Lưu CV entity vào database
     * 8. Chuyển đổi sang DTO và trả về
     * 
     * @param file MultipartFile từ form upload (PDF/DOCX, max 10MB)
     * @param username Username của ứng viên đang đăng nhập
     * @return CVResponse chứa thông tin CV vừa tạo (bao gồm CVCode)
     * @throws ResourceNotFoundException Nếu không tìm thấy User hoặc Candidate profile
     * @throws FileStorageException Nếu file không hợp lệ hoặc lỗi khi lưu
     */
    @Override
    @Transactional
    public CVResponse createCV(MultipartFile file, String username) {
        log.info("Creating CV for user: {}", username);
        
        // Get authenticated user and their candidate profile
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + username));

        // Upload file to uploads/cvs/ directory
        String cvFilePath = fileStorageService.storeFile(file, "cvs");
        log.info("File uploaded successfully: {}", cvFilePath);

        // Generate unique CVCode
        String cvCode = codeGenerator.generateCVCode(code -> cvRepository.existsByCvCode(code));
        log.info("Generated CVCode: {}", cvCode);

        // Create CV entity
        CV cv = new CV();
        cv.setCandidate(candidate);
        cv.setCvCode(cvCode);
        cv.setCvFile(cvFilePath); // Store file path (e.g., uploads/cvs/uuid-resume.pdf)
        cv.setCvStatus(CVStatus.ACTIVE);

        CV savedCV = cvRepository.save(cv);
        log.info("CV created successfully - CVCode: {}", savedCV.getCvCode());
        
        return cvMapper.toResponse(savedCV);
    }

    /**
     * Lấy danh sách CV của ứng viên đang đăng nhập (bao gồm ACTIVE và HIDDEN).
     * 
     * Candidate-only endpoint - chỉ ứng viên mới có thể xem danh sách CV của chính mình.
     * 
     * Quy trình xử lý:
     * 1. Tìm User entity theo username (từ JWT)
     * 2. Tìm Candidate entity liên kết với User
     * 3. Lấy tất cả CV entity của candidate
     * 4. Lọc lấy CV có trạng thái ACTIVE hoặc HIDDEN (loại bỏ DELETED)
     * 5. Chuyển đổi List<CV> sang List<CVResponse>
     * 6. Trả về danh sách CV
     * 
     * Lưu ý: 
     * - Trả về cả CV ACTIVE và HIDDEN để ứng viên quản lý (toggle trạng thái)
     * - CV DELETED sẽ không hiển thị (xóa vĩnh viễn)
     * 
     * @param username Username của ứng viên đang đăng nhập
     * @return List<CVResponse> chứa các CV ACTIVE và HIDDEN của ứng viên
     * @throws ResourceNotFoundException Nếu không tìm thấy User hoặc Candidate profile
     */
    @Override
    @Transactional(readOnly = true)
    public List<CVResponse> getMyCVs(String username) {
        log.info("Fetching CVs for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + username));
        
        List<CV> cvs = cvRepository.findByCandidateCandidateId(candidate.getCandidateId());
        log.info("Found {} CVs (total) for candidate: {}", cvs.size(), candidate.getCandidateCode());
        
        // Filter to return ACTIVE and HIDDEN CVs (exclude DELETED ones)
        List<CVResponse> managedCVs = cvs.stream()
                .filter(cv -> cv.getCvStatus() != CVStatus.DELETED)
                .map(cvMapper::toResponse)
                .collect(Collectors.toList());
        
        log.info("Returning {} manageable CVs (ACTIVE + HIDDEN)", managedCVs.size());
        return managedCVs;
    }

    /**
     * Cập nhật trạng thái CV (ACTIVE/HIDDEN).
     * 
     * Candidate-only endpoint - chỉ ứng viên mới có thể cập nhật trạng thái CV của chính mình.
     * 
     * Quy trình xử lý:
     * 1. Tìm User entity theo username (từ JWT)
     * 2. Tìm Candidate entity liên kết với User
     * 3. Tìm CV entity theo cvId
     * 4. Kiểm tra ownership: CV phải thuộc về ứng viên đang đăng nhập
     *    - Nếu không: Throw AccessDeniedException
     * 5. Cập nhật CVStatus với giá trị mới (ACTIVE/HIDDEN)
     * 6. Lưu CV entity vào database
     * 7. Chuyển đổi sang DTO và trả về
     * 
     * Business Rule (RBCV): CVStatus chỉ có thể là ACTIVE hoặc HIDDEN
     * 
     * @param cvId ID của CV cần cập nhật
     * @param newStatus Trạng thái mới (ACTIVE hoặc HIDDEN)
     * @param username Username của ứng viên đang đăng nhập
     * @return CVResponse với trạng thái đã cập nhật
     * @throws ResourceNotFoundException Nếu không tìm thấy User, Candidate hoặc CV
     * @throws AccessDeniedException Nếu ứng viên cố cập nhật CV không phải của mình
     */
    @Override
    @Transactional
    public CVResponse updateCVStatus(Long cvId, CVStatus newStatus, String username) {
        log.info("Updating CV status - CVId: {}, NewStatus: {}", cvId, newStatus);
        
        // Get authenticated user and their candidate profile
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + username));

        // Get CV
        CV cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new ResourceNotFoundException("CV not found with ID: " + cvId));

        // Verify ownership
        if (!cv.getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            throw new AccessDeniedException("You can only update your own CVs");
        }

        // Update status
        cv.setCvStatus(newStatus);
        CV updatedCV = cvRepository.save(cv);
        
        log.info("CV status updated successfully - CVCode: {}", updatedCV.getCvCode());
        return cvMapper.toResponse(updatedCV);
    }

    /**
     * Xóa CV vĩnh viễn (Permanent Delete).
     * 
     * Candidate-only endpoint - chỉ ứng viên mới có thể xóa CV của chính mình.
     * 
     * Quy trình xử lý:
     * 1. Tìm User entity theo username (từ JWT)
     * 2. Tìm Candidate entity liên kết với User
     * 3. Tìm CV entity theo cvId
     * 4. Kiểm tra ownership: CV phải thuộc về ứng viên đang đăng nhập
     *    - Nếu không: Throw AccessDeniedException
     * 5. Xóa file vật lý (nếu tồn tại) khỏi uploads/cvs/
     * 6. Set CVStatus = DELETED (không hiển thị trong danh sách)
     * 7. Lưu CV entity vào database
     * 
     * Lưu ý: 
     * - CV vẫn trong database với status DELETED (audit trail)
     * - File vật lý được xóa khỏi disk
     * - CV không hiển thị trong getMyCVs()
     * 
     * @param cvId ID của CV cần xóa
     * @param username Username của ứng viên đang đăng nhập
     * @throws ResourceNotFoundException Nếu không tìm thấy User, Candidate hoặc CV
     * @throws AccessDeniedException Nếu ứng viên cố xóa CV không phải của mình
     */
    @Override
    @Transactional
    public void deleteCV(Long cvId, String username) {
        log.info("Deleting CV permanently - CVId: {}", cvId);
        
        // Get authenticated user and their candidate profile
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + username));

        // Get CV
        CV cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new ResourceNotFoundException("CV not found with ID: " + cvId));

        // Verify ownership
        if (!cv.getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            throw new AccessDeniedException("You can only delete your own CVs");
        }

        // Delete physical file from disk (if exists)
        if (cv.getCvFile() != null && !cv.getCvFile().isEmpty()) {
            try {
                fileStorageService.deleteFile(cv.getCvFile());
                log.info("Physical file deleted: {}", cv.getCvFile());
            } catch (Exception e) {
                log.warn("Failed to delete physical file: {} - Error: {}", cv.getCvFile(), e.getMessage());
                // Continue with status update even if file deletion fails
            }
        }

        // Set status to DELETED (soft delete in database for audit trail)
        cv.setCvStatus(CVStatus.DELETED);
        cvRepository.save(cv);
        
        log.info("CV marked as DELETED - CVCode: {}", cv.getCvCode());
    }
}
