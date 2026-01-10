package com.jobrecruitment.backend.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobrecruitment.backend.dtos.request.JobSeekPostRequest;
import com.jobrecruitment.backend.dtos.response.JobSeekPostResponse;
import com.jobrecruitment.backend.entities.Candidate;
import com.jobrecruitment.backend.entities.SeekingPost;
import com.jobrecruitment.backend.entities.User;
import com.jobrecruitment.backend.enums.SeekingPostStatus;
import com.jobrecruitment.backend.exceptions.ResourceNotFoundException;
import com.jobrecruitment.backend.exceptions.ValidationException;
import com.jobrecruitment.backend.mappers.SeekingPostMapper;
import com.jobrecruitment.backend.repositories.CandidateRepository;
import com.jobrecruitment.backend.repositories.SeekingPostRepository;
import com.jobrecruitment.backend.repositories.UserRepository;
import com.jobrecruitment.backend.services.SeekingPostService;
import com.jobrecruitment.backend.utils.CodeGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SeekingPostServiceImpl - Service triển khai logic nghiệp vụ cho Tin tìm việc (Version 1)
 * 
 * Chức năng chính:
 * - Ứng viên tạo tin đăng tìm việc (ROLE_UV)
 * - Ứng viên cập nhật tin đăng của mình (Owner only)
 * - Ứng viên thay đổi trạng thái tin đăng (Owner only)
 * - Public/Employer tìm kiếm tin đăng (với privacy logic)
 * - Admin xóa tin đăng vi phạm (ROLE_ADM)
 * 
 * Business Rules:
 * - Tin đăng mới tự động có status=ACTIVE (Section 4.2)
 * - Một ứng viên chỉ có 1 tin ACTIVE tại một thời điểm
 * - Nếu tạo tin mới khi đã có tin ACTIVE, tin cũ tự động chuyển sang HIDDEN
 * 
 * Privacy Logic (Section 4.2):
 * - Guest/Candidate (UV): Xem masked data (name="Nguyễn Văn ***", no contact)
 * - Employer (DN): Xem full data (name, phone, email)
 * 
 * Dependencies:
 * - SeekingPostRepository: Truy vấn database cho SeekingPost entity
 * - CandidateRepository: Xác định candidate từ user
 * - UserRepository: Xác định user từ username (JWT)
 * - CodeGenerator: Tạo SKPostCode unique
 * - SeekingPostMapper: Chuyển đổi Entity ↔ DTO (với privacy logic)
 * 
 * Transaction Management:
 * - @Transactional: Rollback nếu có lỗi
 * - @Transactional(readOnly = true): Query optimization cho read operations
 * 
 * @author JobRecruitment Development Team
 * @version 1.0
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeekingPostServiceImpl implements SeekingPostService {

    private final SeekingPostRepository seekingPostRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final CodeGenerator codeGenerator;
    private final SeekingPostMapper seekingPostMapper;

    /**
     * Ứng viên tạo tin đăng tìm việc mới
     * 
     * Business Rules:
     * - Tin đăng mới tự động có status=ACTIVE
     * - Nếu ứng viên đã có tin ACTIVE, tự động chuyển sang HIDDEN
     * - Chỉ ứng viên (ROLE_UV) mới có thể tạo tin
     * 
     * Quy trình xử lý:
     * 1. Tìm User entity theo username (từ JWT)
     * 2. Tìm Candidate entity liên kết với User
     * 3. Kiểm tra có tin ACTIVE không:
     *    - Nếu có: Chuyển tin cũ sang HIDDEN
     *    - Nếu không: Tiếp tục
     * 4. Tạo SKPostCode unique theo format "BV" + 8 chữ số
     * 5. Tạo SeekingPost entity với status=ACTIVE
     * 6. Lưu vào database
     * 7. Trả về response DTO (full data vì owner)
     * 
     * @param username Username của ứng viên (từ JWT)
     * @param request Dữ liệu tin đăng
     * @return JobSeekPostResponse
     * @throws ResourceNotFoundException nếu không tìm thấy User/Candidate
     */
    @Override
    @Transactional
    public JobSeekPostResponse createPost(String username, JobSeekPostRequest request) {
        log.info("Creating seeking post for candidate: {}", username);
        
        // Step 1: Tìm User
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        // Step 2: Tìm Candidate
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + username));
        
        // Step 3: Kiểm tra tin ACTIVE hiện có
        Optional<SeekingPost> existingActivePost = seekingPostRepository
            .findByCandidateCandidateIdAndSkPostStatus(candidate.getCandidateId(), SeekingPostStatus.ACTIVE);
        
        if (existingActivePost.isPresent()) {
            log.info("Found existing ACTIVE post for candidate: {}. Changing to HIDDEN.", username);
            SeekingPost oldPost = existingActivePost.get();
            oldPost.setSkPostStatus(SeekingPostStatus.HIDDEN);
            seekingPostRepository.save(oldPost);
        }
        
        // Step 4: Khởi tạo mã SKPostCode duy nhất
        String skPostCode = codeGenerator.generateCode(
            CodeGenerator.PREFIX_SEEKING_POST,
            code -> seekingPostRepository.findBySkPostCode(code).isPresent()
        );
        
        // Step 5: Tạo mới SeekingPost entity
        SeekingPost newPost = new SeekingPost();
        newPost.setCandidate(candidate);
        newPost.setSkPostCode(skPostCode);
        newPost.setSkPostTitle(request.getTitle());
        newPost.setDesiredSalary(request.getDesiredSalary());
        newPost.setDesiredLocation(request.getLocation());
        // Chuyển skills List<String> thành chuỗi phân cách bằng dấu phẩy
        newPost.setSkPostSkills(request.getSkills() != null ? String.join(",", request.getSkills()) : "");
        newPost.setSkPostIntro(request.getIntroduction());
        newPost.setSkPostStatus(SeekingPostStatus.ACTIVE); // Giá trị mặc định: ACTIVE
        
        // Step 6: Lưu vào cơ sở dữ liệu
        SeekingPost savedPost = seekingPostRepository.save(newPost);
        
        log.info("Created seeking post successfully. Code: {}", skPostCode);
        
        // Step 7: Trả về response đầy đủ (owner có thể xem tất cả)
        return seekingPostMapper.toFullResponse(savedPost);
    }

    /**
     * Ứng viên cập nhật tin đăng của mình
     * 
     * Business Rules:
     * - Chỉ owner mới có thể cập nhật
     * - Không thay đổi status (dùng changeStatus)
     * 
     * Quy trình xử lý:
     * 1. Tìm SeekingPost theo ID
     * 2. Verify ownership: Username phải khớp với Candidate của tin đăng
     * 3. Cập nhật các field từ request
     * 4. Lưu vào database
     * 5. Trả về response DTO (full data)
     * 
     * @param skPostId ID tin đăng
     * @param username Username của ứng viên (từ JWT)
     * @param request Dữ liệu cập nhật
     * @return JobSeekPostResponse
     * @throws ResourceNotFoundException nếu không tìm thấy tin đăng
     * @throws AccessDeniedException nếu không phải owner
     */
    @Override
    @Transactional
    public JobSeekPostResponse updatePost(Long skPostId, String username, JobSeekPostRequest request) {
        log.info("Updating seeking post ID: {} by user: {}", skPostId, username);
        
        // Step 1: Tìm SeekingPost
        SeekingPost post = seekingPostRepository.findById(skPostId)
            .orElseThrow(() -> new ResourceNotFoundException("Seeking post not found: " + skPostId));
        
        // Step 2: Xác minh quyền sở hữu
        String ownerUsername = post.getCandidate().getUser().getUsername();
        if (!ownerUsername.equals(username)) {
            log.warn("Access denied. User {} tried to update post owned by {}", username, ownerUsername);
            throw new AccessDeniedException("You do not have permission to edit this seeking post");
        }
        
        // Step 3: Cập nhật các trường (không cập nhật trạng thái)
        post.setSkPostTitle(request.getTitle());
        post.setDesiredSalary(request.getDesiredSalary());
        post.setDesiredLocation(request.getLocation());
        post.setSkPostSkills(request.getSkills() != null ? String.join(",", request.getSkills()) : "");
        post.setSkPostIntro(request.getIntroduction());
        
        // Step 4: Lưu vào cơ sở dữ liệu
        SeekingPost updatedPost = seekingPostRepository.save(post);
        
        log.info("Updated seeking post successfully. Code: {}", post.getSkPostCode());
        
        // Step 5: Trả về response đầy đủ
        return seekingPostMapper.toFullResponse(updatedPost);
    }

    /**
     * Ứng viên thay đổi trạng thái tin đăng
     * 
     * Business Rules:
     * - Chỉ owner mới có thể thay đổi
     * - Trạng thái hợp lệ: ACTIVE, HIDDEN, CLOSED
     * 
     * Quy trình xử lý:
     * 1. Tìm SeekingPost theo ID
     * 2. Verify ownership
     * 3. Kiểm tra nếu chuyển sang ACTIVE:
     *    - Nếu đã có tin ACTIVE khác, chuyển tin đó sang HIDDEN
     * 4. Cập nhật status
     * 5. Lưu vào database
     * 6. Trả về response DTO
     * 
     * @param skPostId ID tin đăng
     * @param username Username của ứng viên (từ JWT)
     * @param newStatus Trạng thái mới
     * @return JobSeekPostResponse
     */
    @Override
    @Transactional
    public JobSeekPostResponse changeStatus(Long skPostId, String username, SeekingPostStatus newStatus) {
        log.info("Changing status of seeking post ID: {} to {} by user: {}", skPostId, newStatus, username);
        
        // Step 1: Tìm SeekingPost
        SeekingPost post = seekingPostRepository.findById(skPostId)
            .orElseThrow(() -> new ResourceNotFoundException("Seeking post not found: " + skPostId));
        
        // Step 2: Xác minh quyền sở hữu
        String ownerUsername = post.getCandidate().getUser().getUsername();
        if (!ownerUsername.equals(username)) {
            log.warn("Access denied. User {} tried to change status of post owned by {}", username, ownerUsername);
            throw new AccessDeniedException("You do not have permission to change the status of this seeking post");
        }
        
        // Step 3: Kiểm tra nếu chuyển sang ACTIVE
        if (newStatus == SeekingPostStatus.ACTIVE) {
            Optional<SeekingPost> existingActivePost = seekingPostRepository
                .findByCandidateCandidateIdAndSkPostStatus(
                    post.getCandidate().getCandidateId(), 
                    SeekingPostStatus.ACTIVE
                );
            
            if (existingActivePost.isPresent() && !existingActivePost.get().getSkPostId().equals(skPostId)) {
                log.info("Found another ACTIVE post. Changing it to HIDDEN.");
                SeekingPost otherPost = existingActivePost.get();
                otherPost.setSkPostStatus(SeekingPostStatus.HIDDEN);
                seekingPostRepository.save(otherPost);
            }
        }
        
        // Step 4: Cập nhật trạng thái
        post.setSkPostStatus(newStatus);
        
        // Step 5: Lưu vào cơ sở dữ liệu
        SeekingPost updatedPost = seekingPostRepository.save(post);
        
        log.info("Changed status successfully. Code: {}", post.getSkPostCode());
        
        // Step 6: Trả về response đầy đủ
        return seekingPostMapper.toFullResponse(updatedPost);
    }

    /**
     * Tìm kiếm tin đăng công khai (với filter)
     * 
     * Privacy Logic:
     * - Nếu username = null (Guest): Trả về masked data
     * - Nếu role = DN (Employer): Trả về full data
     * - Nếu role = UV (Candidate): Trả về masked data
     * 
     * Filter:
     * - location: Lọc theo địa điểm (LIKE)
     * - skills: Lọc theo kỹ năng (LIKE)
     * 
     * Quy trình xử lý:
     * 1. Xác định role của requester (nếu có username)
     * 2. Tìm kiếm tin đăng ACTIVE với filter
     * 3. Map sang response DTO:
     *    - Nếu role = DN: Full data
     *    - Khác: Masked data
     * 4. Trả về Page<JobSeekPostResponse>
     * 
     * @param username Username của người yêu cầu (null = Guest)
     * @param location Địa điểm (nullable)
     * @param skills Kỹ năng (nullable)
     * @param pageable Phân trang
     * @return Page<JobSeekPostResponse>
     */
    @Override
    @Transactional(readOnly = true)
    public Page<JobSeekPostResponse> searchPosts(String username, String location, String skills, Pageable pageable) {
        log.info("Searching seeking posts. User: {}, Location: {}, Skills: {}", username, location, skills);
        
        // Step 1: Xác định vai trò
        boolean isEmployer = false;
        if (username != null) {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                String roleCode = user.get().getRole().getRoleCode();
                isEmployer = "DN".equals(roleCode);
            }
        }
        
        // Step 2: Tìm kiếm tin đăng ACTIVE với filter
        Page<SeekingPost> posts = seekingPostRepository.searchActivePosts(location, skills, pageable);
        
        // Step 3: Map sang response DTO với privacy
        final boolean showFullData = isEmployer;
        return posts.map(post -> 
            showFullData 
                ? seekingPostMapper.toFullResponse(post)
                : seekingPostMapper.toMaskedResponse(post)
        );
    }

    /**
     * Xem chi tiết một tin đăng
     * 
     * Privacy Logic: Tương tự searchPosts
     * 
     * @param skPostId ID tin đăng
     * @param username Username của người yêu cầu (null = Guest)
     * @return JobSeekPostResponse
     * @throws ResourceNotFoundException nếu không tìm thấy tin đăng
     */
    @Override
    @Transactional(readOnly = true)
    public JobSeekPostResponse getPostById(Long skPostId, String username) {
        log.info("Getting seeking post detail. ID: {}, User: {}", skPostId, username);
        
        // Tìm SeekingPost
        SeekingPost post = seekingPostRepository.findById(skPostId)
            .orElseThrow(() -> new ResourceNotFoundException("Seeking post not found: " + skPostId));
        
        // Kiểm tra nếu tin đăng là ACTIVE (công khai chỉ xem được tin ACTIVE)
        if (post.getSkPostStatus() != SeekingPostStatus.ACTIVE) {
            // Kiểm tra quyền sở hữu: Chỉ chủ sở hữu mới xem được tin HIDDEN/CLOSED
            if (username == null || !post.getCandidate().getUser().getUsername().equals(username)) {
                throw new ResourceNotFoundException("Seeking post not found: " + skPostId);
            }
        }
        
        // Xác định vai trò
        boolean isEmployer = false;
        boolean isOwner = false;
        if (username != null) {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                String roleCode = user.get().getRole().getRoleCode();
                isEmployer = "DN".equals(roleCode);
                isOwner = post.getCandidate().getUser().getUsername().equals(username);
            }
        }
        
        // Map sang response DTO với privacy
        if (isEmployer || isOwner) {
            return seekingPostMapper.toFullResponse(post);
        } else {
            return seekingPostMapper.toMaskedResponse(post);
        }
    }

    /**
     * Admin xóa tin đăng vi phạm
     * 
     * Business Rules:
     * - Chỉ Admin (ROLE_ADM) mới có quyền
     * - Xóa vĩnh viễn (hard delete)
     * 
     * @param skPostId ID tin đăng
     * @throws ResourceNotFoundException nếu không tìm thấy tin đăng
     */
    @Override
    @Transactional
    public void deletePost(Long skPostId) {
        log.info("Deleting seeking post ID: {}", skPostId);
        
        // Kiểm tra tồn tại
        if (!seekingPostRepository.existsById(skPostId)) {
            throw new ResourceNotFoundException("Seeking post not found: " + skPostId);
        }
        
        // Xóa vĩnh viễn
        seekingPostRepository.deleteById(skPostId);
        
        log.info("Deleted seeking post successfully. ID: {}", skPostId);
    }

    /**
     * Ứng viên xóa tin đăng của chính mình
     * 
     * @param skPostId ID tin đăng cần xóa
     * @param username Username của ứng viên (từ JWT)
     * @throws ResourceNotFoundException nếu không tìm thấy tin đăng
     * @throws ForbiddenException nếu user không phải owner
     */
    @Override
    @Transactional
    public void deleteOwnPost(Long skPostId, String username) {
        log.info("Candidate {} attempting to delete own seeking post ID: {}", username, skPostId);
        
        // 1. Tìm User và Candidate
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + username));
        
        // 2. Tìm seeking post
        SeekingPost post = seekingPostRepository.findById(skPostId)
            .orElseThrow(() -> new ResourceNotFoundException("Seeking post not found: " + skPostId));
        
        // 3. Kiểm tra quyền sở hữu
        if (!post.getCandidate().getCandidateId().equals(candidate.getCandidateId())) {
            log.warn("User {} tried to delete post {} owned by another candidate", username, skPostId);
            throw new ValidationException("You can only delete your own seeking posts");
        }
        
        // 4. Xóa
        seekingPostRepository.deleteById(skPostId);
        
        log.info("Candidate {} deleted own post successfully. ID: {}", username, skPostId);
    }

    /**
     * Ứng viên xem danh sách tin đăng của mình
     * 
     * Quy trình xử lý:
     * 1. Tìm User theo username
     * 2. Tìm Candidate liên kết
     * 3. Lấy tất cả tin đăng của candidate (bao gồm HIDDEN, CLOSED)
     * 4. Trả về full data (owner)
     * 
     * @param username Username của ứng viên (từ JWT)
     * @return Page<JobSeekPostResponse>
     * @throws ResourceNotFoundException nếu không tìm thấy User/Candidate
     */
    @Override
    @Transactional(readOnly = true)
    public Page<JobSeekPostResponse> getMyPosts(String username, Pageable pageable) {
        log.info("Getting my posts for candidate: {}", username);
        
        // Tìm User
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));
        
        // Tìm Candidate
        Candidate candidate = candidateRepository.findByUserUserId(user.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ ứng viên cho người dùng: " + username));
        
        // Lấy tất cả tin đăng của candidate
        List<SeekingPost> allPosts = seekingPostRepository.findByCandidateCandidateId(candidate.getCandidateId());
        
        // Áp dụng phân trang thủ công
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allPosts.size());
        List<SeekingPost> pagedPosts = allPosts.subList(start, end);
        
        // Chuyển đổi thành Page
        Page<SeekingPost> posts = new org.springframework.data.domain.PageImpl<>(
            pagedPosts,
            pageable,
            allPosts.size()
        );
        
        // Map thành full response (owner)
        return posts.map(seekingPostMapper::toFullResponse);
    }
}
