/**
 * Định dạng tiền tệ VND 
 * @param {number} amount - Số tiền cần định dạng
 * @returns {string} Chuỗi tiền tệ đã định dạng (ví dụ: "20.000.000 ₫")
 */
export const formatCurrency = (amount) => {
  if (!amount && amount === 0) return 'Thỏa thuận';
  
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount);
};

// Định dạng tiền tệ VND (để tương thích ngược)
export const formatVND = formatCurrency;

/**
 * Định dạng tiền tệ USD
 * @param {number} amount - Số tiền cần định dạng
 * @returns {string} Chuỗi tiền tệ đã định dạng (ví dụ: "$20,000")
 */
export const formatUSD = (amount) => {
  if (!amount && amount !== 0) return 'N/A';
  
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount);
};

/**
 * Định dạng khoảng lương bằng VND
 * @param {number} minSalary - Mức lương tối thiểu
 * @param {number} maxSalary - Mức lương tối đa (tùy chọn)
 * @returns {string} Chuỗi khoảng lương đã định dạng (ví dụ: "10.000.000 ₫ - 20.000.000 ₫")
 */
export const formatSalaryRange = (minSalary, maxSalary = null) => {
  if (!minSalary && minSalary !== 0) return 'Thỏa thuận';
  
  if (maxSalary) {
    return `${formatCurrency(minSalary)} - ${formatCurrency(maxSalary)}`;
  }
  
  return `Từ ${formatCurrency(minSalary)}`;
};

/**
 * Định dạng ngày theo định dạng tiếng Việt
 * @param {string|Date} dateString - Ngày cần định dạng (chuỗi ISO hoặc đối tượng Date)
 * @param {string} format - Loại định dạng ('short', 'long', 'relative')
 * @returns {string} Chuỗi ngày đã định dạng
 * 
 * Ví dụ:
 * - short: "23/12/2025"
 * - long: "23 tháng 12, 2025"
 * - relative: "2 ngày trước"
 */
export const formatDate = (dateString, format = 'short') => {
  if (!dateString) return 'Chưa cập nhật';
  
  const dateObj = typeof dateString === 'string' ? new Date(dateString) : dateString;
  
  // Kiểm tra xem ngày có hợp lệ không
  if (isNaN(dateObj.getTime())) return 'Ngày không hợp lệ';
  
  if (format === 'relative') {
    return formatRelativeDate(dateObj);
  }
  
  if (format === 'long') {
    return new Intl.DateTimeFormat('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    }).format(dateObj);
  }
  
  // Mặc định: định dạng ngắn DD/MM/YYYY
  return new Intl.DateTimeFormat('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(dateObj);
};

/**
 * Định dạng ngày tương đối so với hiện tại bằng tiếng Việt (ví dụ: "2 ngày trước")
 * @param {Date} date - Ngày cần định dạng
 * @returns {string} Chuỗi ngày tương đối bằng tiếng Việt
 */
export const formatRelativeDate = (date) => {
  const now = new Date();
  const diffMs = now - date;
  const diffSecs = Math.floor(diffMs / 1000);
  const diffMins = Math.floor(diffSecs / 60);
  const diffHours = Math.floor(diffMins / 60);
  const diffDays = Math.floor(diffHours / 24);
  const diffWeeks = Math.floor(diffDays / 7);
  const diffMonths = Math.floor(diffDays / 30);
  const diffYears = Math.floor(diffDays / 365);
  
  if (diffSecs < 60) return 'Vừa xong';
  if (diffMins < 60) return `${diffMins} phút trước`;
  if (diffHours < 24) return `${diffHours} giờ trước`;
  if (diffDays < 7) return `${diffDays} ngày trước`;
  if (diffWeeks < 4) return `${diffWeeks} tuần trước`;
  if (diffMonths < 12) return `${diffMonths} tháng trước`;
  return `${diffYears} năm trước`;
};

/**
 * Rút gọn văn bản với dấu ba chấm
 * @param {string} text - Văn bản cần rút gọn
 * @param {number} maxLength - Độ dài tối đa
 * @returns {string} Văn bản đã được rút gọn
 */
export const truncateText = (text, maxLength = 100) => {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

/**
 * Chuyển đổi trạng thái công việc từ API sang văn bản hiển thị bằng tiếng Việt
 * @param {string} status - Giá trị trạng thái từ API
 * @returns {string} Văn bản hiển thị bằng tiếng Việt
 */
export const mapJobStatus = (status) => {
  const statusMap = {
    PENDING: 'Chờ duyệt',
    WAIT: 'Chưa bắt đầu',
    ACTIVE: 'Đang hoạt động',
    CLOSED: 'Đã đóng',
    HIDDEN: 'Đã ẩn',
  };
  return statusMap[status] || status;
};

/**
 * Chuyển đổi trạng thái công việc từ API sang màu badge Tailwind
 * @param {string} status - Giá trị trạng thái từ API
 * @returns {string} Các lớp màu Tailwind
 */
export const getJobStatusColor = (status) => {
  const colorMap = {
    PENDING: 'bg-yellow-100 text-yellow-800',
    WAIT: 'bg-gray-100 text-gray-800',
    ACTIVE: 'bg-green-100 text-green-800',
    CLOSED: 'bg-red-100 text-red-800',
    HIDDEN: 'bg-gray-100 text-gray-800',
  };
  return colorMap[status] || 'bg-gray-100 text-gray-800';
};
