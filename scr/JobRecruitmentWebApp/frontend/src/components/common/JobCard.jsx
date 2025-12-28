import React, { useState, useEffect } from 'react';
import { MapPin, DollarSign, Heart } from 'lucide-react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import savedJobService from '../../services/savedJob.service';
import { useAuth } from '../../contexts/AuthContext';

/**
 * JobCard Component
 * Based on 'Hot Jobs' wireframe
 * Layout: Box with shadow/border
 * Top: Logo placeholder + Bookmark button
 * Middle: Job Title & Tags (Salary, Location)
 * Bottom: 'Apply' button (Solid Blue)
 * 
 * Features:
 * - Bookmark/Save job functionality
 * - Heart icon (outline = unsaved, solid red = saved)
 * - Toast notifications for save/unsave actions
 * 
 * @param {Object} props
 * @param {Object} props.job - Job object matching backend JobResponse DTO
 * - jobId: number
 * - title: string (mapped from jobTitle)
 * - company: string (mapped from companyName)
 * - companyLogo: string (mapped from companyLogo)
 * - salary: string (formatted from jobSalary)
 * - location: string (mapped from jobLocation)
 * @param {boolean} props.isSaved - Initial saved state (optional)
 * @param {Function} props.onSaveToggle - Callback when save state changes (optional)
 */
const JobCard = ({ job, isSaved: initialIsSaved = false, onSaveToggle }) => {
  // Utility function to construct full backend URL for uploaded files
  const getBackendFileUrl = (filePath) => {
    if (!filePath) return null;
    if (filePath.startsWith('http')) return filePath;
    const baseUrl = import.meta.env.VITE_API_URL?.replace('/api/v1', '') || 'http://localhost:8080';
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

  // Check if job is saved on mount (only for authenticated candidates)
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
   * Handle bookmark/save toggle
   */
  const handleSaveToggle = async (e) => {
    e.preventDefault(); // Prevent navigation
    e.stopPropagation();

    console.log('🔖 Save button clicked', { jobId, isAuthenticated, userRole: user?.role, user });

    // Check authentication
    if (!isAuthenticated) {
      console.log('❌ Not authenticated');
      toast.info('Vui lòng đăng nhập để lưu công việc', {
        position: 'top-right',
        autoClose: 3000,
      });
      return;
    }

    // Check role (only candidates can save jobs)
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
        // Unsave
        console.log('🗑️ Unsaving job:', jobId);
        const result = await savedJobService.unsaveJob(jobId);
        console.log('✅ Unsave result:', result);
        setIsSaved(false);
        toast.success('Đã bỏ lưu công việc', {
          position: 'top-right',
          autoClose: 2000,
        });
      } else {
        // Save
        console.log('💾 Saving job:', jobId);
        const result = await savedJobService.saveJob(jobId);
        console.log('✅ Save result:', result);
        setIsSaved(true);
        toast.success('Đã lưu công việc thành công', {
          position: 'top-right',
          autoClose: 2000,
        });
      }

      // Call parent callback if provided
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
      {/* Bookmark Button - Top Right - Always visible */}
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
          <DollarSign className="w-4 h-4 mr-2 text-primary" />
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