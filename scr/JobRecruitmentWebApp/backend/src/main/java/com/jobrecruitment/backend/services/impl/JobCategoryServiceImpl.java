package com.jobrecruitment.backend.services.impl;

import com.jobrecruitment.backend.dtos.request.JobCategoryRequest;
import com.jobrecruitment.backend.dtos.response.JobCategoryResponse;
import com.jobrecruitment.backend.entities.JobCategory;
import com.jobrecruitment.backend.exceptions.ResourceNotFoundException;
import com.jobrecruitment.backend.mappers.JobCategoryMapper;
import com.jobrecruitment.backend.repositories.JobCategoryRepository;
import com.jobrecruitment.backend.services.JobCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JobCategoryServiceImpl - Service triển khai quản lý danh mục công việc
 * 
 * Tham khảo: Section 4.4 - Admin Module - Category Management
 * 
 * Chức năng chính:
 * - CRUD danh mục: Tạo (Admin), Đọc (Công khai), Cập nhật (Admin), Xoá (Admin)
 * - Danh mục gồm: Tên (JCName), Mô tả (JCDescription), Mức lương cơ bản (JCBaseSalary)
 * - JobCategory là dữ liệu Master Data, cần quản lý cẩn thận
 * 
 * Quy tắc nghiệp vụ:
 * - RBGTN: Mức lương cơ bản (JCBaseSalary) phải > 0
 * - Quyền truy cập:
 *   + Đọc (GET): Công khai - Không cần xác thực
 *   + Tạo/Cập nhật/Xoá (POST/PUT/DELETE): Chỉ Admin (ROLE_ADM)
 * 
 * Phụ thuộc:
 * - JobCategoryRepository: Truy vấn và lưu JobCategory
 * - JobCategoryMapper: Chuyển đổi Entity <-> DTO
 */
@Service
@RequiredArgsConstructor
public class JobCategoryServiceImpl implements JobCategoryService {

    private final JobCategoryRepository jobCategoryRepository;
    private final JobCategoryMapper jobCategoryMapper;

    /**
     * Lấy danh sách tất cả danh mục công việc
     * 
     * Chức năng:
     * - Lấy toàn bộ danh mục từ database (không phân trang)
     * - Chuyển đổi Entity -> DTO
     * 
     * Quy trình:
     * 1. Gọi jobCategoryRepository.findAll() để lấy tất cả JobCategory
     * 2. Stream qua danh sách và map bằng jobCategoryMapper::toResponse
     * 3. Collect và trả về List<JobCategoryResponse>
     * 
     * Quyền truy cập: Công khai - Không cần xác thực
     * 
     * @return List<JobCategoryResponse> - Danh sách tất cả danh mục
     */
    @Override
    public List<JobCategoryResponse> getAllCategories() {
        return jobCategoryRepository.findAll().stream()
                .map(jobCategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin danh mục công việc theo ID
     * 
     * Chức năng:
     * - Tìm JobCategory theo JCID (Primary Key)
     * - Chuyển đổi Entity -> DTO
     * 
     * Quy trình:
     * 1. Gọi jobCategoryRepository.findById(jcId)
     * 2. Nếu không tồn tại: Ném ResourceNotFoundException
     * 3. Chuyển đổi Entity -> DTO bằng jobCategoryMapper::toResponse
     * 4. Trả về JobCategoryResponse
     * 
     * Quyền truy cập: Công khai - Không cần xác thực
     * 
     * @param jcId ID danh mục công việc (JCID)
     * @return JobCategoryResponse - Thông tin danh mục
     * @throws ResourceNotFoundException Nếu không tìm thấy danh mục
     */
    @Override
    public JobCategoryResponse getCategoryById(Integer jcId) {
        JobCategory category = jobCategoryRepository.findById(jcId)
                .orElseThrow(() -> new ResourceNotFoundException("Job category not found with ID: " + jcId));
        return jobCategoryMapper.toResponse(category);
    }

    /**
     * Tạo danh mục công việc mới
     * 
     * Chức năng:
     * - Tạo mới JobCategory từ JobCategoryRequest
     * - Validate RBGTN: JCBaseSalary > 0 (thực hiện tại DTO bằng @Positive)
     * 
     * Quy trình:
     * 1. Tạo JobCategory entity mới: new JobCategory()
     * 2. Set các giá trị: JCName, JCDescription, JCBaseSalary
     * 3. Lưu vào database: jobCategoryRepository.save(category)
     * 4. Chuyển đổi Entity -> DTO: jobCategoryMapper::toResponse
     * 5. Trả về JobCategoryResponse
     * 
     * Quyền truy cập: Chỉ Admin (ROLE_ADM)
     * 
     * @param request JobCategoryRequest - Dữ liệu danh mục mới
     * @return JobCategoryResponse - Danh mục vừa tạo
     */
    @Override
    @Transactional
    public JobCategoryResponse createCategory(JobCategoryRequest request) {
        // Entity manipulation pattern: Create -> Set values -> Save
        JobCategory category = new JobCategory();
        category.setJcName(request.getJcName());
        category.setJcDescription(request.getJcDescription());
        category.setJcBaseSalary(request.getJcBaseSalary());
        
        JobCategory saved = jobCategoryRepository.save(category);
        return jobCategoryMapper.toResponse(saved);
    }

    /**
     * Cập nhật thông tin danh mục công việc
     * 
     * Chức năng:
     * - Cập nhật toàn bộ (full update) thông tin danh mục
     * - Validate RBGTN: JCBaseSalary > 0
     * 
     * Quy trình:
     * 1. Tìm JobCategory theo jcId: jobCategoryRepository.findById(jcId)
     * 2. Nếu không tồn tại: Ném ResourceNotFoundException
     * 3. Cập nhật các giá trị: JCName, JCDescription, JCBaseSalary
     * 4. Lưu: jobCategoryRepository.save(category)
     * 5. Chuyển đổi Entity -> DTO: jobCategoryMapper::toResponse
     * 6. Trả về JobCategoryResponse
     * 
     * Quyền truy cập: Chỉ Admin (ROLE_ADM)
     * 
     * @param jcId ID danh mục cần cập nhật
     * @param request JobCategoryRequest - Dữ liệu cập nhật
     * @return JobCategoryResponse - Danh mục sau khi cập nhật
     * @throws ResourceNotFoundException Nếu không tìm thấy danh mục
     */
    @Override
    @Transactional
    public JobCategoryResponse updateCategory(Integer jcId, JobCategoryRequest request) {
        // Entity manipulation pattern: Find -> Validate -> Set -> Save
        JobCategory category = jobCategoryRepository.findById(jcId)
                .orElseThrow(() -> new ResourceNotFoundException("Job category not found with ID: " + jcId));
        
        category.setJcName(request.getJcName());
        category.setJcDescription(request.getJcDescription());
        category.setJcBaseSalary(request.getJcBaseSalary());
        
        JobCategory updated = jobCategoryRepository.save(category);
        return jobCategoryMapper.toResponse(updated);
    }

    /**
     * Xoá danh mục công việc
     * 
     * Chức năng:
     * - Xoá cứng (hard delete) danh mục khỏi database
     * - Lưu ý: Nên kiểm tra các Job đang sử dụng danh mục này trước khi xoá (có thể gây lỗi Foreign Key Constraint)
     * 
     * Quy trình:
     * 1. Tìm JobCategory theo jcId: jobCategoryRepository.findById(jcId)
     * 2. Nếu không tồn tại: Ném ResourceNotFoundException
     * 3. Xoá khỏi database: jobCategoryRepository.delete(category)
     * 
     * Quyền truy cập: Chỉ Admin (ROLE_ADM)
     * 
     * @param jcId ID danh mục cần xoá
     * @throws ResourceNotFoundException Nếu không tìm thấy danh mục
     */
    @Override
    @Transactional
    public void deleteCategory(Integer jcId) {
        // Entity manipulation pattern: Find -> Validate -> Delete
        JobCategory category = jobCategoryRepository.findById(jcId)
                .orElseThrow(() -> new ResourceNotFoundException("Job category not found with ID: " + jcId));
        
        jobCategoryRepository.delete(category);
    }
}
