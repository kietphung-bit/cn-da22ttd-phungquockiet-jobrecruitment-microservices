package com.jobrecruitment.backend.services.impl;

import com.jobrecruitment.backend.dtos.request.SaveJobRequest;
import com.jobrecruitment.backend.dtos.response.SavedJobResponse;
import com.jobrecruitment.backend.entities.Candidate;
import com.jobrecruitment.backend.entities.Job;
import com.jobrecruitment.backend.entities.SavedJob;
import com.jobrecruitment.backend.entities.User;
import com.jobrecruitment.backend.exceptions.ResourceNotFoundException;
import com.jobrecruitment.backend.exceptions.ValidationException;
import com.jobrecruitment.backend.mappers.SavedJobMapper;
import com.jobrecruitment.backend.repositories.CandidateRepository;
import com.jobrecruitment.backend.repositories.JobRepository;
import com.jobrecruitment.backend.repositories.SavedJobRepository;
import com.jobrecruitment.backend.repositories.UserRepository;
import com.jobrecruitment.backend.services.SavedJobServiceV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SavedJobServiceV1Impl - Service triển khai logic nghiệp vụ cho Quản lý Job Đã lưu (Version 1).
 * 
 * Chức năng chính:
 * - Ứng viên lưu/bookmark job để xem sau (Candidate only)
 * - Ứng viên xem danh sách job đã lưu với phân trang (Candidate only)
 * - Ứng viên bỏ lưu/unsave job (Candidate only)
 * 
 * Business Rules (RBSL):
 * - RBSL: Ngăn chặn duplicate saves (kiểm tra job đã lưu chưa)
 * - Ownership: Ứng viên chỉ quản lý saved jobs của chính mình
 * - Pagination: Hỗ trợ phân trang cho danh sách job đã lưu
 * 
 * Dependencies:
 * - SavedJobRepository: Truy vấn database cho SavedJob entity
 * - JobRepository: Kiểm tra sự tồn tại của Job
 * - CandidateRepository: Xác định candidate từ user
 * - UserRepository: Xác định user từ username (JWT)
 * - SavedJobMapper: Chuyển đổi Entity ↔ DTO
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
public class SavedJobServiceV1Impl implements SavedJobServiceV1 {

    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final SavedJobMapper savedJobMapper;

    /**
     * Lưu/bookmark job để xem sau.
     * 
     * Candidate-only endpoint - chỉ ứng viên mới có thể lưu job.
     * 
     * Quy trình xử lý:
     * 1. Tìm User entity theo username (từ JWT)
     * 2. Tìm Candidate entity liên kết với User
     * 3. Tìm Job entity theo jobId
     * 4. Kiểm tra duplicate: Job đã được lưu bởi ứng viên này chưa?
     *    - Nếu đã lưu: Throw ValidationException ("Job is already saved")
     * 5. Tạo SavedJob entity:
     *    - Candidate: Liên kết với ứng viên
     *    - Job: Liên kết với job cần lưu
     *    - SavedTime: Thời điểm lưu (LocalDateTime.now())
     * 6. Lưu SavedJob entity vào database
     * 7. Chuyển đổi sang DTO và trả về
     * 
     * Business Rule (RBSL): Ngăn chặn duplicate saves
     * 
     * @param request SaveJobRequest chứa jobId cần lưu
     * @param username Username của ứng viên đang đăng nhập
     * @return SavedJobResponse chứa thông tin job vừa lưu
     * @throws ResourceNotFoundException Nếu không tìm thấy User, Candidate hoặc Job
     * @throws ValidationException Nếu job đã được lưu trước đó (duplicate)
     */
    @Override
    @Transactional
    public SavedJobResponse saveJob(SaveJobRequest request, String username) {
        log.info("Saving job for user: {}, JobId: {}", username, request.getJobId());
        
        // Get authenticated user and their candidate profile
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + username));

        // Get job
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + request.getJobId()));

        // Check if already saved
        if (savedJobRepository.existsByCandidateIdAndJobId(candidate.getCandidateId(), request.getJobId())) {
            throw new ValidationException("Job is already saved");
        }

        // Create saved job
        SavedJob savedJob = new SavedJob();
        savedJob.setCandidate(candidate);
        savedJob.setJob(job);
        savedJob.setSavedTime(LocalDateTime.now());

        SavedJob saved = savedJobRepository.save(savedJob);
        log.info("Job saved successfully - JobCode: {}", job.getJobCode());
        
        return savedJobMapper.toResponse(saved);
    }

    /**
     * Lấy danh sách các job đã lưu của ứng viên với phân trang.
     * 
     * Candidate-only endpoint - chỉ ứng viên mới có thể xem danh sách job đã lưu của chính mình.
     * 
     * Quy trình xử lý:
     * 1. Tìm User entity theo username (từ JWT)
     * 2. Tìm Candidate entity liên kết với User
     * 3. Lấy tất cả SavedJob entity của candidate
     * 4. Chuyển đổi List<SavedJob> sang List<SavedJobResponse>
     * 5. Thực hiện manual pagination:
     *    - Tính start index và end index dựa vào Pageable
     *    - Tạo sublist cho trang hiện tại
     *    - Tạo PageImpl với content, pageable và total elements
     * 6. Trả về Page<SavedJobResponse>
     * 
     * Lưu ý: Sử dụng manual pagination vì repository không có phương thức Page
     * 
     * @param username Username của ứng viên đang đăng nhập
     * @param pageable Đối tượng phân trang (page, size, sort)
     * @return Page<SavedJobResponse> chứa danh sách job đã lưu và thông tin phân trang
     * @throws ResourceNotFoundException Nếu không tìm thấy User hoặc Candidate profile
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SavedJobResponse> getMySavedJobs(String username, Pageable pageable) {
        log.info("Fetching saved jobs for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + username));
        
        // Get all saved jobs (we'll manually paginate since repository doesn't have Page method)
        List<SavedJob> allSavedJobs = savedJobRepository.findByCandidateCandidateId(candidate.getCandidateId());
        
        // Convert to responses
        List<SavedJobResponse> allResponses = allSavedJobs.stream()
                .map(savedJobMapper::toResponse)
                .collect(Collectors.toList());
        
        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allResponses.size());
        
        List<SavedJobResponse> pageContent = allResponses.subList(
            Math.min(start, allResponses.size()), 
            end
        );
        
        log.info("Found {} saved jobs for candidate: {}", allSavedJobs.size(), candidate.getCandidateCode());
        
        return new PageImpl<>(pageContent, pageable, allResponses.size());
    }

    /**
     * Bỏ lưu/unsave job.
     * 
     * Candidate-only endpoint - chỉ ứng viên mới có thể bỏ lưu job của chính mình.
     * 
     * Quy trình xử lý:
     * 1. Tìm User entity theo username (từ JWT)
     * 2. Tìm Candidate entity liên kết với User
     * 3. Kiểm tra job có được lưu bởi candidate này không
     *    - Nếu không: Throw ResourceNotFoundException ("Saved job not found")
     * 4. Xóa SavedJob entity khỏi database (hard delete)
     * 5. Ghi log thành công
     * 
     * Lưu ý: Đây là hard delete - xóa hoàn toàn khỏi database
     * 
     * @param jobId ID của job cần bỏ lưu
     * @param username Username của ứng viên đang đăng nhập
     * @throws ResourceNotFoundException Nếu không tìm thấy User, Candidate hoặc SavedJob
     */
    @Override
    @Transactional
    public void unsaveJob(Long jobId, String username) {
        log.info("Unsaving job - JobId: {}", jobId);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + username));

        // Verify job exists and is saved
        if (!savedJobRepository.existsByCandidateIdAndJobId(candidate.getCandidateId(), jobId)) {
            throw new ResourceNotFoundException("Saved job not found");
        }

        // Delete saved job
        savedJobRepository.deleteByCandidateCandidateIdAndJobJobId(candidate.getCandidateId(), jobId);
        log.info("Job unsaved successfully - JobId: {}", jobId);
    }
}
