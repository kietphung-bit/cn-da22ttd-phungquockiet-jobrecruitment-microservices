import axiosClient from '../api/axiosClient';

/**
 * Authentication Service
 * Xử lý tất cả các cuộc gọi API liên quan đến xác thực và quản lý người dùng
 * 
 * Các chức năng chính:
 * - Đăng nhập (POST /api/v1/auth/login)
 * - Đăng ký nhà tuyển dụng (POST /api/v1/auth/register/employer)
 * - Đăng ký ứng viên (POST /api/v1/auth/register/candidate)
 */

const authService = {
  /**
   * Đăng nhập người dùng với tên đăng nhập và mật khẩu
   * @param {string} username - Email người dùng
   * @param {string} password - Mật khẩu người dùng
   * @returns {Promise} Phản hồi với token và dữ liệu người dùng
   */
  login: async (username, password) => {
    try {
      const response = await axiosClient.post('/auth/login', {
        username,
        password,
      });
      // Response là ApiResponse: { status, message, data }
      // Trả về response.data để lấy AuthResponse thực tế
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Đăng ký tài khoản nhà tuyển dụng mới
   * @param {Object} employerData - Dữ liệu đăng ký nhà tuyển dụng
   * @returns {Promise} Phản hồi với dữ liệu người dùng nhà tuyển dụng mới
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
      // Response là ApiResponse: { status, message, data }
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Đăng ký tài khoản ứng viên mới
   * @param {Object} candidateData - Dữ liệu đăng ký ứng viên
   * @returns {Promise} Phản hồi với dữ liệu người dùng ứng viên mới
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
      // Response là ApiResponse: { status, message, data }
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Đăng xuất người dùng hiện tại
   * Xóa dữ liệu trong local storage và trạng thái xác thực
   */
  logout: () => {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
    localStorage.removeItem('auth_redirect');
  },

  /**
   * Lấy người dùng hiện tại từ localStorage
   * @returns {Object|null} Đối tượng người dùng hoặc null
   */
  getCurrentUser: () => {
    const userStr = localStorage.getItem('auth_user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch (error) {
        return null;
      }
    }
    return null;
  },

  /**
   * Kiểm tra xem người dùng đã xác thực hay chưa
   * @returns {boolean} True nếu đã xác thực, ngược lại false
   */
  isAuthenticated: () => {
    const token = localStorage.getItem('auth_token');
    return !!token;
  },

  /**
   * Lấy token JWT đã lưu trữ
   * @returns {string|null} Token JWT hoặc null
   */
  getToken: () => {
    return localStorage.getItem('auth_token');
  },

  /**
   * Lưu dữ liệu xác thực trong localStorage
   * @param {string} token - Token JWT
   * @param {Object} user - Dữ liệu người dùng
   */
  setAuthData: (token, user) => {
    localStorage.setItem('auth_token', token);
    localStorage.setItem('auth_user', JSON.stringify(user));
  },

  /**
   * Thay đổi mật khẩu người dùng
   * @param {Object} passwordData - { oldPassword, newPassword, confirmPassword }
   * @returns {Promise} Phản hồi với thông báo thành công
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
   * Đăng xuất khỏi tất cả thiết bị (vô hiệu hóa tất cả token)
   * @returns {Promise} Phản hồi với thông báo thành công
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
