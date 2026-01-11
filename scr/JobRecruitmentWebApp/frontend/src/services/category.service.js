import axiosClient from '../api/axiosClient';

/**
 * Category Service
 * Xử lý tất cả các cuộc gọi API liên quan đến danh mục công việc
 */
const categoryService = {
  /**
   * Lấy tất cả danh mục công việc
   * @returns {Promise} Promise với dữ liệu danh mục công việc
   */
  getAllCategories: async () => {
    try {
      const response = await axiosClient.get('/categories');
      return response;
    } catch (error) {
      throw error;
    }
  },
};

export default categoryService;
