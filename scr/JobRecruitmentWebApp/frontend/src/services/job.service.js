import axiosClient from '../api/axiosClient';

/**
 * Job Service
 * Handles all job-related API calls
 */
const jobService = {
  /**
   * Get hot jobs for homepage
   * @param {number} size - Number of jobs to fetch (default: 8)
   * @returns {Promise} Promise with job data
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
      console.error('Error fetching hot jobs:', error);
      throw error;
    }
  },

  /**
   * Search jobs with filters
   * @param {Object} filters - Search filters
   * @param {number} filters.page - Page number (0-indexed)
   * @param {number} filters.size - Page size
   * @param {string} filters.keyword - Search keyword
   * @param {string} filters.location - Job location
   * @param {number} filters.jcId - Job category ID
   * @param {number} filters.minSalary - Minimum salary
   * @param {number} filters.maxSalary - Maximum salary
   * @param {string} filters.jobType - Job type
   * @param {string} filters.sort - Sort field and direction
   * @returns {Promise} Promise with search results
   */
  searchJobs: async (filters = {}) => {
    try {
      // Clean up undefined values
      const params = Object.entries(filters).reduce((acc, [key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          acc[key] = value;
        }
        return acc;
      }, {});

      const response = await axiosClient.get('/jobs', { params });
      return response;
    } catch (error) {
      console.error('Error searching jobs:', error);
      throw error;
    }
  },

  /**
   * Get job details by ID
   * @param {number|string} jobId - Job ID
   * @returns {Promise} Promise with job details
   */
  getJobDetail: async (jobId) => {
    try {
      const response = await axiosClient.get(`/jobs/${jobId}`);
      return response;
    } catch (error) {
      console.error('Error fetching job details:', error);
      throw error;
    }
  },

  /**
   * Get related jobs (similar jobs based on category)
   * @param {number} jcId - Job category ID
   * @param {number} excludeJobId - Job ID to exclude from results
   * @param {number} size - Number of jobs to fetch (default: 4)
   * @returns {Promise} Promise with related jobs
   */
  getRelatedJobs: async (jcId, excludeJobId, size = 4) => {
    try {
      const response = await axiosClient.get('/jobs', {
        params: {
          jcId: jcId,
          page: 0,
          size: size + 1, // Fetch one extra to exclude current job
        },
      });

      // Filter out the current job
      if (response.data && response.data.content) {
        response.data.content = response.data.content
          .filter((job) => job.jobId !== excludeJobId)
          .slice(0, size);
      }

      return response;
    } catch (error) {
      console.error('Error fetching related jobs:', error);
      throw error;
    }
  },

  /**
   * Get all job categories
   * @returns {Promise} Promise with job categories
   */
  getJobCategories: async () => {
    try {
      const response = await axiosClient.get('/categories');
      return response;
    } catch (error) {
      console.error('Error fetching job categories:', error);
      throw error;
    }
  },

  /**
   * Get employer's own jobs (authenticated)
   * @param {Object} params - Query parameters (page, size, sort, etc.)
   * @returns {Promise} Promise with employer's jobs
   */
  getMyJobs: async (params = {}) => {
    try {
      const response = await axiosClient.get('/jobs', { params });
      return response;
    } catch (error) {
      console.error('Error fetching my jobs:', error);
      throw error;
    }
  },

  /**
   * Create new job posting (Employer only)
   * @param {Object} jobData - Job creation data
   * @returns {Promise} Promise with created job
   */
  createJob: async (jobData) => {
    try {
      const response = await axiosClient.post('/jobs', jobData);
      return response;
    } catch (error) {
      console.error('Error creating job:', error);
      throw error;
    }
  },

  /**
   * Update existing job (Employer only)
   * @param {number} jobId - Job ID
   * @param {Object} jobData - Updated job data
   * @returns {Promise} Promise with updated job
   */
  updateJob: async (jobId, jobData) => {
    try {
      const response = await axiosClient.put(`/jobs/${jobId}`, jobData);
      return response;
    } catch (error) {
      console.error('Error updating job:', error);
      throw error;
    }
  },

  /**
   * Update job status (Employer only)
   * @param {number} jobId - Job ID
   * @param {string} status - New status (ACTIVE, CLOSED, HIDDEN)
   * @returns {Promise} Promise with updated job
   */
  updateJobStatus: async (jobId, status) => {
    try {
      const response = await axiosClient.patch(`/jobs/${jobId}/status`, null, {
        params: { status }
      });
      return response;
    } catch (error) {
      console.error('Error updating job status:', error);
      throw error;
    }
  },

  /**
   * Delete job (soft delete - changes status to HIDDEN)
   * @param {number} jobId - Job ID
   * @returns {Promise} Promise with void response
   */
  deleteJob: async (jobId) => {
    try {
      const response = await axiosClient.delete(`/jobs/${jobId}`);
      return response;
    } catch (error) {
      console.error('Error deleting job:', error);
      throw error;
    }
  },
};

export default jobService;
