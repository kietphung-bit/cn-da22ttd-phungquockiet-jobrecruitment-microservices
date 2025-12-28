import React from 'react';
import { Loader2 } from 'lucide-react';

/**
 * LoadingSpinner Component
 * A reusable loading spinner component for async operations and code splitting fallbacks
 * 
 * Props:
 * @param {string} size - Size variant: 'sm' | 'md' | 'lg' | 'xl' (default: 'md')
 * @param {string} text - Optional loading text to display below spinner
 * @param {boolean} fullScreen - If true, renders centered in full screen
 * @param {string} color - Color variant: 'primary' | 'white' | 'gray' (default: 'primary')
 */
const LoadingSpinner = ({ 
  size = 'md', 
  text = 'Đang tải...', 
  fullScreen = true,
  color = 'primary' 
}) => {
  // Size mapping
  const sizeMap = {
    sm: 'h-4 w-4',
    md: 'h-8 w-8',
    lg: 'h-12 w-12',
    xl: 'h-16 w-16'
  };

  // Color mapping
  const colorMap = {
    primary: 'text-primary-600',
    white: 'text-white',
    gray: 'text-gray-400'
  };

  const spinnerClasses = `${sizeMap[size]} ${colorMap[color]} animate-spin`;

  // Inline spinner (no full screen)
  if (!fullScreen) {
    return (
      <div className="flex items-center justify-center gap-2">
        <Loader2 className={spinnerClasses} />
        {text && <span className="text-sm text-gray-600">{text}</span>}
      </div>
    );
  }

  // Full screen centered spinner
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
