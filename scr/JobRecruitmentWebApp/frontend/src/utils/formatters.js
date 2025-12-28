/**
 * Format currency in VND (Vietnamese Dong)
 * @param {number} amount - Amount to format
 * @returns {string} Formatted currency string (e.g., "20.000.000 ₫")
 */
export const formatCurrency = (amount) => {
  if (!amount && amount === 0) return 'Thỏa thuận';
  
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount);
};

// Alias for backward compatibility
export const formatVND = formatCurrency;

/**
 * Format currency in USD
 * @param {number} amount - Amount to format
 * @returns {string} Formatted currency string
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
 * Format salary range in VND
 * @param {number} minSalary - Minimum salary
 * @param {number} maxSalary - Maximum salary (optional)
 * @returns {string} Formatted salary range (e.g., "10.000.000 ₫ - 20.000.000 ₫")
 */
export const formatSalaryRange = (minSalary, maxSalary = null) => {
  if (!minSalary && minSalary !== 0) return 'Thỏa thuận';
  
  if (maxSalary) {
    return `${formatCurrency(minSalary)} - ${formatCurrency(maxSalary)}`;
  }
  
  return `Từ ${formatCurrency(minSalary)}`;
};

/**
 * Format date to Vietnamese format
 * @param {string|Date} dateString - Date to format (ISO string or Date object)
 * @param {string} format - Format type ('short', 'long', 'relative')
 * @returns {string} Formatted date string
 * 
 * Examples:
 * - short: "23/12/2025"
 * - long: "23 tháng 12, 2025"
 * - relative: "2 ngày trước"
 */
export const formatDate = (dateString, format = 'short') => {
  if (!dateString) return 'Chưa cập nhật';
  
  const dateObj = typeof dateString === 'string' ? new Date(dateString) : dateString;
  
  // Check if date is valid
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
  
  // Default: short format DD/MM/YYYY
  return new Intl.DateTimeFormat('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(dateObj);
};

/**
 * Format date relative to now in Vietnamese (e.g., "2 ngày trước")
 * @param {Date} date - Date to format
 * @returns {string} Relative date string in Vietnamese
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
 * Truncate text with ellipsis
 * @param {string} text - Text to truncate
 * @param {number} maxLength - Maximum length
 * @returns {string} Truncated text
 */
export const truncateText = (text, maxLength = 100) => {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

/**
 * Map API job status to Vietnamese display text
 * @param {string} status - API status value
 * @returns {string} Display text in Vietnamese
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
 * Map API job status to badge color
 * @param {string} status - API status value
 * @returns {string} Tailwind color classes
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
