import React, { useState, useEffect } from 'react';
import { Plus, Edit2, Trash2, X, Loader2 } from 'lucide-react';
import { toast } from 'react-toastify';
import axiosClient from '../../api/axiosClient';
import { formatVND } from '../../utils/formatters';

/**
 * CategoryManager Component
 * CRUD interface for managing job categories
 * 
 * Features:
 * - View all categories in a table
 * - Add new category (Modal form)
 * - Edit existing category (Modal form)
 * - Delete category with confirmation
 * - Real-time data synchronization
 * 
 * API Endpoints:
 * - GET /api/v1/categories - List all categories
 * - POST /api/v1/categories - Create new category (Admin only)
 * - PUT /api/v1/categories/{id} - Update category (Admin only)
 * - DELETE /api/v1/categories/{id} - Delete category (Admin only)
 * 
 * Business Rules:
 * - Category name required
 * - Base salary must be positive (> 0)
 * - Cannot delete if jobs exist with this category (backend validation)
 */
const CategoryManager = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState('add'); // 'add' or 'edit'
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [saving, setSaving] = useState(false);

  // Form state
  const [formData, setFormData] = useState({
    jcName: '',
    jcDescription: '',
    jcBaseSalary: '',
  });

  const [errors, setErrors] = useState({});

  // Fetch categories on mount
  useEffect(() => {
    fetchCategories();
  }, []);

  /**
   * Fetch all job categories
   */
  const fetchCategories = async () => {
    try {
      setLoading(true);
      const response = await axiosClient.get('/categories');
      
      // Backend returns: {status, message, data: [...]}
      // Axios wraps it in: {data: {status, message, data: [...]}}
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
   * Open modal for adding new category
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
   * Open modal for editing existing category
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
   * Close modal and reset form
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
   * Handle form input change
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
   * Validate form data
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
   * Handle form submission (Create or Update)
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
        // Create new category
        await axiosClient.post('/categories', payload);
        toast.success('Thêm danh mục thành công');
      } else {
        // Update existing category
        await axiosClient.put(`/categories/${selectedCategory.jcId}`, payload);
        toast.success('Cập nhật danh mục thành công');
      }

      handleCloseModal();
      fetchCategories(); // Refresh list
    } catch (error) {
      console.error('Error saving category:', error);
      const errorMessage = error.response?.data?.message || 'Có lỗi xảy ra khi lưu danh mục';
      toast.error(errorMessage);
    } finally {
      setSaving(false);
    }
  };

  /**
   * Handle category deletion
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
      fetchCategories(); // Refresh list
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
            {categories.length === 0 ? (
              <tr>
                <td colSpan="5" className="px-6 py-8 text-center text-neutral-500">
                  Chưa có danh mục nào
                </td>
              </tr>
            ) : (
              categories.map((category) => (
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
