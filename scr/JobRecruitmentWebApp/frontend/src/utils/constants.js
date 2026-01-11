/**
 * Application Constants
 * Định vị trung tâm cho tất cả các giá trị hằng số được sử dụng trong toàn bộ ứng dụng
 */

// API Configuration
export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:5000/api/v1';
export const API_TIMEOUT = 10000; // 10 seconds

// Role Codes (phải trùng khớp backend)
export const ROLES = {
  ADMIN: 'ADM',
  EMPLOYER: 'DN',
  CANDIDATE: 'UV',
};

// Role Names
export const ROLE_NAMES = {
  ADM: 'Quản trị viên',
  DN: 'Nhà tuyển dụng',
  UV: 'Ứng viên',
};

// Trang thái công việc
export const JOB_STATUS = {
  PENDING: 'PENDING',
  WAIT: 'WAIT',
  ACTIVE: 'ACTIVE',
  CLOSED: 'CLOSED',
  HIDDEN: 'HIDDEN',
};

// Nhãn trạng thái công việc
export const JOB_STATUS_LABELS = {
  PENDING: 'Chờ xét duyệt',
  WAIT: 'Chưa mở',
  ACTIVE: 'Đang mở',
  CLOSED: 'Đã đóng',
  HIDDEN: 'Tạm ẩn',
};

// Trang thái công ty
export const COMPANY_STATUS = {
  PENDING: 'PENDING',
  ACTIVE: 'ACTIVE',
  BLOCKED: 'BLOCKED',
};

// Nhãn trạng thái công ty
export const COMPANY_STATUS_LABELS = {
  PENDING: 'Chờ xét duyệt',
  ACTIVE: 'Đang hoạt động',
  BLOCKED: 'Bị khóa',
};

// Trang thái ứng dụng
export const APPLICATION_STATUS = {
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
};

// Nhãn trạng thái ứng dụng
export const APPLICATION_STATUS_LABELS = {
  PENDING: 'Đang chờ',
  APPROVED: 'Đã duyệt',
  REJECTED: 'Đã từ chối',
};

// Trang thái CV
export const CV_STATUS = {
  ACTIVE: 'ACTIVE',
  HIDDEN: 'HIDDEN',
};

// Nhãn trạng thái CV
export const CV_STATUS_LABELS = {
  ACTIVE: 'Đang hoạt động',
  HIDDEN: 'Tạm ẩn',
};

// Giới tính
export const GENDER = {
  MALE: 'MALE',
  FEMALE: 'FEMALE',
  OTHER: 'OTHER',
};

// Nhãn giới tính
export const GENDER_LABELS = {
  MALE: 'Nam',
  FEMALE: 'Nữ',
  OTHER: 'Khác',
};

// Phân trang
export const DEFAULT_PAGE_SIZE = 10;
export const PAGE_SIZE_OPTIONS = [5, 10, 20, 50];

// Tải lên tập tin
export const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
export const ALLOWED_IMAGE_EXTENSIONS = ['jpg', 'jpeg', 'png', 'gif'];
export const ALLOWED_DOCUMENT_EXTENSIONS = ['pdf', 'docx', 'doc'];

// Định dạng ngày
export const DATE_FORMAT = 'dd/MM/yyyy';
export const DATE_TIME_FORMAT = 'dd/MM/yyyy HH:mm';
export const API_DATE_FORMAT = 'yyyy-MM-dd';

// Khoảng lương (VND)
export const SALARY_RANGES = [
  { label: 'Dưới 10 triệu', min: 0, max: 10000000 },
  { label: '10 - 20 triệu', min: 10000000, max: 20000000 },
  { label: '20 - 30 triệu', min: 20000000, max: 30000000 },
  { label: '30 - 50 triệu', min: 30000000, max: 50000000 },
  { label: 'Trên 50 triệu', min: 50000000, max: null },
];

// Levels of Experience
export const EXPERIENCE_LEVELS = [
  { label: 'Chưa có kinh nghiệm', value: '0' },
  { label: 'Dưới 1 năm', value: '0-1' },
  { label: '1-3 năm', value: '1-3' },
  { label: '3-5 năm', value: '3-5' },
  { label: 'Trên 5 năm', value: '5+' },
];

// Job Types
export const JOB_TYPES = [
  { label: 'Full-time', value: 'full-time' },
  { label: 'Part-time', value: 'part-time' },
  { label: 'Remote', value: 'remote' },
  { label: 'Hợp đồng', value: 'contract' },
  { label: 'Thực tập', value: 'internship' },
];

// Vietnam Locations
export const LOCATIONS = [
  'Hà Nội',
  'TP. Hồ Chí Minh',
  'Đà Nẵng',
  'Hải Phòng',
  'Cần Thơ',
  'Biên Hòa',
  'Nha Trang',
  'Huế',
  'Buôn Ma Thuột',
  'Quy Nhơn',
];

// Local Storage Keys
export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'accessToken',
  REFRESH_TOKEN: 'refreshToken',
  USER_INFO: 'userInfo',
  THEME: 'theme',
  LANGUAGE: 'language',
};

// Toast Notification Config
export const TOAST_CONFIG = {
  position: 'top-right',
  autoClose: 3000,
  hideProgressBar: false,
  closeOnClick: true,
  pauseOnHover: true,
  draggable: true,
};

// Validation Rules
export const VALIDATION = {
  MIN_PASSWORD_LENGTH: 8,
  MAX_PASSWORD_LENGTH: 32,
  MIN_NAME_LENGTH: 2,
  MAX_NAME_LENGTH: 100,
  PHONE_REGEX: /^\d{10,11}$/,
  EMAIL_REGEX: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  NAME_REGEX: /^[a-zA-Z\s\p{L}]+$/u,
};

export default {
  API_BASE_URL,
  API_TIMEOUT,
  ROLES,
  ROLE_NAMES,
  JOB_STATUS,
  JOB_STATUS_LABELS,
  COMPANY_STATUS,
  COMPANY_STATUS_LABELS,
  APPLICATION_STATUS,
  APPLICATION_STATUS_LABELS,
  CV_STATUS,
  CV_STATUS_LABELS,
  GENDER,
  GENDER_LABELS,
  DEFAULT_PAGE_SIZE,
  PAGE_SIZE_OPTIONS,
  MAX_FILE_SIZE,
  ALLOWED_IMAGE_EXTENSIONS,
  ALLOWED_DOCUMENT_EXTENSIONS,
  DATE_FORMAT,
  DATE_TIME_FORMAT,
  API_DATE_FORMAT,
  SALARY_RANGES,
  EXPERIENCE_LEVELS,
  JOB_TYPES,
  LOCATIONS,
  STORAGE_KEYS,
  TOAST_CONFIG,
  VALIDATION,
};
