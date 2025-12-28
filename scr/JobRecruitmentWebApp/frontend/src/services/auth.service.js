import axiosClient from '../api/axiosClient';

/**
 * Authentication Service
 * Handles all authentication-related API calls
 */

const authService = {
  /**
   * Login user with username and password
   * @param {string} username - User's email
   * @param {string} password - User's password
   * @returns {Promise} Response with token and user data
   */
  login: async (username, password) => {
    try {
      const response = await axiosClient.post('/auth/login', {
        username,
        password,
      });
      // Response is ApiResponse: { status, message, data }
      // Return response.data to get the actual AuthResponse
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Register new employer account
   * @param {Object} employerData - Employer registration data
   * @returns {Promise} Response with new employer user data
   */
  registerEmployer: async (employerData) => {
    try {
      const response = await axiosClient.post('/auth/register/employer', {
        username: employerData.username,
        password: employerData.password,
        companyName: employerData.companyName,
        companyEmail: employerData.companyEmail,
        companyAddress: employerData.companyAddress,
        companyWebsite: employerData.companyWebsite || null,
        companyDescription: employerData.companyDescription || null,
      });
      // Response is ApiResponse: { status, message, data }
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Register new candidate account
   * @param {Object} candidateData - Candidate registration data
   * @returns {Promise} Response with new candidate user data
   */
  registerCandidate: async (candidateData) => {
    try {
      const response = await axiosClient.post('/auth/register/candidate', {
        username: candidateData.username,
        password: candidateData.password,
        candidateName: candidateData.candidateName,
        candidateEmail: candidateData.candidateEmail,
        candidatePhone: candidateData.candidatePhone,
        candidateBirthdate: candidateData.candidateBirthdate,
        candidateGender: candidateData.candidateGender,
      });
      // Response is ApiResponse: { status, message, data }
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Logout current user
   * Clears local storage and auth state
   */
  logout: () => {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
    localStorage.removeItem('auth_redirect');
  },

  /**
   * Get current user from localStorage
   * @returns {Object|null} User object or null
   */
  getCurrentUser: () => {
    const userStr = localStorage.getItem('auth_user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch (error) {
        console.error('Error parsing user data:', error);
        return null;
      }
    }
    return null;
  },

  /**
   * Check if user is authenticated
   * @returns {boolean} True if authenticated
   */
  isAuthenticated: () => {
    const token = localStorage.getItem('auth_token');
    return !!token;
  },

  /**
   * Get stored JWT token
   * @returns {string|null} JWT token or null
   */
  getToken: () => {
    return localStorage.getItem('auth_token');
  },

  /**
   * Set authentication data in localStorage
   * @param {string} token - JWT token
   * @param {Object} user - User data
   */
  setAuthData: (token, user) => {
    localStorage.setItem('auth_token', token);
    localStorage.setItem('auth_user', JSON.stringify(user));
  },

  /**
   * Change user password
   * @param {Object} passwordData - { oldPassword, newPassword, confirmPassword }
   * @returns {Promise} Response with success message
   */
  changePassword: async (passwordData) => {
    try {
      const response = await axiosClient.patch('/auth/change-password', passwordData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Logout from all devices (invalidate all tokens)
   * @returns {Promise} Response with success message
   */
  logoutAllSessions: async () => {
    try {
      const response = await axiosClient.post('/auth/logout-all');
      return response.data;
    } catch (error) {
      throw error;
    }
  },
};

export default authService;
