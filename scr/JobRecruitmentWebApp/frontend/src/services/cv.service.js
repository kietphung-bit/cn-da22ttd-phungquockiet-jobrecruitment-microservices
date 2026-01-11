import axiosClient from '../api/axiosClient';

/**
 * CV Service
 * Xử lý tất cả các cuộc gọi API liên quan đến CV cho ứng viên
 * 
 * Endpoints:
 * - POST /api/v1/cvs - Tải lên file CV
 * - GET /api/v1/cvs/me - Lấy CV của tôi
 * - PATCH /api/v1/cvs/{id}/status - Cập nhật trạng thái CV
 * - DELETE /api/v1/cvs/{id} - Xóa CV (xóa mềm)
 */
const cvService = {
  /**
   * Tải lên file CV
   * @param {File} file - Đối tượng file PDF từ <input type="file">
   * @returns {Promise} Promise với dữ liệu phản hồi CV
   */
  uploadCV: async (file) => {
    try {
      // Tạo FormData để tải lên multipart/form-data
      const formData = new FormData();
      formData.append('file', file); // Key 'file' phải khớp với Backend @RequestParam
      
      const response = await axiosClient.post('/cvs', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Lấy tất cả CV của ứng viên đã xác thực
   * @returns {Promise} Promise với danh sách dữ liệu CV
   */
  getMyCVs: async () => {
    try {
      const response = await axiosClient.get('/cvs/me');
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Cập nhật trạng thái CV (ACTIVE/HIDDEN)
   * @param {number} cvId - ID CV
   * @param {string} status - Trạng thái mới: 'ACTIVE' hoặc 'HIDDEN'
   * @returns {Promise} Promise với dữ liệu CV đã cập nhật
   */
  updateCVStatus: async (cvId, status) => {
    try {
      const response = await axiosClient.patch(`/cvs/${cvId}/status`, null, {
        params: {
          newStatus: status, // Key 'newStatus' phải khớp với tên @RequestParam của Backend
        },
      });
      
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Xóa CV (xóa mềm - đặt trạng thái thành HIDDEN)
   * @param {number} cvId - ID CV
   * @returns {Promise} Promise với thông báo thành công
   */
  deleteCV: async (cvId) => {
    try {
      const response = await axiosClient.delete(`/cvs/${cvId}`);
      return response;
    } catch (error) {
      throw error;
    }
  },
};

export default cvService;
