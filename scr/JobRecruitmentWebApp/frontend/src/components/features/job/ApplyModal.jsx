import { useState, useEffect } from 'react';
import { X, FileText, Upload, AlertCircle, Loader2 } from 'lucide-react';
import cvService from '../../../services/cv.service';
import applicationService from '../../../services/application.service';
import { toast } from 'react-toastify';

/**
 * ApplyModal Component
 * 
 * Modal for candidates to apply to a job using one of their uploaded CVs
 * 
 * Features:
 * - Fetch and display candidate's CVs
 * - Select CV from dropdown/radio list
 * - Upload new CV link (opens upload page)
 * - Optional cover letter text area
 * - Submit application
 * 
 * Props:
 * - isOpen: Boolean to show/hide modal
 * - onClose: Function to close modal
 * - jobId: ID of the job to apply
 * - jobTitle: Title of the job (for display)
 * - onSuccess: Callback after successful application
 */
const ApplyModal = ({ isOpen, onClose, jobId, jobTitle, onSuccess }) => {
  const [cvList, setCvList] = useState([]);
  const [selectedCvId, setSelectedCvId] = useState(null);
  const [coverLetter, setCoverLetter] = useState('');
  const [loading, setLoading] = useState(false);
  const [fetchingCvs, setFetchingCvs] = useState(false);

  // Fetch candidate's CVs when modal opens
  useEffect(() => {
    if (isOpen) {
      fetchCvs();
      // Reset form
      setSelectedCvId(null);
      setCoverLetter('');
    }
  }, [isOpen]);

  const fetchCvs = async () => {
    try {
      setFetchingCvs(true);
      console.log('ApplyModal - Fetching CVs');
      const response = await cvService.getMyCVs({ size: 100, status: 'ACTIVE' });
      
      // Handle different response structures
      let cvs = [];
      if (Array.isArray(response)) {
        cvs = response;
      } else if (response.content && Array.isArray(response.content)) {
        cvs = response.content;
      } else if (response.data) {
        if (Array.isArray(response.data)) {
          cvs = response.data;
        } else if (response.data.content && Array.isArray(response.data.content)) {
          cvs = response.data.content;
        }
      }
      
      console.log('ApplyModal - CVs fetched:', cvs);
      setCvList(cvs);
      
      // Auto-select first CV if available
      if (cvs.length > 0) {
        setSelectedCvId(cvs[0].cvId);
      }
    } catch (error) {
      console.error('ApplyModal - Failed to fetch CVs:', error);
      toast.error('Không thể tải danh sách CV');
    } finally {
      setFetchingCvs(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!selectedCvId) {
      toast.warning('Vui lòng chọn CV để ứng tuyển');
      return;
    }

    try {
      setLoading(true);
      console.log('ApplyModal - Submitting application:', { jobId, selectedCvId });
      
      await applicationService.applyToJob({
        jobId: parseInt(jobId, 10),
        cvId: selectedCvId
      });
      
      toast.success('Nộp hồ sơ thành công!');
      
      // Call success callback
      if (onSuccess) {
        onSuccess();
      }
      
      // Close modal
      onClose();
    } catch (error) {
      console.error('ApplyModal - Application failed:', error);
      
      // Handle specific error messages from backend
      const errorMsg = error.response?.data?.message || error.message || 'Nộp hồ sơ thất bại';
      
      if (errorMsg.includes('already applied') || errorMsg.includes('đã ứng tuyển')) {
        toast.error('Bạn đã ứng tuyển vào công việc này rồi');
      } else if (errorMsg.includes('expired') || errorMsg.includes('hết hạn')) {
        toast.error('Tin tuyển dụng đã hết hạn');
      } else if (errorMsg.includes('inactive') || errorMsg.includes('không hoạt động')) {
        toast.error('Tin tuyển dụng không còn hoạt động');
      } else {
        toast.error(errorMsg);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleUploadNew = () => {
    // Close modal and navigate to CV upload page
    onClose();
    window.location.href = '/candidate/cv-upload';
  };

  if (!isOpen) return null;

  return (
    <>
      {/* Backdrop */}
      <div 
        className="fixed inset-0 bg-black bg-opacity-50 z-40"
        onClick={onClose}
      />
      
      {/* Modal */}
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
          {/* Header */}
          <div className="flex items-center justify-between p-6 border-b">
            <div>
              <h2 className="text-2xl font-bold text-gray-900">Ứng tuyển vào vị trí</h2>
              <p className="text-sm text-gray-600 mt-1">{jobTitle}</p>
            </div>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors"
              disabled={loading}
            >
              <X size={24} />
            </button>
          </div>

          {/* Body */}
          <form onSubmit={handleSubmit} className="p-6">
            {/* CV Selection */}
            <div className="mb-6">
              <label className="block text-sm font-semibold text-gray-700 mb-3">
                Chọn CV <span className="text-red-500">*</span>
              </label>
              
              {fetchingCvs ? (
                <div className="flex items-center justify-center py-8">
                  <Loader2 className="animate-spin text-indigo-600" size={32} />
                  <span className="ml-3 text-gray-600">Đang tải CV...</span>
                </div>
              ) : cvList.length === 0 ? (
                <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center">
                  <AlertCircle className="mx-auto text-gray-400 mb-3" size={48} />
                  <p className="text-gray-600 mb-4">Bạn chưa có CV nào</p>
                  <button
                    type="button"
                    onClick={handleUploadNew}
                    className="inline-flex items-center px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors"
                  >
                    <Upload size={18} className="mr-2" />
                    Tải CV lên ngay
                  </button>
                </div>
              ) : (
                <div className="space-y-3">
                  {cvList.map((cv) => (
                    <label
                      key={cv.cvId}
                      className={`flex items-start p-4 border-2 rounded-lg cursor-pointer transition-colors ${
                        selectedCvId === cv.cvId
                          ? 'border-indigo-600 bg-indigo-50'
                          : 'border-gray-200 hover:border-indigo-300'
                      }`}
                    >
                      <input
                        type="radio"
                        name="cv"
                        value={cv.cvId}
                        checked={selectedCvId === cv.cvId}
                        onChange={(e) => setSelectedCvId(parseInt(e.target.value, 10))}
                        className="mt-1 text-indigo-600 focus:ring-indigo-500"
                      />
                      <div className="ml-3 flex-1">
                        <div className="flex items-center">
                          <FileText className="text-indigo-600 mr-2" size={20} />
                          <span className="font-medium text-gray-900">
                            {cv.cvCode || `CV #${cv.cvId}`}
                          </span>
                          {cv.cvStatus === 'ACTIVE' && (
                            <span className="ml-2 text-xs px-2 py-1 bg-green-100 text-green-800 rounded-full">
                              Đang hoạt động
                            </span>
                          )}
                        </div>
                        {cv.cvFile && (
                          <p className="text-sm text-gray-500 mt-1 truncate">
                            {cv.cvFile.split('/').pop()}
                          </p>
                        )}
                      </div>
                    </label>
                  ))}
                  
                  {/* Upload New Link */}
                  <button
                    type="button"
                    onClick={handleUploadNew}
                    className="w-full flex items-center justify-center px-4 py-3 border-2 border-dashed border-gray-300 rounded-lg text-gray-600 hover:border-indigo-400 hover:text-indigo-600 transition-colors"
                  >
                    <Upload size={18} className="mr-2" />
                    Tải CV mới lên
                  </button>
                </div>
              )}
            </div>

            {/* Cover Letter (Optional) */}
            <div className="mb-6">
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Lời nhắn (Không bắt buộc)
              </label>
              <textarea
                value={coverLetter}
                onChange={(e) => setCoverLetter(e.target.value)}
                rows={4}
                placeholder="Viết vài dòng giới thiệu bản thân hoặc lý do bạn phù hợp với vị trí này..."
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 resize-none"
                disabled={loading}
              />
              <p className="text-xs text-gray-500 mt-1">
                Lời nhắn này sẽ được gửi kèm theo hồ sơ của bạn
              </p>
            </div>

            {/* Action Buttons */}
            <div className="flex items-center justify-end gap-3 pt-4 border-t">
              <button
                type="button"
                onClick={onClose}
                className="px-6 py-2.5 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                disabled={loading}
              >
                Hủy
              </button>
              <button
                type="submit"
                className="px-6 py-2.5 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
                disabled={loading || cvList.length === 0}
              >
                {loading ? (
                  <>
                    <Loader2 className="animate-spin mr-2" size={18} />
                    Đang gửi...
                  </>
                ) : (
                  'Gửi hồ sơ'
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </>
  );
};

export default ApplyModal;
