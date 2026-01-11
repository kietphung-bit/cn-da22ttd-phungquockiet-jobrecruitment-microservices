import React, { useState, useEffect } from 'react';
import { FileText, Upload, Trash2, Eye, EyeOff, X, Plus } from 'lucide-react';
import cvService from '../../services/cv.service';

/**
 * CVManager Component
 * Quản lý file CV của ứng viên với bảng hiển thị và modal tải lên
 * 
 * Tính năng:
 * - Bảng liệt kê tất cả CV (Tên, Ngày, Trạng thái)
 * - Modal tải lên file PDF
 * - Chuyển đổi trạng thái CV (ACTIVE/HIDDEN)
 * - Xóa CV (xóa mềm)
 * - API: GET/POST/PATCH/DELETE /api/v1/cvs
 */
const CVManager = () => {
  const [cvs, setCvs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    fetchCVs();
  }, []);

  const fetchCVs = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await cvService.getMyCVs();
      setCvs(response.data || []);
    } catch (err) {
      console.error('Error fetching CVs:', err);
      setError('Không thể tải danh sách CV. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  };

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      // Kiểm tra loại file
      if (file.type !== 'application/pdf') {
        setError('Chỉ chấp nhận file PDF');
        return;
      }
      // Kiểm tra kích thước file (tối đa 5MB)
      if (file.size > 5 * 1024 * 1024) {
        setError('Kích thước file không được vượt quá 5MB');
        return;
      }
      setSelectedFile(file);
      setError('');
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      setError('Vui lòng chọn file CV');
      return;
    }

    setUploading(true);
    setError('');
    setSuccess('');

    try {
      // Sử dụng cvService, trong đó xử lý việc tạo FormData bên trong
      const response = await cvService.uploadCV(selectedFile);

      setSuccess('Tải CV lên thành công!');
      setIsModalOpen(false);
      setSelectedFile(null);
      fetchCVs(); // Refresh list

      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      console.error('Error uploading CV:', err);
      setError(err.response?.data?.message || 'Tải CV lên thất bại. Vui lòng thử lại.');
    } finally {
      setUploading(false);
    }
  };

  const handleToggleStatus = async (cvId, currentStatus) => {
    const newStatus = currentStatus === 'ACTIVE' ? 'HIDDEN' : 'ACTIVE';
    
    try {
      // Sử dụng cvService với tên tham số đúng 'newStatus'
      await cvService.updateCVStatus(cvId, newStatus);

      setSuccess(`Cập nhật trạng thái thành công!`);
      fetchCVs(); // Làm mới danh sách

      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      console.error('Error toggling CV status:', err);
      setError(err.response?.data?.message || 'Cập nhật trạng thái thất bại.');
    }
  };

  const handleDelete = async (cvId) => {
    if (!window.confirm('Bạn có chắc chắn muốn xóa CV này?')) {
      return;
    }

    try {
      await cvService.deleteCV(cvId);
      setSuccess('Xóa CV thành công!');
      fetchCVs(); // Làm mới danh sách

      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      console.error('Error deleting CV:', err);
      setError(err.response?.data?.message || 'Xóa CV thất bại.');
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
  };

  const getStatusBadge = (status) => {
    if (status === 'ACTIVE') {
      return (
        <span className="inline-flex items-center gap-1 px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm font-medium">
          <Eye className="w-4 h-4" />
          Đang hoạt động
        </span>
      );
    }
    return (
      <span className="inline-flex items-center gap-1 px-3 py-1 bg-gray-100 text-gray-800 rounded-full text-sm font-medium">
        <EyeOff className="w-4 h-4" />
        Tạm ẩn
      </span>
    );
  };

  return (
    <div className="bg-white rounded-lg shadow-sm min-h-[calc(100vh-200px)]">
      {/* Header */}
      <div className="px-6 py-5 border-b border-neutral-200 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-neutral-900">Danh sách hồ sơ</h1>
          <p className="text-sm text-neutral-600 mt-1">
            Quản lý các file CV của bạn
          </p>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="flex items-center gap-2 px-6 py-3 bg-primary text-white rounded-lg hover:bg-primary-600 transition-colors font-medium"
        >
          <Plus className="w-5 h-5" />
          Thêm mới
        </button>
      </div>

      {/* Messages */}
      <div className="p-6 space-y-3">
        {success && (
          <div className="bg-green-50 border border-green-200 text-green-800 px-4 py-3 rounded-lg">
            {success}
          </div>
        )}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}
      </div>

      {/* Table */}
      <div className="px-6 pb-6">
        {loading ? (
          <div className="flex items-center justify-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
          </div>
        ) : cvs.length === 0 ? (
          <div className="text-center py-12">
            <FileText className="w-16 h-16 text-neutral-300 mx-auto mb-4" />
            <p className="text-neutral-500 text-lg">Chưa có CV nào</p>
            <p className="text-neutral-400 text-sm mt-2">
              Nhấn nút "Thêm mới" để tải lên CV của bạn
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="bg-neutral-50 border-b border-neutral-200">
                  <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">
                    Mã CV
                  </th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">
                    Tên file
                  </th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">
                    Ngày tải lên
                  </th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">
                    Trạng thái
                  </th>
                  <th className="px-6 py-4 text-right text-sm font-semibold text-neutral-700">
                    Thao tác
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-200">
                {cvs.map((cv) => (
                  <tr key={cv.cvId} className="hover:bg-neutral-50 transition-colors">
                    <td className="px-6 py-4 text-sm text-neutral-900 font-medium">
                      {cv.cvCode}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        <FileText className="w-5 h-5 text-red-500" />
                        <span className="text-sm text-neutral-900">
                          {cv.cvFile ? cv.cvFile.split('/').pop() : 'CV.pdf'}
                        </span>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-neutral-600">
                      {formatDate(cv.createdAt)}
                    </td>
                    <td className="px-6 py-4">
                      {getStatusBadge(cv.cvStatus)}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => handleToggleStatus(cv.cvId, cv.cvStatus)}
                          className="p-2 text-neutral-600 hover:text-primary hover:bg-primary-50 rounded-lg transition-colors"
                          title={cv.cvStatus === 'ACTIVE' ? 'Ẩn CV' : 'Kích hoạt CV'}
                        >
                          {cv.cvStatus === 'ACTIVE' ? (
                            <EyeOff className="w-5 h-5" />
                          ) : (
                            <Eye className="w-5 h-5" />
                          )}
                        </button>
                        <button
                          onClick={() => handleDelete(cv.cvId)}
                          className="p-2 text-neutral-600 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                          title="Xóa CV"
                        >
                          <Trash2 className="w-5 h-5" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Upload Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full">
            {/* Modal Header */}
            <div className="px-6 py-4 border-b border-neutral-200 flex items-center justify-between">
              <h2 className="text-xl font-bold text-neutral-900">Tải lên CV mới</h2>
              <button
                onClick={() => {
                  setIsModalOpen(false);
                  setSelectedFile(null);
                  setError('');
                }}
                className="p-1 text-neutral-400 hover:text-neutral-600 transition-colors"
              >
                <X className="w-6 h-6" />
              </button>
            </div>

            {/* Modal Body */}
            <div className="px-6 py-4 space-y-4">
              {error && (
                <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg text-sm">
                  {error}
                </div>
              )}

              <div>
                <label className="block text-sm font-medium text-neutral-700 mb-2">
                  Chọn file CV (PDF, tối đa 5MB)
                </label>
                <input
                  type="file"
                  accept=".pdf"
                  onChange={handleFileSelect}
                  className="block w-full text-sm text-neutral-600
                    file:mr-4 file:py-2 file:px-4
                    file:rounded-lg file:border-0
                    file:text-sm file:font-medium
                    file:bg-primary file:text-white
                    file:cursor-pointer
                    hover:file:bg-primary-600
                    transition-colors"
                />
              </div>

              {selectedFile && (
                <div className="flex items-center gap-2 p-3 bg-neutral-50 rounded-lg">
                  <FileText className="w-5 h-5 text-red-500" />
                  <span className="text-sm text-neutral-900 flex-1 truncate">
                    {selectedFile.name}
                  </span>
                  <span className="text-xs text-neutral-500">
                    {(selectedFile.size / 1024 / 1024).toFixed(2)} MB
                  </span>
                </div>
              )}
            </div>

            {/* Modal Footer */}
            <div className="px-6 py-4 border-t border-neutral-200 flex justify-end gap-3">
              <button
                onClick={() => {
                  setIsModalOpen(false);
                  setSelectedFile(null);
                  setError('');
                }}
                className="px-6 py-2 border border-neutral-300 text-neutral-700 rounded-lg hover:bg-neutral-50 transition-colors font-medium"
              >
                Hủy
              </button>
              <button
                onClick={handleUpload}
                disabled={!selectedFile || uploading}
                className="flex items-center gap-2 px-6 py-2 bg-primary text-white rounded-lg hover:bg-primary-600 disabled:bg-neutral-300 disabled:cursor-not-allowed transition-colors font-medium"
              >
                {uploading ? (
                  <>
                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                    Đang tải...
                  </>
                ) : (
                  <>
                    <Upload className="w-5 h-5" />
                    Tải lên
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CVManager;
