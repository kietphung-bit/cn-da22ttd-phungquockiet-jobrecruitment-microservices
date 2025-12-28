import axiosClient from '../api/axiosClient';

/**
 * Saved Job Service
 * API calls for managing saved/bookmarked jobs
 * 
 * Endpoints:
 * - POST /saved-jobs - Save/bookmark a job
 * - GET /saved-jobs/me - Get my saved jobs (paginated)
 * - DELETE /saved-jobs/{jobId} - Unsave/unbookmark a job
 * - GET /saved-jobs/check/{jobId} - Check if job is saved (optional)
 * 
 * Backend: SavedJobControllerV1.java
 */
const savedJobService = {
  /**
   * Save/bookmark a job
   * @param {number} jobId - Job ID to save
   * @returns {Promise} Response with SavedJobResponse
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
   * Get my saved jobs with pagination
   * @param {Object} params - Query parameters { page, size, sort }
   * @returns {Promise} Response with Page<SavedJobResponse>
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
   * Unsave/unbookmark a job
   * @param {number} jobId - Job ID to unsave
   * @returns {Promise} Response with success message
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
   * Check if a job is saved
   * Note: This is a helper method that checks against the saved jobs list
   * For better performance, you might want to add a dedicated endpoint in backend
   * @param {number} jobId - Job ID to check
   * @returns {Promise<boolean>} True if saved, false otherwise
   */
  checkIsSaved: async (jobId) => {
    try {
      // Fetch first page only to check
      const response = await axiosClient.get('/saved-jobs/me', {
        params: { page: 0, size: 100 } // Get enough to check
      });
      
      console.log('checkIsSaved response:', response);
      
      // Handle different response structures
      let savedJobs = [];
      if (response.data?.data?.content) {
        // ApiResponse wrapper
        savedJobs = response.data.data.content;
      } else if (response.data?.content) {
        // Direct Page
        savedJobs = response.data.content;
      } else if (Array.isArray(response.data)) {
        // Direct array
        savedJobs = response.data;
      }
      
      console.log('Checking if job is saved:', { jobId, savedJobs });
      
      // Convert jobId to number for comparison
      const jobIdNum = typeof jobId === 'string' ? parseInt(jobId, 10) : jobId;
      
      return savedJobs.some(saved => {
        const savedJobId = saved.jobId || saved.job?.jobId;
        return savedJobId === jobIdNum || savedJobId === jobId;
      });
    } catch (error) {
      console.error('Error checking if job is saved:', error);
      return false;
    }
  },

  /**
   * Get all saved job IDs (for checking multiple jobs at once)
   * @returns {Promise<Array<number>>} Array of saved job IDs
   */
  getSavedJobIds: async () => {
    try {
      const response = await axiosClient.get('/saved-jobs/me', {
        params: { page: 0, size: 1000 } // Get all saved jobs
      });
      
      // Handle different response structures
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
      console.error('Error fetching saved job IDs:', error);
      return [];
    }
  }
};

export default savedJobService;
