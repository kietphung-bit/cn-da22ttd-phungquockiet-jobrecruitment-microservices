import axiosClient from '../api/axiosClient';

/**
 * Admin Service
 * API calls for admin operations (user management, job moderation)
 * 
 * Base URL: /api/v1/admin
 * 
 * Endpoints:
 * - GET /stats - Dashboard statistics
 * - GET /users - List users with pagination and filters
 * - PATCH /users/{id}/lock - Lock user account
 * - PATCH /users/{id}/unlock - Unlock user account
 * - GET /jobs - List jobs with status filter
 * - PATCH /jobs/{id}/status - Change job status (approve/reject)
 * 
 * All endpoints require ROLE_ADM authorization
 */
const adminService = {
  /**
   * Get dashboard statistics
   * @returns {Promise} Promise with stats data
   */
  getDashboardStats: async () => {
    try {
      const response = await axiosClient.get('/admin/stats');
      return response;
    } catch (error) {
      console.error('Error fetching dashboard stats:', error);
      throw error;
    }
  },

  /**
   * Get all users with pagination and filters
   * @param {Object} params - Query parameters
   * @param {string} params.roleCode - Filter by role (ADM, DN, UV)
   * @param {string} params.search - Search by name or email
   * @param {number} params.page - Page number (0-indexed)
   * @param {number} params.size - Page size
   * @param {string} params.sort - Sort criteria (e.g., 'username,asc')
   * @returns {Promise} Promise with paginated users data
   */
  getAllUsers: async (params = {}) => {
    try {
      const response = await axiosClient.get('/admin/users', { params });
      return response;
    } catch (error) {
      console.error('Error fetching users:', error);
      throw error;
    }
  },

  /**
   * Lock/ban a user account
   * @param {number} userId - User ID to lock
   * @returns {Promise} Promise with success message
   */
  lockUser: async (userId) => {
    try {
      const response = await axiosClient.patch(`/admin/users/${userId}/lock`);
      return response;
    } catch (error) {
      console.error('Error locking user:', error);
      throw error;
    }
  },

  /**
   * Unlock/unban a user account
   * @param {number} userId - User ID to unlock
   * @returns {Promise} Promise with success message
   */
  unlockUser: async (userId) => {
    try {
      const response = await axiosClient.patch(`/admin/users/${userId}/unlock`);
      return response;
    } catch (error) {
      console.error('Error unlocking user:', error);
      throw error;
    }
  },

  /**
   * Get jobs by status with pagination
   * @param {Object} params - Query parameters
   * @param {string} params.jobStatus - Filter by job status (PENDING, ACTIVE, REJECTED, etc.)
   * @param {number} params.page - Page number (0-indexed)
   * @param {number} params.size - Page size
   * @param {string} params.sort - Sort criteria (e.g., 'createdDate,desc')
   * @returns {Promise} Promise with paginated jobs data
   */
  getJobsByStatus: async (params = {}) => {
    try {
      const response = await axiosClient.get('/jobs', { params });
      return response;
    } catch (error) {
      console.error('Error fetching jobs:', error);
      throw error;
    }
  },

  /**
   * Approve a job (PENDING → ACTIVE)
   * @param {number} jobId - Job ID to approve
   * @returns {Promise} Promise with success message
   */
  approveJob: async (jobId) => {
    try {
      const response = await axiosClient.patch(
        `/admin/jobs/${jobId}/status`,
        null,
        { params: { newStatus: 'ACTIVE' } }
      );
      return response;
    } catch (error) {
      console.error('Error approving job:', error);
      throw error;
    }
  },

  /**
   * Reject a job (PENDING → REJECTED)
   * @param {number} jobId - Job ID to reject
   * @returns {Promise} Promise with success message
   */
  rejectJob: async (jobId) => {
    try {
      const response = await axiosClient.patch(
        `/admin/jobs/${jobId}/status`,
        null,
        { params: { newStatus: 'REJECTED' } }
      );
      return response;
    } catch (error) {
      console.error('Error rejecting job:', error);
      throw error;
    }
  },
};

export default adminService;
