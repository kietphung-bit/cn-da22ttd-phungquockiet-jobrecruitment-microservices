import axios from 'axios';

/**
 * Axios Client Configuration
 * Base URL: http://localhost:5000/api/v1
 * Handles JWT token authentication and request/response interceptors
 */

// Create axios instance with base configuration
const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1',
  timeout: 10000, // 10 seconds
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request Interceptor
 * Automatically attaches JWT token to every request if available in localStorage
 */
axiosClient.interceptors.request.use(
  (config) => {
    // Get JWT token from localStorage (AuthContext stores it as 'auth_token')
    const token = localStorage.getItem('auth_token');
    
    // If token exists, add it to Authorization header
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // Log request for debugging (remove in production)
    if (import.meta.env.DEV) {
      console.log('🚀 API Request:', {
        method: config.method?.toUpperCase(),
        url: config.url,
        data: config.data,
      });
    }
    
    return config;
  },
  (error) => {
    // Handle request error
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);

/**
 * Response Interceptor
 * Handles response formatting and global error handling
 */
axiosClient.interceptors.response.use(
  (response) => {
    // Log response for debugging (remove in production)
    if (import.meta.env.DEV) {
      console.log('✅ API Response:', {
        status: response.status,
        data: response.data,
      });
    }
    
    // Return the full response.data (ApiResponse wrapper)
    // Backend returns: { status, message, data }
    // Let services decide how to unwrap the data
    return response.data;
  },
  (error) => {
    // Handle response error
    console.error('❌ Response Error:', error);
    
    // Handle specific error cases
    if (error.response) {
      const { status, data } = error.response;
      
      switch (status) {
        case 401:
          // Unauthorized - Token expired or invalid
          console.error('🔒 Unauthorized: Token expired or invalid');
          
          // Clear token and redirect to login (use auth_token key from AuthContext)
          localStorage.removeItem('auth_token');
          localStorage.removeItem('auth_user');
          localStorage.removeItem('auth_redirect');
          
          // Redirect to login page (if not already there)
          if (!window.location.pathname.includes('/login')) {
            window.location.href = '/login';
          }
          break;
          
        case 403:
          // Forbidden - No permission
          console.error('🚫 Forbidden: You do not have permission to access this resource');
          break;
          
        case 404:
          // Not Found
          console.error('🔍 Not Found: Resource not found');
          break;
          
        case 500:
          // Internal Server Error
          console.error('💥 Server Error: Something went wrong on the server');
          break;
          
        default:
          console.error(`⚠️ Error ${status}:`, data?.message || 'Unknown error');
      }
      
      // Return error response in a standardized format
      return Promise.reject({
        status: status,
        message: data?.message || 'An error occurred',
        data: data,
      });
    } else if (error.request) {
      // Request was made but no response received (Network error)
      console.error('🌐 Network Error: No response from server');
      return Promise.reject({
        status: 0,
        message: 'Network error. Please check your connection.',
        data: null,
      });
    } else {
      // Something happened in setting up the request
      console.error('⚙️ Request Setup Error:', error.message);
      return Promise.reject({
        status: 0,
        message: error.message || 'Request setup error',
        data: null,
      });
    }
  }
);

/**
 * Helper function to set JWT token in localStorage and axios headers
 * @param {string} token - JWT access token
 */
export const setAuthToken = (token) => {
  if (token) {
    localStorage.setItem('auth_token', token);
    axiosClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  } else {
    localStorage.removeItem('auth_token');
    delete axiosClient.defaults.headers.common['Authorization'];
  }
};

/**
 * Helper function to clear authentication data
 */
export const clearAuth = () => {
  localStorage.removeItem('auth_token');
  localStorage.removeItem('auth_user');
  localStorage.removeItem('auth_redirect');
  delete axiosClient.defaults.headers.common['Authorization'];
};

export default axiosClient;
