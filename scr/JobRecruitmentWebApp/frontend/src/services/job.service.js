import axiosClient from '../api/axiosClient';

/**
 * Job Service
 * Xử lý tất cả các cuộc gọi API liên quan đến công việc
 */
const jobService = {
  /**
   * Lấy các công việc hot cho trang chủ
   * @param {number} size - Số lượng công việc cần lấy (mặc định: 8)
   * @returns {Promise} Promise với dữ liệu công việc
   */
  getHotJobs: async (size = 8) => {
    try {
      const response = await axiosClient.get('/jobs', {
        params: {
          page: 0,
          size: size,
          sort: 'createdAt,desc',
        },
      });
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Tìm kiếm công việc với bộ lọc
   * @param {Object} filters - Bộ lọc tìm kiếm
   * @param {number} filters.page - Số trang (bắt đầu từ 0)
   * @param {number} filters.size - Kích thước trang
   * @param {string} filters.keyword - Từ khóa tìm kiếm (map tới jobTitle ở backend)
   * @param {string} filters.location - Vị trí công việc (map tới jobLocation ở backend)
   * @param {number} filters.jcId - ID danh mục công việc
   * @param {number} filters.minSalary - Mức lương tối thiểu
   * @param {number} filters.maxSalary - Mức lương tối đa
   * @param {string} filters.jobType - Loại công việc
   * @param {string} filters.sort - Trường và hướng sắp xếp
   * @returns {Promise} Promise với kết quả tìm kiếm
   */
  searchJobs: async (filters = {}) => {
    try {
      // Chuyển tên tham số frontend sang tên tham số mà backend mong đợi
      const backendParams = {};
      
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          // Chuyển tên tham số frontend sang tên tham số mà backend mong đợi
          if (key === 'keyword') {
            backendParams.jobTitle = value; 
          } else if (key === 'location') {
            backendParams.jobLocation = value; 
          } else {
            backendParams[key] = value; // Giữ nguyên các tham số khác
          }
        }
      });

      const response = await axiosClient.get('/jobs', { params: backendParams });
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Lấy chi tiết công việc theo ID
   * @param {number|string} jobId - ID công việc
   * @returns {Promise} Promise với chi tiết công việc
   */
  getJobDetail: async (jobId) => {
    try {
      const response = await axiosClient.get(`/jobs/${jobId}`);
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Lấy các công việc liên quan (công việc tương tự dựa trên danh mục)
   * @param {number} jcId - ID danh mục công việc
   * @param {number} excludeJobId - ID công việc cần loại trừ khỏi kết quả
   * @param {number} size - Số lượng công việc cần lấy (mặc định: 4)
   * @returns {Promise} Promise với các công việc liên quan
   */
  getRelatedJobs: async (jcId, excludeJobId, size = 4) => {
    try {
      const response = await axiosClient.get('/jobs', {
        params: {
          jcId: jcId,
          page: 0,
          size: size + 1, // Lấy thêm một công việc để loại trừ công việc hiện tại
        },
      });

      // Lọc ra công việc hiện tại khỏi kết quả
      if (response.data && response.data.content) {
        response.data.content = response.data.content
          .filter((job) => job.jobId !== excludeJobId)
          .slice(0, size);
      }

      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Lấy tất cả danh mục công việc
   * @returns {Promise} Promise với danh mục công việc
   */
  getJobCategories: async () => {
    try {
      const response = await axiosClient.get('/categories');
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Lấy các công việc của nhà tuyển dụng (đã xác thực)
   * Endpoint: GET /api/v1/jobs/me
   * @param {Object} params - Tham số truy vấn (page, size, sort, v.v.)
   * @returns {Promise} Promise với các công việc của nhà tuyển dụng
   */
  getMyJobs: async (params = {}) => {
    try {
      const response = await axiosClient.get('/jobs/me', { params });
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Tạo mới tin tuyển dụng (Chỉ nhà tuyển dụng)
   * @param {Object} jobData - Dữ liệu tạo tin tuyển dụng
   * @returns {Promise} Promise với tin tuyển dụng đã tạo
   */
  createJob: async (jobData) => {
    try {
      const response = await axiosClient.post('/jobs', jobData);
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Cập nhật tin tuyển dụng (Chỉ nhà tuyển dụng)
   * @param {number} jobId - ID công việc
   * @param {Object} jobData - Dữ liệu cập nhật tin tuyển dụng
   * @returns {Promise} Promise với tin tuyển dụng đã cập nhật
   */
  updateJob: async (jobId, jobData) => {
    try {
      const response = await axiosClient.put(`/jobs/${jobId}`, jobData);
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Cập nhật trạng thái tin tuyển dụng (Chỉ nhà tuyển dụng)
   * @param {number} jobId - ID công việc
   * @param {string} status - Trạng thái mới (ACTIVE, CLOSED, HIDDEN)
   * @returns {Promise} Promise với tin tuyển dụng đã cập nhật
   */
  updateJobStatus: async (jobId, status) => {
    try {
      const response = await axiosClient.patch(`/jobs/${jobId}/status`, null, {
        params: { status }
      });
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Xóa tin tuyển dụng (xóa mềm - đặt trạng thái thành HIDDEN)
   * @param {number} jobId - ID công việc
   * @returns {Promise} Promise với phản hồi void
   */
  deleteJob: async (jobId) => {
    try {
      const response = await axiosClient.delete(`/jobs/${jobId}`);
      return response;
    } catch (error) {
      throw error;
    }
  },
};

export default jobService;
