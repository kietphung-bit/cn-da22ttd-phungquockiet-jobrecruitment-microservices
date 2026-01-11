import axiosClient from '../api/axiosClient';

/**
 * Admin Service
 * Gọi API cho các thao tác quản trị (quản lý người dùng, kiểm duyệt việc làm)
 * 
 * Base URL: /api/v1/admin
 * 
 * Endpoints:
 * - GET /stats - Thống kê dashboard
 * - GET /users - Danh sách người dùng với phân trang và bộ lọc
 * - PATCH /users/{id}/lock - Khóa tài khoản người dùng
 * - PATCH /users/{id}/unlock - Mở khóa tài khoản người dùng
 * - GET /jobs - Danh sách việc làm với bộ lọc trạng thái
 * - PATCH /jobs/{id}/status - Change job status (approve/reject)
 * 
 * Tất cả các endpoints yêu cầu quyền ROLE_ADM
 */
const adminService = {
  /**
   * Lấy thống kê dashboard
   * @returns {Promise} Promise với dữ liệu thống kê
   */
  getDashboardStats: async () => {
    try {
      const response = await axiosClient.get('/admin/dashboard/stats');
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Lấy tất cả người dùng với phân trang và bộ lọc
   * @param {Object} params - Tham số truy vấn
   * @param {string} params.roleCode - Lọc theo vai trò (ADM, DN, UV)
   * @param {string} params.search - Tìm kiếm theo tên hoặc email
   * @param {number} params.page - Số trang (bắt đầu từ 0)
   * @param {number} params.size - Kích thước trang
   * @param {string} params.sort - Tiêu chí sắp xếp (ví dụ: 'username,asc')
   * @returns {Promise} Promise với dữ liệu người dùng phân trang
   */
  getAllUsers: async (params = {}) => {
    try {
      const response = await axiosClient.get('/admin/users', { params });
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Khóa/tạm ngưng tài khoản người dùng
   * @param {number} userId - ID người dùng cần khóa
   * @returns {Promise} Promise với thông báo thành công
   */
  lockUser: async (userId) => {
    try {
      const response = await axiosClient.patch(`/admin/users/${userId}/lock`);
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Mở khóa/mở lại tài khoản người dùng
   * @param {number} userId - ID người dùng cần mở khóa
   * @returns {Promise} Promise với thông báo thành công
   */
  unlockUser: async (userId) => {
    try {
      const response = await axiosClient.patch(`/admin/users/${userId}/unlock`);
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Lấy việc làm theo trạng thái với phân trang
   * @param {Object} params - Tham số truy vấn
   * @param {string} params.jobStatus - Lọc theo trạng thái việc làm (PENDING, ACTIVE, REJECTED, v.v.)
   * @param {number} params.page - Số trang (bắt đầu từ 0)
   * @param {number} params.size - Kích thước trang
   * @param {string} params.sort - Tiêu chí sắp xếp (ví dụ: 'createdDate,desc')
   * @returns {Promise} Promise với dữ liệu việc làm phân trang
   */
  getJobsByStatus: async (params = {}) => {
    try {
      const response = await axiosClient.get('/jobs', { params });
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Xóa việc làm (do vi phạm chính sách)
   * @param {number} jobId - ID việc làm cần xóa
   * @returns {Promise} Promise với thông báo thành công
   */
  deleteJob: async (jobId) => {
    try {
      const response = await axiosClient.delete(`/admin/jobs/${jobId}`);
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Xóa bài đăng tìm việc (do vi phạm chính sách)
   * @param {number} skPostId - ID bài đăng tìm việc cần xóa
   * @returns {Promise} Promise với thông báo thành công
   */
  deleteSeekingPost: async (skPostId) => {
    try {
      const response = await axiosClient.delete(`/admin/seeking-posts/${skPostId}`);
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Thay đổi trạng thái việc làm (ACTIVE ↔ HIDDEN)
   * @param {number} jobId - ID việc làm
   * @param {string} newStatus - Trạng thái mới (ACTIVE hoặc HIDDEN)
   * @returns {Promise} Promise với việc làm đã được cập nhật
   */
  changeJobStatus: async (jobId, newStatus) => {
    try {
      const response = await axiosClient.patch(
        `/admin/jobs/${jobId}/status`,
        null,
        { params: { newStatus } }
      );
      return response;
    } catch (error) {
      throw error;
    }
  },
  
  /**
   * Chuyển đổi trạng thái việc làm (ACTIVE ↔ HIDDEN)
   * @param {number} jobId - ID việc làm cần chuyển đổi
   * @returns {Promise} Promise với thông báo thành công
   */
  toggleJobStatus: async (jobId) => {
    try {
      const response = await axiosClient.patch(`/admin/jobs/${jobId}/toggle-status`);
      return response;
    } catch (error) {
      throw error;
    }
  },
  
  /**
   * Lấy tất cả bài đăng tìm việc (Admin)
   * @param {Object} params - Tham số truy vấn
   * @param {number} params.page - Số trang (bắt đầu từ 0)
   * @param {number} params.size - Kích thước trang
   * @returns {Promise} Promise với dữ liệu bài đăng tìm việc phân trang
   */
  getAllSeekingPosts: async (params = {}) => {
    try {
      const response = await axiosClient.get('/admin/seeking-posts', { params });
      return response;
    } catch (error) {
      throw error;
    }
  },
  
  /**
   * Chuyển đổi trạng thái bài đăng tìm việc (ACTIVE ↔ HIDDEN)
   * @param {number} seekingPostId - ID bài đăng tìm việc cần chuyển đổi
   * @returns {Promise} Promise với thông báo thành công
   */
  toggleSeekingPostStatus: async (seekingPostId) => {
    try {
      const response = await axiosClient.patch(`/admin/seeking-posts/${seekingPostId}/toggle-status`);
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Thay đổi trạng thái công ty (PENDING → ACTIVE hoặc BLOCKED)
   * @param {number} companyId - ID công ty
   * @param {string} newStatus - Trạng thái mới (ACTIVE, PENDING, hoặc BLOCKED)
   * @returns {Promise} Promise với thông báo thành công
   */
  changeCompanyStatus: async (companyId, newStatus) => {
    try {
      const response = await axiosClient.patch(
        `/admin/companies/${companyId}/status`,
        null,
        { params: { newStatus } }
      );
      return response;
    } catch (error) {
      throw error;
    }
  },
};

export default adminService;
