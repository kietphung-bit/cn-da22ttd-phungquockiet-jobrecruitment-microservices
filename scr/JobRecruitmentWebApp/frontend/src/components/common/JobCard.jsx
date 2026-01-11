import React, { useState, useEffect } from 'react';
import { MapPin, Banknote, Heart } from 'lucide-react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import savedJobService from '../../services/savedJob.service';
import { useAuth } from '../../contexts/AuthContext';

/**
 * JobCard Component
 * Layout: Hộp vuông với shadow/border
 * Top: Logo placeholder + Bookmark button
 * Middle: Job Title & Tags (Salary, Location)
 * Bottom: 'Ứng tuyển' button (Solid Blue)
 * 
 * Tính năng:
 * - Chức năng đánh dấu/Lưu công việc
 * - Biểu tượng trái tim (viền = chưa lưu, đỏ đặc = đã lưu)
 * - Thông báo toast cho các hành động lưu/bỏ lưu
 * 
 * @param {Object} props
 * @param {Object} props.job - Đối tượng công việc phù hợp với backend JobResponse DTO
 * - jobId: number
 * - title: string (chuyển từ jobTitle)
 * - company: string (chuyển từ companyName)
 * - companyLogo: string (chuyển từ companyLogo)
 * - salary: string (định dạng từ jobSalary)
 * - location: string (chuyển từ jobLocation)
 * @param {boolean} props.isSaved - Trạng thái lưu ban đầu (tùy chọn)
 * @param {Function} props.onSaveToggle - Callback khi trạng thái lưu thay đổi (tùy chọn)
 */
const JobCard = ({ job, isSaved: initialIsSaved = false, onSaveToggle }) => {
  // Hàm để tạo URL đầy đủ cho file tải lên từ backend
  const getBackendFileUrl = (filePath) => {
    if (!filePath) return null;
    if (filePath.startsWith('http')) return filePath;
    const baseUrl = import.meta.env.VITE_API_URL?.replace('/api/v1', '') || 'http://localhost:5000';
    return `${baseUrl}${filePath}`;
  };

  const {
    jobId = 1,
    title = 'Tên công việc',
    company = 'Tên công ty',
    logoURL = null,
    salary = 'Thỏa thuận',
    location = 'Địa điểm',
  } = job || {};

  const { isAuthenticated, user } = useAuth();
  const [isSaved, setIsSaved] = useState(initialIsSaved);
  const [isSaving, setIsSaving] = useState(false);

  // Kiểm tra xem công việc đã được lưu hay chưa khi component được mount (chỉ dành cho ứng viên đã xác thực)
  useEffect(() => {
    const checkSavedStatus = async () => {
      if (isAuthenticated && user?.role === 'UV' && jobId) {
        try {
          const saved = await savedJobService.checkIsSaved(jobId);
          setIsSaved(saved);
        } catch (error) {
          console.error('Error checking saved status:', error);
        }
      }
    };

    checkSavedStatus();
  }, [isAuthenticated, user, jobId]);

  /**
   * Xử lý toggle đánh dấu/lưu công việc
   */
  const handleSaveToggle = async (e) => {
    e.preventDefault(); // Ngăn chặn điều hướng
    e.stopPropagation();

    //console.log('🔖 Save button clicked', { jobId, isAuthenticated, userRole: user?.role, user });

    // Kiểm tra xác thực
    if (!isAuthenticated) {
      console.log('❌ Not authenticated');
      toast.info('Vui lòng đăng nhập để lưu công việc', {
        position: 'top-right',
        autoClose: 3000,
      });
      return;
    }

    // Kiểm tra vai trò (chỉ ứng viên mới có thể lưu công việc)
    if (user?.role !== 'UV') {
      console.log('❌ Wrong role:', user?.role);
      toast.warning('Chỉ ứng viên mới có thể lưu công việc', {
        position: 'top-right',
        autoClose: 3000,
      });
      return;
    }

    try {
      setIsSaving(true);

      if (isSaved) {
        // Bỏ lưu
        //console.log('🗑️ Bỏ lưu công việc:', jobId);
        const result = await savedJobService.unsaveJob(jobId);
        //console.log('✅ Kết quả bỏ lưu:', result);
        setIsSaved(false);
        toast.success('Đã bỏ lưu công việc', {
          position: 'top-right',
          autoClose: 2000,
        });
      } else {
        // Lưu
        //console.log('💾 Lưu công việc:', jobId);
        const result = await savedJobService.saveJob(jobId);
        //console.log('✅ Kết quả lưu:', result);
        setIsSaved(true);
        toast.success('Đã lưu công việc thành công', {
          position: 'top-right',
          autoClose: 2000,
        });
      }

      // Gọi callback của hàm trên nếu được cung cấp
      if (onSaveToggle) {
        onSaveToggle(jobId, !isSaved);
      }
    } catch (error) {
      console.error('Error toggling save:', error);
      
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          (isSaved ? 'Không thể bỏ lưu công việc' : 'Không thể lưu công việc');
      
      toast.error(errorMessage, {
        position: 'top-right',
        autoClose: 3000,
      });
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="card p-6 hover:shadow-xl transition-all duration-300 relative">
      {/* Nút đánh dấu/lưu - Góc trên bên phải - Luôn hiển thị */}
      <button
        onClick={handleSaveToggle}
        disabled={isSaving}
        className="absolute top-4 right-4 p-2 rounded-full hover:bg-gray-100 transition-colors
                 disabled:opacity-50 disabled:cursor-not-allowed z-10"
        title={isSaved ? 'Bỏ lưu' : 'Lưu công việc'}
      >
        <Heart
          className={`w-5 h-5 transition-all ${
            isSaved 
              ? 'fill-red-500 text-red-500' 
              : 'text-gray-400 hover:text-red-500'
          }`}
        />
      </button>

      {/* Top: Company Logo */}
      <div className="flex items-center mb-4">
        <div className="w-16 h-16 bg-neutral-200 rounded-lg flex items-center justify-center overflow-hidden flex-shrink-0">
          {logoURL ? (
            <img
              src={getBackendFileUrl(logoURL)}
              alt={company}
              className="w-full h-full object-cover"
            />
          ) : (
            <span className="text-neutral-400 font-bold text-xl">
              {company.charAt(0)}
            </span>
          )}
        </div>
        <div className="ml-4 flex-1 min-w-0">
          <h3 className="text-lg font-semibold text-neutral-900 truncate">
            {title}
          </h3>
          <p className="text-sm text-neutral-600 truncate">{company}</p>
        </div>
      </div>

      {/* Middle: Tags (Salary, Location) */}
      <div className="space-y-2 mb-4">
        <div className="flex items-center text-neutral-700">
          <Banknote className="w-4 h-4 mr-2 text-primary" />
          <span className="text-sm font-medium">{salary}</span>
        </div>
        <div className="flex items-center text-neutral-700">
          <MapPin className="w-4 h-4 mr-2 text-primary" />
          <span className="text-sm">{location}</span>
        </div>
      </div>

      {/* Bottom: Apply Button */}
      <Link
        to={`/jobs/${jobId}`}
        className="block w-full px-6 py-2 bg-primary text-white rounded-lg font-medium hover:bg-primary-600 transition-all duration-200 text-center"
      >
        Ứng tuyển ngay
      </Link>
    </div>
  );
};

export default JobCard;