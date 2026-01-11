import React from 'react';
import { Loader2 } from 'lucide-react';

/**
 * LoadingSpinner Component
 * Một thành phần spinner tải lại có thể tái sử dụng cho các hoạt động bất đồng bộ và các fallback phân tách mã
 * 
 * Thuộc tính:
 * @param {string} size - Biến thể kích thước: 'sm' | 'md' | 'lg' | 'xl' (mặc định: 'md')
 * @param {string} text - Văn bản tải tùy chọn để hiển thị bên dưới spinner
 * @param {boolean} fullScreen - Nếu true, hiển thị ở giữa màn hình đầy đủ
 * @param {string} color - Biến thể màu sắc: 'primary' | 'white' | 'gray' (mặc định: 'primary')
 */
const LoadingSpinner = ({ 
  size = 'md', 
  text = 'Đang tải...', 
  fullScreen = true,
  color = 'primary' 
}) => {
  // Bản đồ kích thước
  const sizeMap = {
    sm: 'h-4 w-4',
    md: 'h-8 w-8',
    lg: 'h-12 w-12',
    xl: 'h-16 w-16'
  };

  // Bản đồ màu sắc
  const colorMap = {
    primary: 'text-primary-600',
    white: 'text-white',
    gray: 'text-gray-400'
  };

  const spinnerClasses = `${sizeMap[size]} ${colorMap[color]} animate-spin`;

  // Spinner nội tuyến (không toàn màn hình)
  if (!fullScreen) {
    return (
      <div className="flex items-center justify-center gap-2">
        <Loader2 className={spinnerClasses} />
        {text && <span className="text-sm text-gray-600">{text}</span>}
      </div>
    );
  }

  // Spinner toàn màn hình căn giữa
  return (
    <div className="fixed inset-0 flex items-center justify-center bg-white bg-opacity-80 z-50">
      <div className="flex flex-col items-center gap-4">
        <Loader2 className={spinnerClasses} />
        {text && (
          <p className="text-base font-medium text-gray-700 animate-pulse">
            {text}
          </p>
        )}
      </div>
    </div>
  );
};

export default LoadingSpinner;
