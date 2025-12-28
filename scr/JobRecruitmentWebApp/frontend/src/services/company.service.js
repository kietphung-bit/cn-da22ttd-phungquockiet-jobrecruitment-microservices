import axiosClient from '../api/axiosClient';

/**
 * Company Service
 * Handles all company-related API calls
 */
const companyService = {
  /**
   * Get all companies with pagination
   * @param {Object} params - Query parameters
   * @param {number} params.page - Page number (0-indexed)
   * @param {number} params.size - Page size
   * @param {string} params.name - Search by company name
   * @param {string} params.sort - Sort field and direction
   * @returns {Promise} Promise with companies data
   */
  getAllCompanies: async (params = {}) => {
    try {
      const response = await axiosClient.get('/companies', { params });
      return response;
    } catch (error) {
      console.error('Error fetching companies:', error);
      throw error;
    }
  },

  /**
   * Get company details by ID
   * @param {number|string} companyId - Company ID
   * @returns {Promise} Promise with company details
   */
  getCompanyDetail: async (companyId) => {
    try {
      const response = await axiosClient.get(`/companies/${companyId}`);
      return response;
    } catch (error) {
      console.error('Error fetching company details:', error);
      throw error;
    }
  },

  /**
   * Get featured companies for homepage
   * @param {number} size - Number of companies to fetch (default: 5)
   * @returns {Promise} Promise with featured companies
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
      console.error('Error fetching featured companies:', error);
      throw error;
    }
  },

  /**
   * Search companies with keyword
   * @param {Object} params - Search parameters
   * @param {number} params.page - Page number (0-indexed)
   * @param {number} params.size - Page size
   * @param {string} params.keyword - Search keyword for company name
   * @param {string} params.sort - Sort field and direction
   * @returns {Promise} Promise with search results
   */
  searchCompanies: async (params = {}) => {
    try {
      // Clean up undefined values
      const cleanParams = Object.entries(params).reduce((acc, [key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          acc[key] = value;
        }
        return acc;
      }, {});

      const response = await axiosClient.get('/companies', { params: cleanParams });
      return response;
    } catch (error) {
      console.error('Error searching companies:', error);
      throw error;
    }
  },

  /**
   * Get jobs by company ID
   * @param {number|string} companyId - Company ID
   * @param {Object} params - Additional query parameters
   * @param {number} params.page - Page number (0-indexed)
   * @param {number} params.size - Page size
   * @returns {Promise} Promise with jobs data
   */
  getJobsByCompany: async (companyId, params = {}) => {
    try {
      // Use /jobs endpoint with companyId filter instead of nested resource
      const response = await axiosClient.get('/jobs', { 
        params: { ...params, companyId } 
      });
      return response;
    } catch (error) {
      console.error('Error fetching company jobs:', error);
      throw error;
    }
  },
};

export default companyService;
