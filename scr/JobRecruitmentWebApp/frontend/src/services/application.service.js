/**
 * Application Service
 * 
 * API integration for job application management
 * Handles:
 * - Submit application (POST /api/v1/applications)
 * - Get candidate's applications (GET /api/v1/applications/me)
 * - Get applications for a job (GET /api/v1/applications/job/{jobId})
 */

import axiosClient from '../api/axiosClient';

const applicationService = {
  /**
   * Submit a job application
   * Endpoint: POST /api/v1/applications
   * Request: { jobId: Long, cvId: Long }
   * Response: ApplicationResponse
   * 
   * Business Rules:
   * - Job must be ACTIVE and within posting period
   * - CV must belong to candidate and be ACTIVE
   * - No duplicate applications
   * 
   * @param {Object} data - Application data
   * @param {number} data.jobId - Job ID to apply
   * @param {number} data.cvId - CV ID to use
   * @returns {Promise<Object>} Application response
   */
  async applyToJob(data) {
    try {
      console.log('ApplicationService - Submitting application:', data);
      const response = await axiosClient.post('/applications', data);
      console.log('ApplicationService - Application submitted:', response.data);
      
      // Handle ApiResponse wrapper
      if (response.data && response.data.data) {
        return response.data.data;
      }
      
      return response.data;
    } catch (error) {
      console.error('ApplicationService - Apply failed:', error.response?.data || error);
      throw error;
    }
  },

  /**
   * Get all applications submitted by current candidate
   * Endpoint: GET /api/v1/applications/me
   * Response: Page<ApplicationResponse>
   * 
   * @param {Object} params - Query parameters
   * @param {number} params.page - Page number (0-indexed)
   * @param {number} params.size - Page size
   * @param {string} params.status - Filter by status (PENDING, APPROVED, REJECTED)
   * @returns {Promise<Object>} Paginated applications
   */
  async getMyApplications(params = {}) {
    try {
      console.log('ApplicationService - Fetching my applications:', params);
      const response = await axiosClient.get('/applications/me', { params });
      console.log('ApplicationService - My applications:', response.data);
      
      // Handle ApiResponse wrapper
      if (response.data && response.data.data) {
        return response.data.data;
      }
      
      return response.data;
    } catch (error) {
      console.error('ApplicationService - Fetch failed:', error.response?.data || error);
      throw error;
    }
  },

  /**
   * Check if candidate has already applied to a job
   * Endpoint: GET /api/v1/applications/me
   * Filter locally by jobId
   * 
   * @param {number} jobId - Job ID to check
   * @returns {Promise<boolean>} True if already applied
   */
  async hasApplied(jobId) {
    try {
      const jobIdNum = typeof jobId === 'string' ? parseInt(jobId, 10) : jobId;
      console.log('ApplicationService - Checking if applied:', { jobId, jobIdNum });
      
      const applications = await this.getMyApplications({ size: 1000 });
      
      // Handle different response structures
      let applicationsList = [];
      if (Array.isArray(applications)) {
        applicationsList = applications;
      } else if (applications.content && Array.isArray(applications.content)) {
        applicationsList = applications.content;
      }
      
      const applied = applicationsList.some(app => {
        const appJobId = app.jobId || app.job?.jobId;
        return appJobId === jobIdNum || appJobId === jobId;
      });
      
      console.log('ApplicationService - Has applied result:', applied);
      return applied;
    } catch (error) {
      console.error('ApplicationService - Check failed:', error);
      return false;
    }
  },

  /**
   * Get all applications for a specific job (Employer view)
   * Endpoint: GET /api/v1/applications/job/{jobId}
   * Response: Page<ApplicationResponse>
   * 
   * @param {number} jobId - Job ID
   * @param {Object} params - Query parameters
   * @param {number} params.page - Page number (0-indexed)
   * @param {number} params.size - Page size
   * @param {string} params.status - Filter by status
   * @returns {Promise<Object>} Paginated applications
   */
  async getApplicationsByJob(jobId, params = {}) {
    try {
      console.log('ApplicationService - Fetching applications for job:', { jobId, params });
      const response = await axiosClient.get(`/applications/job/${jobId}`, { params });
      console.log('ApplicationService - Job applications:', response.data);
      
      // Handle ApiResponse wrapper
      if (response.data && response.data.data) {
        return response.data.data;
      }
      
      return response.data;
    } catch (error) {
      console.error('ApplicationService - Fetch failed:', error.response?.data || error);
      throw error;
    }
  },

  /**
   * Update application status (Employer action)
   * Endpoint: PATCH /api/v1/applications/{id}/status
   * Request Params: status (PENDING, APPROVED, REJECTED)
   * 
   * @param {number} applicationId - Application ID
   * @param {string} status - New status (PENDING, APPROVED, REJECTED)
   * @returns {Promise<Object>} Updated application
   */
  async updateApplicationStatus(applicationId, status) {
    try {
      console.log('ApplicationService - Updating status:', { applicationId, status });
      const response = await axiosClient.patch(
        `/applications/${applicationId}/status`,
        null,
        { params: { status } }
      );
      console.log('ApplicationService - Status updated:', response.data);
      
      // Handle ApiResponse wrapper
      if (response.data && response.data.data) {
        return response.data.data;
      }
      
      return response.data;
    } catch (error) {
      console.error('ApplicationService - Update failed:', error.response?.data || error);
      throw error;
    }
  },

  /**
   * Withdraw/Cancel application (Candidate action)
   * Endpoint: DELETE /api/v1/applications/{id}
   * 
   * @param {number} applicationId - Application ID to withdraw
   * @returns {Promise<void>} No content on success
   */
  async withdrawApplication(applicationId) {
    try {
      console.log('ApplicationService - Withdrawing application:', applicationId);
      const response = await axiosClient.delete(`/applications/${applicationId}`);
      console.log('ApplicationService - Application withdrawn successfully');
      return response.data;
    } catch (error) {
      console.error('ApplicationService - Withdraw failed:', error.response?.data || error);
      throw error;
    }
  },

  /**
   * Get all applications for employer's jobs (Employer view)
   * Endpoint: GET /api/v1/applications/company
   * Response: Page<ApplicationResponse>
   * 
   * @param {Object} params - Query parameters
   * @param {number} params.page - Page number (0-indexed)
   * @param {number} params.size - Page size
   * @param {number} params.jobId - Filter by specific job ID
   * @param {string} params.status - Filter by status (PENDING, APPROVED, REJECTED)
   * @returns {Promise<Object>} Paginated applications
   */
  async getCompanyApplications(params = {}) {
    try {
      console.log('ApplicationService - Fetching company applications:', params);
      const response = await axiosClient.get('/applications/company', { params });
      console.log('ApplicationService - Company applications:', response.data);
      
      // Handle ApiResponse wrapper
      if (response.data && response.data.data) {
        return response.data.data;
      }
      
      return response.data;
    } catch (error) {
      console.error('ApplicationService - Fetch failed:', error.response?.data || error);
      throw error;
    }
  }
};

export default applicationService;
