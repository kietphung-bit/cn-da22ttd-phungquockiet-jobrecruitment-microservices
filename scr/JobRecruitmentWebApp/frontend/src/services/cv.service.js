import axiosClient from '../api/axiosClient';

/**
 * CV Service
 * Handles all CV-related API calls for candidates
 * 
 * Endpoints:
 * - POST /api/v1/cvs - Upload CV file
 * - GET /api/v1/cvs/me - Get my CVs
 * - PATCH /api/v1/cvs/{id}/status - Update CV status
 * - DELETE /api/v1/cvs/{id} - Delete CV (soft delete)
 */
const cvService = {
  /**
   * Upload CV file
   * @param {File} file - PDF file object from <input type="file">
   * @returns {Promise} Promise with CV response data
   */
  uploadCV: async (file) => {
    try {
      // Create FormData for multipart/form-data upload
      const formData = new FormData();
      formData.append('file', file); // Key 'file' must match Backend @RequestParam
      
      const response = await axiosClient.post('/cvs', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      
      return response;
    } catch (error) {
      console.error('Error uploading CV:', error);
      throw error;
    }
  },

  /**
   * Get all CVs of authenticated candidate
   * @returns {Promise} Promise with list of CV data
   */
  getMyCVs: async () => {
    try {
      const response = await axiosClient.get('/cvs/me');
      return response;
    } catch (error) {
      console.error('Error fetching CVs:', error);
      throw error;
    }
  },

  /**
   * Update CV status (ACTIVE/HIDDEN)
   * @param {number} cvId - CV ID
   * @param {string} status - New status: 'ACTIVE' or 'HIDDEN'
   * @returns {Promise} Promise with updated CV data
   */
  updateCVStatus: async (cvId, status) => {
    try {
      // Backend expects @RequestParam CVStatus newStatus
      const response = await axiosClient.patch(`/cvs/${cvId}/status`, null, {
        params: {
          newStatus: status, // Key 'newStatus' matches Backend @RequestParam name
        },
      });
      
      return response;
    } catch (error) {
      console.error('Error updating CV status:', error);
      throw error;
    }
  },

  /**
   * Delete CV (soft delete - sets status to HIDDEN)
   * @param {number} cvId - CV ID
   * @returns {Promise} Promise with success message
   */
  deleteCV: async (cvId) => {
    try {
      const response = await axiosClient.delete(`/cvs/${cvId}`);
      return response;
    } catch (error) {
      console.error('Error deleting CV:', error);
      throw error;
    }
  },
};

export default cvService;
