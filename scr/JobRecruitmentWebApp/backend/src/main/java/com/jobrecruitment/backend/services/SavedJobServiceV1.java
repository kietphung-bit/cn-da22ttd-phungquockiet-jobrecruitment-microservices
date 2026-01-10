package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.dtos.request.SaveJobRequest;
import com.jobrecruitment.backend.dtos.response.SavedJobResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * SavedJobServiceV1 - Service interface cho chức năng lưu Job (Bookmark)
 * 
 * Chức năng:
 * - Candidate: Lưu Job, xem danh sách Job đã lưu, bỏ lưu Job
 * 
 * Quy tắc nghiệp vụ:
 * - Mỗi Candidate có thể lưu nhiều Job
 * - 1 Job chỉ lưu 1 lần (unique constraint: candidateId + jobId)
 * - Bỏ lưu = hard delete khỏi SavedJob table
 * 
 * Tính năng:
 * - Phân trang: Spring Data JPA Pageable
 * - SavedJobResponse bao gồm toàn bộ JobResponse (để hiển thị thông tin Job)
 * 
 * @see SavedJobServiceV1Impl
 */
public interface SavedJobServiceV1 {
    
    /**
     * Lưu Job (Candidate only, bookmark)
     * 
     * Sử dụng:
     * - API POST /api/v1/saved-jobs
     * - Candidate bookmark Job để xem sau
     * 
     * Quy tắc nghiệp vụ:
     * - Kiểm tra Job có tồn tại không
     * - Kiểm tra Job đã lưu chưa (existsByCandidateIdAndJobId)
     * - Tạo SavedJob mới
     * 
     * @param request SaveJobRequest (jobId)
     * @param username Username của Candidate đang authenticate
     * @return SavedJobResponse (bao gồm savedTime, JobResponse)
     * @throws ValidationException nếu Job đã lưu
     * @throws ResourceNotFoundException nếu Job không tìm thấy
     */
    SavedJobResponse saveJob(SaveJobRequest request, String username);
    
    /**
     * Lấy danh sách Job đã lưu (Chỉ ứng viên, phân trang)
     * 
     * Sử dụng:
     * - API GET /api/v1/saved-jobs
     * - Candidate xem danh sách Job đã bookmark
     * 
     * @param username Username của Candidate đang authenticate
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page<SavedJobResponse> (bao gồm toàn bộ JobResponse)
     */
    Page<SavedJobResponse> getMySavedJobs(String username, Pageable pageable);
    
    /**
     * Bỏ lưu Job (Candidate only, remove bookmark)
     * 
     * Sử dụng:
     * - API DELETE /api/v1/saved-jobs/{jobId}
     * - Candidate unsave Job
     * 
     * Quy tắc nghiệp vụ:
     * - Kiểm tra SavedJob có tồn tại không
     * - Xóa SavedJob khỏi database (hard delete)
     * 
     * @param jobId ID công việc cần bỏ lưu
     * @param username Tên đăng nhập ứng viên đã xác thực
     * @throws ResourceNotFoundException nếu SavedJob không tìm thấy
     */
    void unsaveJob(Long jobId, String username);
}
