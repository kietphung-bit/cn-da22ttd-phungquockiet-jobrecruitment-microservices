import axiosClient from '../api/axiosClient';

/**
 * Saved Job Service
 * Gọi API quản lý các công việc đã lưu/đánh dấu
 * 
 * Endpoints:
 * - POST /saved-jobs - Lưu/đánh dấu một công việc
 * - GET /saved-jobs/me - Lấy các công việc đã lưu của tôi (có phân trang)
 * - DELETE /saved-jobs/{jobId} - Bỏ lưu/bỏ đánh dấu một công việc
 * - GET /saved-jobs/check/{jobId} - Kiểm tra xem công việc đã được lưu chưa (tùy chọn)
 * 
 * Backend: SavedJobControllerV1.java
 */
const savedJobService = {
  /**
   * Lưu/đánh dấu một công việc
   * @param {number} jobId - ID công việc cần lưu
   * @returns {Promise} Promise với SavedJobResponse
   */
  saveJob: async (jobId) => {
    try {
      const response = await axiosClient.post('/saved-jobs', { jobId });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Lấy các công việc đã lưu của tôi (có phân trang)
   * @param {Object} params - Tham số truy vấn { page, size, sort }
   * @returns {Promise} Promise với Page<SavedJobResponse>
   */
  getMySavedJobs: async (params = {}) => {
    try {
      const response = await axiosClient.get('/saved-jobs/me', { params });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Bỏ lưu/bỏ đánh dấu một công việc
   * @param {number} jobId - ID công việc cần bỏ lưu
   * @returns {Promise} Promise với phản hồi thành công
   */
  unsaveJob: async (jobId) => {
    try {
      const response = await axiosClient.delete(`/saved-jobs/${jobId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Kiểm tra xem một công việc đã được lưu chưa
   * Lưu ý: Đây là phương thức trợ giúp kiểm tra dựa trên danh sách công việc đã lưu
   * Để hiệu suất tốt hơn, bạn có thể muốn thêm một endpoint riêng trong backend
   * @param {number} jobId - ID công việc cần kiểm tra
   * @returns {Promise<boolean>} True nếu đã lưu, false nếu chưa
   */
  checkIsSaved: async (jobId) => {
    try {
      // Lấy trang đầu tiên để kiểm tra
      const response = await axiosClient.get('/saved-jobs/me', {
        params: { page: 0, size: 100 } // Get enough to check
      });
      
      // Xử lý các cấu trúc phản hồi khác nhau
      let savedJobs = [];
      if (response.data?.data?.content) {
        // ApiResponse wrapper
        savedJobs = response.data.data.content;
      } else if (response.data?.content) {
        // Trang trực tiếp
        savedJobs = response.data.content;
      } else if (Array.isArray(response.data)) {
        // Mảng trực tiếp
        savedJobs = response.data;
      }
      
      // Chuyển jobId sang số để so sánh
      const jobIdNum = typeof jobId === 'string' ? parseInt(jobId, 10) : jobId;
      
      return savedJobs.some(saved => {
        const savedJobId = saved.jobId || saved.job?.jobId;
        return savedJobId === jobIdNum || savedJobId === jobId;
      });
    } catch (error) {
      return false;
    }
  },

  /**
   * Lấy tất cả ID công việc đã lưu (để kiểm tra nhiều công việc cùng lúc)
   * @returns {Promise<Array<number>>} Mảng các ID công việc đã lưu
   */
  getSavedJobIds: async () => {
    try {
      const response = await axiosClient.get('/saved-jobs/me', {
        params: { page: 0, size: 1000 } // Lấy tất cả công việc đã lưu
      });
      
      // Xử lý các cấu trúc phản hồi khác nhau
      let savedJobs = [];
      if (response.data?.data?.content) {
        savedJobs = response.data.data.content;
      } else if (response.data?.content) {
        savedJobs = response.data.content;
      } else if (Array.isArray(response.data)) {
        savedJobs = response.data;
      }
      
      return savedJobs.map(saved => saved.jobId || saved.job?.jobId).filter(Boolean);
    } catch (error) {
      return [];
    }
  }
};

export default savedJobService;
