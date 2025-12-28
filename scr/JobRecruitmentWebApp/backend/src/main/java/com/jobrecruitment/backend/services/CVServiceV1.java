package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.dtos.response.CVResponse;
import com.jobrecruitment.backend.enums.CVStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * CVServiceV1 - Service interface cho quản lý CV
 * 
 * Chức năng:
 * - Candidate: Upload CV, xem danh sách CV, ẩn/hiện CV, xóa CV
 * 
 * Business Rules:
 * - Mỗi Candidate có thể có nhiều CV (One-to-Many)
 * - CV có 2 trạng thái: ACTIVE (hiện thị), HIDDEN (ẩn)
 * - Xóa CV = soft delete (set CVStatus = HIDDEN)
 * 
 * Features:
 * - Code generation: CVCode = CV + 8 số
 * - File storage: cvFile = path/URL đến file CV trên server
 * 
 * @see CVServiceV1Impl
 */
public interface CVServiceV1 {
    
    /**
     * Tạo CV mới (Candidate only, upload CV)
     * 
     * Sử dụng:
     * - API POST /api/v1/cvs (multipart/form-data)
     * - Upload file CV trực tiếp qua MultipartFile
     * 
     * Business Logic:
     * - Lưu file vào uploads/cvs/ bằng FileStorageService
     * - Tạo CV mới với CVCode = CV + 8 số (unique)
     * - CVStatus = ACTIVE (mặc định)
     * 
     * @param file MultipartFile từ form upload (PDF/DOCX, max 10MB)
     * @param username Username của Candidate đang authenticate
     * @return CVResponse (bao gồm cvCode, cvId, cvFile, cvStatus)
     */
    CVResponse createCV(MultipartFile file, String username);
    
    /**
     * Lấy danh sách CV của Candidate (Candidate only)
     * 
     * Sử dụng:
     * - API GET /api/v1/cvs
     * - Candidate xem tất cả CV của mình (để chọn CV khi ứng tuyển)
     * 
     * @param username Username của Candidate đang authenticate
     * @return List<CVResponse> (tất cả CV của Candidate, bao gồm ACTIVE và HIDDEN)
     */
    List<CVResponse> getMyCVs(String username);
    
    /**
     * Cập nhật trạng thái CV (Candidate only - ẩn/hiện CV)
     * 
     * Sử dụng:
     * - API PATCH /api/v1/cvs/{cvId}/status
     * - Candidate ẩn CV cũ (ACTIVE -> HIDDEN) hoặc hiện lại CV (HIDDEN -> ACTIVE)
     * 
     * Business Logic:
     * - Kiểm tra CV có thuộc Candidate không
     * - Cập nhật CVStatus
     * 
     * @param cvId CV ID
     * @param newStatus Trạng thái mới (ACTIVE/HIDDEN)
     * @param username Username của Candidate đang authenticate
     * @return CVResponse (đã cập nhật status)
     * @throws ResourceNotFoundException nếu CV không tìm thấy
     * @throws ValidationException nếu Candidate không sở hữu CV
     */
    CVResponse updateCVStatus(Long cvId, CVStatus newStatus, String username);
    
    /**
     * Xóa CV (Soft delete - set CVStatus = HIDDEN)
     * 
     * Sử dụng:
     * - API DELETE /api/v1/cvs/{cvId}
     * 
     * Business Logic:
     * - Kiểm tra CV có thuộc Candidate không
     * - Soft delete: Set CVStatus = HIDDEN (không xóa khỏi database)
     * 
     * @param cvId CV ID
     * @param username Username của Candidate đang authenticate
     * @throws ResourceNotFoundException nếu CV không tìm thấy
     * @throws ValidationException nếu Candidate không sở hữu CV
     */
    void deleteCV(Long cvId, String username);
}
