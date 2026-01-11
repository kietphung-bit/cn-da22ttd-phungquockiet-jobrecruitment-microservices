import axios from 'axios';

/**
 * Cấu hình Axios 
 * Base URL: http://localhost:5000/api/v1
 * Xử lý JWT token authentication và request/response interceptors
 */

// Tạo mới axios instance với cấu hình cơ bản
const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:5000/api/v1',
  timeout: 10000, // 10 giây
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Khai báo Interceptors
 * Tự động đính kèm JWT token vào mọi request nếu có trong localStorage
 */
axiosClient.interceptors.request.use(
  (config) => {
    // Lấy JWT token từ localStorage (AuthContext lưu nó dưới key 'auth_token')
    const token = localStorage.getItem('auth_token');
    
    // Nếu token tồn tại, thêm nó vào header Authorization
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // // Log request for debugging (remove in production)
    // if (import.meta.env.DEV) {
    //   console.log('🚀 API Request:', {
    //     method: config.method?.toUpperCase(),
    //     url: config.url,
    //     data: config.data,
    //   });
    // }
    
    return config;
  },
  (error) => {
    // Xử lý lỗi request
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);

/**
 * Response Interceptor
 * Xử lý định dạng response và xử lý lỗi toàn cục
 */
axiosClient.interceptors.response.use(
  (response) => {
    // // Log response for debugging (remove in production)
    // if (import.meta.env.DEV) {
    //   console.log('✅ API Response:', {
    //     status: response.status,
    //     data: response.data,
    //   });
    // }
    
    // Return the full response.data (ApiResponse wrapper)
    // Backend returns: { status, message, data }
    // Let services decide how to unwrap the data
    return response.data;
  },
  (error) => {
    // Xử lý lỗi response
    console.error('❌ Response Error:', error);
    
    // Xử lý các trường hợp lỗi cụ thể
    if (error.response) {
      const { status, data } = error.response;
      
      switch (status) {
        case 401:
          // Không được phép - Token hết hạn hoặc không hợp lệ
          console.error('🔒 Unauthorized: Token expired or invalid');
          
          // Xóa token và chuyển hướng đến trang đăng nhập (sử dụng key auth_token từ AuthContext)
          localStorage.removeItem('auth_token');
          localStorage.removeItem('auth_user');
          localStorage.removeItem('auth_redirect');
          
          // Chuyển hướng đến trang đăng nhập (nếu chưa ở đó)
          if (!window.location.pathname.includes('/login')) {
            window.location.href = '/login';
          }
          break;
          
        case 403:
          // Forbidden - Không có quyền truy cập
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
      
      // Trả về lỗi response theo định dạng chuẩn
      return Promise.reject({
        status: status,
        message: data?.message || 'An error occurred',
        data: data,
      });
    } else if (error.request) {
      // Request được gửi nhưng không nhận được phản hồi (Lỗi mạng, server không phản hồi, v.v.)
      console.error('🌐 Network Error: No response from server');
      return Promise.reject({
        status: 0,
        message: 'Lỗi mạng. Vui lòng kiểm tra kết nối của bạn.',
        data: null,
      });
    } else {
      // Có lỗi xảy ra trong quá trình thiết lập request
      console.error('⚙️ Request Setup Error:', error.message);
      return Promise.reject({
        status: 0,
        message: error.message || 'Lỗi thiết lập request',
        data: null,
      });
    }
  }
);

/**
 * Hàm để thiết lập token JWT trong localStorage và headers của axios
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
 * Hàm để xóa dữ liệu xác thực khỏi localStorage và headers của axios
 */
export const clearAuth = () => {
  localStorage.removeItem('auth_token');
  localStorage.removeItem('auth_user');
  localStorage.removeItem('auth_redirect');
  delete axiosClient.defaults.headers.common['Authorization'];
};

export default axiosClient;
