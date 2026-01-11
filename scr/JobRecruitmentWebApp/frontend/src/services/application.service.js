/**
 * Application Service
 * 
 * Khởi tạo dịch vụ quản lý ứng tuyển việc làm
 * Xử lý các tương tác với API liên quan đến ứng tuyển
 * 
 * Các chức năng chính:
 * - Nộp đơn ứng tuyển (POST /api/v1/applications)
 * - Lấy các đơn ứng tuyển của ứng viên (GET /api/v1/applications/me)
 * - Lấy các đơn ứng tuyển cho một việc làm (GET /api/v1/applications/job/{jobId})
 */

import axiosClient from '../api/axiosClient';

const applicationService = {
  /**
   * Nộp đơn ứng tuyển cho một việc làm
   * Endpoint: POST /api/v1/applications
   * Request: { jobId: Long, cvId: Long }
   * Response: ApplicationResponse
   * 
   * Quy tắc nghiệp vụ:
   * - Việc làm phải ở trạng thái ACTIVE và trong thời gian đăng tuyển
   * - CV phải thuộc về ứng viên và ở trạng thái ACTIVE
   * - Không được nộp đơn trùng lặp
   * 
   * @param {Object} data - Dữ liệu ứng tuyển
   * @param {number} data.jobId - ID việc làm để ứng tuyển
   * @param {number} data.cvId - ID CV để sử dụng
   * @returns {Promise<Object>} Phản hồi ứng tuyển
   */
  async applyToJob(data) {
    try {
      const response = await axiosClient.post('/applications', data);
      
      // Xử lý ApiResponse wrapper
      if (response.data && response.data.data) {
        return response.data.data;
      }
      
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Lấy tất cả đơn ứng tuyển của ứng viên hiện tại
   * Endpoint: GET /api/v1/applications/me
   * Response: Page<ApplicationResponse>
   * 
   * @param {Object} params - Tham số truy vấn
   * @param {number} params.page - Số trang (bắt đầu từ 0)
   * @param {number} params.size - Kích thước trang
   * @param {string} params.status - Lọc theo trạng thái (PENDING, APPROVED, REJECTED)
   * @returns {Promise<Object>} Danh sách ứng tuyển phân trang
   */
  async getMyApplications(params = {}) {
    try {
      const response = await axiosClient.get('/applications/me', { params });
      
      // Xử lý ApiResponse wrapper
      if (response.data && response.data.data) {
        return response.data.data;
      }
      
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Kiểm tra xem ứng viên đã nộp đơn cho việc làm chưa
   * Endpoint: GET /api/v1/applications/me
   * Lọc cục bộ theo jobId
   * 
   * @param {number} jobId - ID việc làm để kiểm tra
   * @returns {Promise<boolean>} True nếu đã nộp đơn, ngược lại false
   */
  async hasApplied(jobId) {
    try {
      const jobIdNum = typeof jobId === 'string' ? parseInt(jobId, 10) : jobId;
      
      const applications = await this.getMyApplications({ size: 1000 });
      
      // Xử lý các cấu trúc phản hồi khác nhau
      let applicationsList = [];
      if (Array.isArray(applications)) {
        applicationsList = applications;
      } else if (applications.content && Array.isArray(applications.content)) {
        applicationsList = applications.content;
      }
      
      const applied = applicationsList.some(app => {
        const appJobId = app.jobId || app.job?.jobId;
        return appJobId === jobIdNum || appJobId === jobId;
      });
      
      return applied;
    } catch (error) {
      return false;
    }
  },

  /**
   * Lấy tất cả đơn ứng tuyển cho một việc làm cụ thể (giao diện nhà tuyển dụng)
   * Endpoint: GET /api/v1/applications/job/{jobId}
   * Response: Page<ApplicationResponse>
   * 
   * @param {number} jobId - ID việc làm
   * @param {Object} params - Tham số truy vấn
   * @param {number} params.page - Số trang (bắt đầu từ 0)
   * @param {number} params.size - Kích thước trang
   * @param {string} params.status - Lọc theo trạng thái
   * @returns {Promise<Object>} Danh sách ứng tuyển phân trang
   */
  async getApplicationsByJob(jobId, params = {}) {
    try {
      const response = await axiosClient.get(`/applications/job/${jobId}`, { params });
      
      // Xử lý ApiResponse wrapper
      if (response.data && response.data.data) {
        return response.data.data;
      }
      
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Cập nhật trạng thái đơn ứng tuyển (Hành động của nhà tuyển dụng)
   * Endpoint: PATCH /api/v1/applications/{id}/status
   * Tham số yêu cầu: status (PENDING, APPROVED, REJECTED)
   * 
   * @param {number} applicationId - ID đơn ứng tuyển
   * @param {string} status - Trạng thái mới (PENDING, APPROVED, REJECTED)
   * @returns {Promise<Object>} Đơn ứng tuyển đã được cập nhật
   */
  async updateApplicationStatus(applicationId, status) {
    try {
      const response = await axiosClient.patch(
        `/applications/${applicationId}/status`,
        null,
        { params: { status } }
      );
      
      // Xử lý ApiResponse wrapper
      if (response.data && response.data.data) {
        return response.data.data;
      }
      
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Rút lui/Hủy đơn ứng tuyển (Hành động của ứng viên)
   * Endpoint: DELETE /api/v1/applications/{id}
   * 
   * @param {number} applicationId - ID đơn ứng tuyển để rút lui
   * @returns {Promise<void>} Không có nội dung khi thành công
   */
  async withdrawApplication(applicationId) {
    try {
      const response = await axiosClient.delete(`/applications/${applicationId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Lấy tất cả đơn ứng tuyển cho các việc làm của nhà tuyển dụng (giao diện nhà tuyển dụng)
   * Endpoint: GET /api/v1/applications/company
   * Response: Page<ApplicationResponse>
   * 
   * @param {Object} params - Tham số truy vấn
   * @param {number} params.page - Số trang (bắt đầu từ 0)
   * @param {number} params.size - Kích thước trang
   * @param {number} params.jobId - Lọc theo ID việc làm cụ thể
   * @param {string} params.status - Lọc theo trạng thái (PENDING, APPROVED, REJECTED)
   * @returns {Promise<Object>} Danh sách ứng tuyển phân trang
   */
  async getCompanyApplications(params = {}) {
    try {
      const response = await axiosClient.get('/applications/company', { params });
      
      // Xử lý ApiResponse wrapper
      if (response.data && response.data.data) {
        return response.data.data;
      }
      
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};

export default applicationService;
