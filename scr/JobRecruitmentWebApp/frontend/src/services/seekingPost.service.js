import axiosClient from '../api/axiosClient';

/**
 * SeekingPost Service
 * Xử lý tất cả các cuộc gọi API liên quan đến hồ sơ tìm việc
 * 
 * Endpoints:
 * - GET /seeking-posts - Tìm kiếm/danh sách hồ sơ tìm việc (công khai/nhà tuyển dụng)
 * - GET /seeking-posts/my - Lấy hồ sơ tìm việc của ứng viên hiện tại
 * - POST /seeking-posts - Tạo hồ sơ tìm việc mới (chỉ ứng viên)
 * - PUT /seeking-posts/{id} - Cập nhật hồ sơ tìm việc (chỉ ứng viên)
 * - DELETE /seeking-posts/{id} - Xóa hồ sơ tìm việc (chỉ ứng viên)
 * - GET /seeking-posts/{id} - Lấy chi tiết hồ sơ tìm việc
 * 
 * Che dữ liệu/chưa che dựa trên vai trò người dùng:
 * - Backend xử lý logic che dữ liệu dựa trên vai trò người dùng
 * - Khách/Ứng viên: Tên được che, không có thông tin liên hệ
 * - Nhà tuyển dụng: Tên đầy đủ, thông tin liên hệ đầy đủ
 * - Frontend chỉ hiển thị những gì backend trả về
 */
const seekingPostService = {
  /**
   * Tìm kiếm/danh sách hồ sơ tìm việc (công khai/nhà tuyển dụng)
   * @param {Object} params - Tham số truy vấn
   * @param {number} params.page - Số trang (bắt đầu từ 0)
   * @param {number} params.size - Kích thước trang
   * @param {string} params.keyword - Từ khóa tìm kiếm (tiêu đề, kỹ năng)
   * @param {string} params.location - Lọc theo địa điểm
   * @param {string} params.skills - Lọc theo kỹ năng (phân tách bằng dấu phẩy)
   * @param {string} params.sort - Sắp xếp theo trường và hướng
   * @returns {Promise} Promise với dữ liệu hồ sơ tìm việc
   */
  searchSeekingPosts: async (params = {}) => {
    try {
      // Dọn dẹp các giá trị undefined/null khỏi tham số
      const cleanParams = Object.entries(params).reduce((acc, [key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          acc[key] = value;
        }
        return acc;
      }, {});

      const response = await axiosClient.get('/seeking-posts', { params: cleanParams });
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Lấy chi tiết hồ sơ tìm việc theo ID
   * Dữ liệu được che/chưa che dựa trên vai trò người dùng (backend xử lý)
   * @param {number|string} skPostId - ID hồ sơ tìm việc
   * @returns {Promise} Promise với chi tiết hồ sơ tìm việc
   */
  getSeekingPostDetail: async (skPostId) => {
    try {
      const response = await axiosClient.get(`/seeking-posts/${skPostId}`);
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Lấy hồ sơ tìm việc của ứng viên hiện tại
   * Yêu cầu xác thực (ROLE_UV)
   * Endpoint: GET /api/v1/seeking-posts/me
   * @param {Object} params - Tham số truy vấn (page, size, sort)
   * @returns {Promise} Promise với hồ sơ tìm việc của ứng viên (dữ liệu đầy đủ)
   */
  getMySeekingPosts: async (params = {}) => {
    try {
      const response = await axiosClient.get('/seeking-posts/me', { params });
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Tạo hồ sơ tìm việc mới (chỉ ứng viên)
   * @param {Object} postData - Dữ liệu hồ sơ tìm việc
   * @param {string} postData.skPostTitle - Tiêu đề hồ sơ
   * @param {string} postData.desiredSalary - Mức lương mong muốn
   * @param {string} postData.desiredLocation - Địa điểm mong muốn
   * @param {string} postData.skPostSkills - Kỹ năng (phân tách bằng dấu phẩy hoặc JSON)
   * @param {string} postData.skPostIntro - Giới thiệu/mô tả
   * @param {string} postData.expiryDate - Ngày hết hạn (YYYY-MM-DD)
   * @returns {Promise} Promise với hồ sơ tìm việc đã tạo
   */
  createSeekingPost: async (postData) => {
    try {
      const response = await axiosClient.post('/seeking-posts', postData);
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Cập nhật hồ sơ tìm việc (chỉ ứng viên)
   * @param {number|string} skPostId - ID hồ sơ tìm việc
   * @param {Object} postData - Dữ liệu hồ sơ tìm việc đã cập nhật
   * @returns {Promise} Promise với hồ sơ tìm việc đã cập nhật
   */
  updateSeekingPost: async (skPostId, postData) => {
    try {
      const response = await axiosClient.put(`/seeking-posts/${skPostId}`, postData);
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Xóa hồ sơ tìm việc (chỉ ứng viên)
   * @param {number|string} skPostId - ID hồ sơ tìm việc
   * @returns {Promise} Promise với thông báo thành công
   */
  deleteSeekingPost: async (skPostId) => {
    try {
      // Sử dụng endpoint /my/{id} để xóa hồ sơ tìm việc của ứng viên (không phải admin /{id})
      const response = await axiosClient.delete(`/seeking-posts/my/${skPostId}`);
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Chuyển đổi trạng thái hồ sơ tìm việc (ACTIVE ↔ HIDDEN)
   * @param {number|string} skPostId - ID hồ sơ tìm việc
   * @param {string} newStatus - Trạng thái mới ('ACTIVE' hoặc 'HIDDEN')
   * @returns {Promise} Promise với hồ sơ tìm việc đã cập nhật
   */
  toggleSeekingPostStatus: async (skPostId, newStatus) => {
    try {
      const response = await axiosClient.patch(`/seeking-posts/${skPostId}/status?status=${newStatus}`);
      return response;
    } catch (error) {
      // Trả về PUT với tham số truy vấn
      try {
        const fallbackResponse = await axiosClient.put(`/seeking-posts/${skPostId}/status?status=${newStatus}`);
        return fallbackResponse;
      } catch (fallbackError) {
        throw error; // Throw original error
      }
    }
  },
};

export default seekingPostService;
