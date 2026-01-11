import React, { useState, useEffect } from 'react';
import { Plus, Edit2, Trash2, X, Loader2, Search } from 'lucide-react';
import { toast } from 'react-toastify';
import axiosClient from '../../api/axiosClient';
import { formatVND } from '../../utils/formatters';

/**
 * CategoryManager Component
 * Giao diện CRUD quản lý danh mục công việc
 * 
 * Tính năng:
 * - Xem tất cả danh mục trong bảng
 * - Thêm danh mục mới (form modal)
 * - Chỉnh sửa danh mục hiện có (form modal)
 * - Xóa danh mục với xác nhận
 * - Đồng bộ dữ liệu thời gian thực với backend
 * 
 * API Endpoints:
 * - GET /api/v1/categories - Xem tất cả danh mục
 * - POST /api/v1/categories - Thêm danh mục mới (Chỉ Admin)
 * - PUT /api/v1/categories/{id} - Cập nhật danh mục (Chỉ Admin)
 * - DELETE /api/v1/categories/{id} - Xóa danh mục (Chỉ Admin)
 * 
 * Quy tắc nghiệp vụ:
 * - Tên danh mục bắt buộc
 * - Mức lương cơ bản phải là số dương (> 0)
 * - Không thể xóa nếu tồn tại công việc thuộc danh mục này (xác thực backend)
 */
const CategoryManager = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState('add'); // 'add' or 'edit'
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [saving, setSaving] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  // Form state
  const [formData, setFormData] = useState({
    jcName: '',
    jcDescription: '',
    jcBaseSalary: '',
  });

  const [errors, setErrors] = useState({});

  // Lấy danh sách danh mục khi component được mount
  useEffect(() => {
    fetchCategories();
  }, []);

  /**
   * Lấy tất cả danh mục công việc
   */
  const fetchCategories = async () => {
    try {
      setLoading(true);
      const response = await axiosClient.get('/categories');
      
      // Backend trả về: {status, message, data: [...]}
      // Axios bọc nó trong: {data: {status, message, data: [...]}}
      const categoriesData = response.data?.data || response.data || [];
      console.log('📋 Categories loaded:', categoriesData);
      setCategories(categoriesData);
    } catch (error) {
      console.error('Error fetching categories:', error);
      toast.error('Không thể tải danh sách danh mục');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Mở modal để thêm danh mục mới
   */
  const handleAddClick = () => {
    setModalMode('add');
    setSelectedCategory(null);
    setFormData({
      jcName: '',
      jcDescription: '',
      jcBaseSalary: '',
    });
    setErrors({});
    setIsModalOpen(true);
  };

  /**
   * Mở modal để chỉnh sửa danh mục hiện có
   */
  const handleEditClick = (category) => {
    setModalMode('edit');
    setSelectedCategory(category);
    setFormData({
      jcName: category.jcName || '',
      jcDescription: category.jcDescription || '',
      jcBaseSalary: category.jcBaseSalary || '',
    });
    setErrors({});
    setIsModalOpen(true);
  };

  /**
   * Đóng modal và đặt lại form
   */
  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedCategory(null);
    setFormData({
      jcName: '',
      jcDescription: '',
      jcBaseSalary: '',
    });
    setErrors({});
  };

  /**
   * Xử lý thay đổi input form
   */
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    // Clear error for this field
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  /**
   * Xác thực dữ liệu form
   */
  const validateForm = () => {
    const newErrors = {};

    if (!formData.jcName.trim()) {
      newErrors.jcName = 'Tên danh mục không được để trống';
    }

    if (!formData.jcBaseSalary) {
      newErrors.jcBaseSalary = 'Mức lương cơ bản không được để trống';
    } else if (parseFloat(formData.jcBaseSalary) <= 0) {
      newErrors.jcBaseSalary = 'Mức lương phải lớn hơn 0';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  /**
   * Xử lý gửi form (Tạo hoặc Cập nhật danh mục)
   */
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      setSaving(true);

      const payload = {
        jcName: formData.jcName.trim(),
        jcDescription: formData.jcDescription.trim() || null,
        jcBaseSalary: parseFloat(formData.jcBaseSalary),
      };

      if (modalMode === 'add') {
        // Tạo danh mục mới
        await axiosClient.post('/categories', payload);
        toast.success('Thêm danh mục thành công');
      } else {
        // Cập nhật danh mục hiện có
        await axiosClient.put(`/categories/${selectedCategory.jcId}`, payload);
        toast.success('Cập nhật danh mục thành công');
      }

      handleCloseModal();
      fetchCategories(); // Làm mới danh sách
    } catch (error) {
      console.error('Error saving category:', error);
      const errorMessage = error.response?.data?.message || 'Có lỗi xảy ra khi lưu danh mục';
      toast.error(errorMessage);
    } finally {
      setSaving(false);
    }
  };

  /**
   * Xử lý xóa danh mục
   */
  const handleDelete = async (category) => {
    const confirmed = window.confirm(
      `Bạn có chắc chắn muốn xóa danh mục "${category.jcName}"?\n\nLưu ý: Không thể xóa danh mục nếu có công việc đang sử dụng.`
    );

    if (!confirmed) {
      return;
    }

    try {
      await axiosClient.delete(`/categories/${category.jcId}`);
      toast.success('Xóa danh mục thành công');
      fetchCategories(); // Làm mới danh sách
    } catch (error) {
      console.error('Error deleting category:', error);
      const errorMessage =
        error.response?.data?.message ||
        'Không thể xóa danh mục. Có thể danh mục này đang được sử dụng bởi các công việc.';
      toast.error(errorMessage);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-600"></div>
      </div>
    );
  }

  // Lọc danh mục dựa trên từ khóa tìm kiếm
  const filteredCategories = categories.filter((category) => {
    const searchLower = searchTerm.toLowerCase();
    return (
      category.jcName?.toLowerCase().includes(searchLower) ||
      category.jcDescription?.toLowerCase().includes(searchLower) ||
      category.jcId?.toString().includes(searchLower)
    );
  });

  return (
    <div>
      {/* Page Header */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold text-neutral-900">Quản lý danh mục</h1>
          <p className="text-neutral-600 mt-2">Quản lý danh mục công việc trong hệ thống</p>
        </div>
        <button
          onClick={handleAddClick}
          className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
        >
          <Plus className="w-5 h-5" />
          Thêm danh mục
        </button>
      </div>

      {/* Search Bar */}
      <div className="mb-4">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-neutral-400 w-5 h-5" />
          <input
            type="text"
            placeholder="Tìm kiếm danh mục theo tên, mô tả hoặc ID..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
          />
          {searchTerm && (
            <button
              onClick={() => setSearchTerm('')}
              className="absolute right-3 top-1/2 transform -translate-y-1/2 text-neutral-400 hover:text-neutral-600"
              title="Xóa tìm kiếm"
            >
              <X className="w-5 h-5" />
            </button>
          )}
        </div>
        {searchTerm && (
          <p className="text-sm text-neutral-600 mt-2">
            Tìm thấy {filteredCategories.length} kết quả
          </p>
        )}
      </div>

      {/* Categories Table */}
      <div className="bg-white rounded-lg shadow-sm border border-neutral-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-neutral-50 border-b border-neutral-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                ID
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                Tên danh mục
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                Mô tả
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                Mức lương cơ bản
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-neutral-500 uppercase tracking-wider">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-neutral-200">
            {filteredCategories.length === 0 ? (
              <tr>
                <td colSpan="5" className="px-6 py-8 text-center text-neutral-500">
                  {searchTerm ? 'Không tìm thấy danh mục nào' : 'Chưa có danh mục nào'}
                </td>
              </tr>
            ) : (
              filteredCategories.map((category) => (
                <tr key={category.jcId} className="hover:bg-neutral-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-neutral-900">
                    {category.jcId}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-neutral-900">
                    {category.jcName}
                  </td>
                  <td className="px-6 py-4 text-sm text-neutral-600 max-w-md truncate">
                    {category.jcDescription || '-'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-neutral-900">
                    {formatVND(category.jcBaseSalary)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button
                      onClick={() => handleEditClick(category)}
                      className="text-blue-600 hover:text-blue-900 mr-4"
                      title="Chỉnh sửa"
                    >
                      <Edit2 className="w-4 h-4 inline" />
                    </button>
                    <button
                      onClick={() => handleDelete(category)}
                      className="text-red-600 hover:text-red-900"
                      title="Xóa"
                    >
                      <Trash2 className="w-4 h-4 inline" />
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Modal for Add/Edit */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-lg w-full">
            {/* Modal Header */}
            <div className="flex justify-between items-center px-6 py-4 border-b border-neutral-200">
              <h2 className="text-xl font-bold text-neutral-900">
                {modalMode === 'add' ? 'Thêm danh mục mới' : 'Chỉnh sửa danh mục'}
              </h2>
              <button
                onClick={handleCloseModal}
                className="text-neutral-400 hover:text-neutral-600"
              >
                <X className="w-6 h-6" />
              </button>
            </div>

            {/* Modal Body */}
            <form onSubmit={handleSubmit} className="px-6 py-4 space-y-4">
              {/* Category Name */}
              <div>
                <label className="block text-sm font-medium text-neutral-700 mb-1">
                  Tên danh mục <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  name="jcName"
                  value={formData.jcName}
                  onChange={handleInputChange}
                  placeholder="Ví dụ: Công nghệ thông tin"
                  className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500 ${
                    errors.jcName ? 'border-red-500' : 'border-neutral-300'
                  }`}
                />
                {errors.jcName && (
                  <p className="text-red-500 text-sm mt-1">{errors.jcName}</p>
                )}
              </div>

              {/* Description */}
              <div>
                <label className="block text-sm font-medium text-neutral-700 mb-1">
                  Mô tả
                </label>
                <textarea
                  name="jcDescription"
                  value={formData.jcDescription}
                  onChange={handleInputChange}
                  placeholder="Mô tả chi tiết về danh mục..."
                  rows="3"
                  className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                ></textarea>
              </div>

              {/* Base Salary */}
              <div>
                <label className="block text-sm font-medium text-neutral-700 mb-1">
                  Mức lương cơ bản (VNĐ) <span className="text-red-500">*</span>
                </label>
                <input
                  type="number"
                  name="jcBaseSalary"
                  value={formData.jcBaseSalary}
                  onChange={handleInputChange}
                  placeholder="Ví dụ: 15000000"
                  min="0"
                  step="1000000"
                  className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500 ${
                    errors.jcBaseSalary ? 'border-red-500' : 'border-neutral-300'
                  }`}
                />
                {errors.jcBaseSalary && (
                  <p className="text-red-500 text-sm mt-1">{errors.jcBaseSalary}</p>
                )}
              </div>

              {/* Modal Footer */}
              <div className="flex justify-end gap-3 pt-4">
                <button
                  type="button"
                  onClick={handleCloseModal}
                  disabled={saving}
                  className="px-4 py-2 border border-neutral-300 text-neutral-700 rounded-lg hover:bg-neutral-50 transition-colors disabled:opacity-50"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50"
                >
                  {saving && <Loader2 className="w-4 h-4 animate-spin" />}
                  {modalMode === 'add' ? 'Thêm mới' : 'Cập nhật'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default CategoryManager;
