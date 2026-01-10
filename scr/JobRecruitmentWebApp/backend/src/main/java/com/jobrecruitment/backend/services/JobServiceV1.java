package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.dtos.request.JobRequest;
import com.jobrecruitment.backend.dtos.response.JobResponse;
import com.jobrecruitment.backend.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * JobServiceV1 - Service interface cho quản lý tin tuyển dụng
 * 
 * Chức năng:
 * - Employer: Đăng tin, chỉnh sửa, xóa, đóng/mở tin tuyển dụng
 * - Candidate/Public: Xem, tìm kiếm tin tuyển dụng (phân trang + filter)
 * - Admin: Quản lý tất cả tin tuyển dụng
 * 
 * Quy tắc nghiệp vụ:
 * - Employer chỉ quản lý được Job của mình
 * - Job có 3 trạng thái: OPEN (mở), CLOSED (đóng), HIDDEN (ẩn)
 * - Xóa Job = soft delete (set JobStatus = HIDDEN)
 * 
 * Tính năng:
 * - Phân trang: Spring Data JPA Pageable
 * - Lọc động: JPA Specifications (title, location, category, salary range, status...)
 * - Tạo mã: JobCode = JOB + 8 số
 * 
 * @see JobServiceV1Impl
 */
public interface JobServiceV1 {
    
    /**
     * Lấy tất cả công việc với phân trang và lọc động
     * 
     * @param pageable Tham số phân trang (trang, kích thước, sắp xếp)
     * @param jobTitle Lọc theo tiêu đề công việc (tìm kiếm gần đúng)
     * @param jobStatus Lọc theo trạng thái công việc
     * @param jobLocation Lọc theo địa điểm (tìm kiếm gần đúng)
     * @param companyId Lọc theo ID công ty
     * @param jcId Lọc theo ID danh mục
     * @param minSalary Lọc theo mức lương tối thiểu
     * @param maxSalary Lọc theo mức lương tối đa
     * @return Trang các đối tượng JobResponse
     */
    Page<JobResponse> getAllJobs(
            Pageable pageable,
            String jobTitle,
            JobStatus jobStatus,
            String jobLocation,
            Long companyId,
            Integer jcId,
            Double minSalary,
            Double maxSalary
    );
    
    /**
     * Lấy công việc theo ID
     * 
     * @param jobId ID công việc
     * @return DTO JobResponse
     */
    JobResponse getJobById(Long jobId);
    
    /**
     * Tạo tin tuyển dụng mới (Chỉ nhà tuyển dụng)
     * 
     * @param request Yêu cầu tạo công việc
     * @param username Tên đăng nhập nhà tuyển dụng đã xác thực
     * @return DTO JobResponse đã tạo
     */
    JobResponse createJob(JobRequest request, String username);
    
    /**
     * Cập nhật tin tuyển dụng (Chỉ nhà tuyển dụng - tin của mình)
     * 
     * @param jobId ID công việc
     * @param request Yêu cầu cập nhật công việc
     * @param username Tên đăng nhập nhà tuyển dụng đã xác thực
     * @return DTO JobResponse đã cập nhật
     */
    JobResponse updateJob(Long jobId, JobRequest request, String username);
    
    /**
     * Cập nhật trạng thái công việc (Chỉ nhà tuyển dụng - tin của mình)
     * 
     * @param jobId ID công việc
     * @param newStatus Trạng thái công việc mới
     * @param username Tên đăng nhập nhà tuyển dụng đã xác thực
     * @return DTO JobResponse đã cập nhật
     */
    JobResponse updateJobStatus(Long jobId, JobStatus newStatus, String username);
    
    /**
     * Xóa công việc (Soft delete - chuyển trạng thái thành HIDDEN)
     * 
     * @param jobId ID công việc
     * @param username Tên đăng nhập nhà tuyển dụng đã xác thực
     */
    void deleteJob(Long jobId, String username);
    
    /**
     * Lấy các công việc do nhà tuyển dụng đã xác thực đăng với phân trang
     * 
     * @param pageable Tham số phân trang
     * @param username Tên đăng nhập nhà tuyển dụng đã xác thực
     * @return Trang các DTO JobResponse
     */
    Page<JobResponse> getMyJobs(Pageable pageable, String username);
}
