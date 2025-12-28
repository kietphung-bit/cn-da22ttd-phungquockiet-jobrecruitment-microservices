import axiosClient from '../api/axiosClient';

/**
 * Category Service
 * Handles all job category-related API calls
 */
const categoryService = {
  /**
   * Get all job categories
   * @returns {Promise} Promise with categories data
   */
  getAllCategories: async () => {
    try {
      const response = await axiosClient.get('/categories');
      return response;
    } catch (error) {
      console.error('Error fetching categories:', error);
      throw error;
    }
  },
};

export default categoryService;
