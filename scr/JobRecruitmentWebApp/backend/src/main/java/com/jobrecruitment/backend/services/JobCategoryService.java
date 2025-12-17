package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.dtos.request.JobCategoryRequest;
import com.jobrecruitment.backend.dtos.response.JobCategoryResponse;

import java.util.List;

/**
 * Job Category Service Interface
 * Section 4.4 - Admin Module - Quản lý danh mục công việc
 */
public interface JobCategoryService {
    
    /**
     * Lấy tất cả danh mục công việc (Truy cập công khai)
     * @return Danh sách JobCategoryResponse
     */
    List<JobCategoryResponse> getAllCategories();
    
    /**
     * Lấy một danh mục cụ thể theo ID
     * @param jcId - ID danh mục
     * @return JobCategoryResponse
     */
    JobCategoryResponse getCategoryById(Integer jcId);
    
    /**
     * Tạo mới một danh mục công việc (Chỉ dành cho Admin)
     * @param request - dữ liệu danh mục mới
     * @return JobCategoryResponse
     */
    JobCategoryResponse createCategory(JobCategoryRequest request);
    
    /**
     * Cập nhật một danh mục công việc (Chỉ dành cho Admin)
     * @param jcId - ID danh mục
     * @param request - dữ liệu cập nhật
     * @return JobCategoryResponse
     */
    JobCategoryResponse updateCategory(Integer jcId, JobCategoryRequest request);
    
    /**
     * Xóa một danh mục (Chỉ dành cho Admin)
     * @param jcId - ID danh mục
     */
    void deleteCategory(Integer jcId);
}
