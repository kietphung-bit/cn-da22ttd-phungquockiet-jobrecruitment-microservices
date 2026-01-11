import axiosClient from '../api/axiosClient';

/**
 * Company Service
 * Xử lý tất cả các cuộc gọi API liên quan đến công ty
 */
const companyService = {
  /**
   * Lấy tất cả công ty với phân trang và lọc
   * @param {Object} params - Tham số truy vấn
   * @param {number} params.page - Số trang (bắt đầu từ 0)
   * @param {number} params.size - Kích thước trang
   * @param {string} params.name - Tìm kiếm theo tên công ty
   * @param {string} params.sort - Trường và hướng sắp xếp
   * @returns {Promise} Promise với dữ liệu công ty
   */
  getAllCompanies: async (params = {}) => {
    try {
      const response = await axiosClient.get('/companies', { params });
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Lấy chi tiết công ty theo ID
   * @param {number|string} companyId - ID công ty
   * @returns {Promise} Promise với chi tiết công ty
   */
  getCompanyDetail: async (companyId) => {
    try {
      const response = await axiosClient.get(`/companies/${companyId}`);
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Lấy các công ty nổi bật cho trang chủ
   * @param {number} size - Số lượng công ty cần lấy (mặc định: 5)
   * @returns {Promise} Promise với các công ty nổi bật
   */
  getFeaturedCompanies: async (size = 5) => {
    try {
      const response = await axiosClient.get('/companies', {
        params: {
          page: 0,
          size: size,
          sort: 'companyName,asc',
        },
      });
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Tìm kiếm công ty với từ khóa
   * @param {Object} params - Tham số tìm kiếm
   * @param {number} params.page - Số trang (bắt đầu từ 0)
   * @param {number} params.size - Kích thước trang
   * @param {string} params.keyword - Từ khóa tìm kiếm tên công ty (map tới 'name' ở backend)
   * @param {string} params.sort - Trường và hướng sắp xếp
   * @returns {Promise} Promise với kết quả tìm kiếm
   */
  searchCompanies: async (params = {}) => {
    try {
      // Chuyển tên tham số frontend sang tên tham số mà backend mong đợi
      const backendParams = {};
      
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          // Chuyển keyword thành name (backend mong đợi tham số 'name' để tìm kiếm tên công ty)
          if (key === 'keyword') {
            backendParams.name = value;
          } else {
            backendParams[key] = value;
          }
        }
      });

      const response = await axiosClient.get('/companies', { params: backendParams });
      return response;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Lấy các công việc theo ID công ty
   * @param {number|string} companyId - ID công ty
   * @param {Object} params - Tham số truy vấn bổ sung
   * @param {number} params.page - Số trang (bắt đầu từ 0)
   * @param {number} params.size - Kích thước trang
   * @returns {Promise} Promise với dữ liệu công việc
   */
  getJobsByCompany: async (companyId, params = {}) => {
    try {
      // Sử dụng endpoint /jobs với bộ lọc companyId thay vì tài nguyên lồng nhau
      const response = await axiosClient.get('/jobs', { 
        params: { ...params, companyId } 
      });
      return response;
    } catch (error) {
      throw error;
    }
  },
};

export default companyService;
