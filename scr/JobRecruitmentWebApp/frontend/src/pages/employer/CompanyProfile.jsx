import { useState, useEffect } from 'react';
import { Building2, Globe, Mail, MapPin, Phone, Upload, Save, Loader2 } from 'lucide-react';
import { toast } from 'react-toastify';
import axiosClient from '../../api/axiosClient';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';

/**
 * CompanyProfile Component
 * Biểu mẫu cho nhà tuyển dụng xem và cập nhật thông tin công ty của họ
 * 
 * Tinh năng:
 * - Lấy dữ liệu công ty: GET /api/v1/companies/me
 * - Cập nhật dữ liệu công ty: PUT /api/v1/companies/me
 * - Tải lên logo công ty: PATCH /api/v1/companies/me/logo
 * - Trình soạn thảo văn bản phong phú cho mô tả (TipTap)
 * - Xác thực biểu mẫu cơ bản
 * 
 * Trường dữ liệu công ty:
 * - Tên công ty (bắt buộc)
 * - URL trang web
 * - Email (bắt buộc)
 * - Địa chỉ
 * - Số điện thoại
 * - Mô tả (Văn bản phong phú)
 * - Logo (Tải lên ảnh)
 */
const CompanyProfile = () => {
  // Hàm tiện ích để tạo URL đầy đủ cho các tệp đã tải lên từ backend
  const getBackendFileUrl = (filePath) => {
    if (!filePath) return null;
    if (filePath.startsWith('http')) return filePath; // Đã là URL đầy đủ
    const baseUrl = import.meta.env.VITE_API_URL?.replace('/api/v1', '') || 'http://localhost:5000';
    return `${baseUrl}${filePath}`;
  };

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [uploadingLogo, setUploadingLogo] = useState(false);
  
  const [formData, setFormData] = useState({
    companyName: '',
    companyWebsite: '',
    companyEmail: '',
    companyAddress: '',
    companyPhone: '',
    companyDescription: '',
    logoUrl: null,
  });

  const [errors, setErrors] = useState({});

  // Trình soạn thảo TipTap
  const editor = useEditor({
    extensions: [StarterKit],
    content: '',
    editorProps: {
      attributes: {
        class: 'prose max-w-none focus:outline-none min-h-[200px] p-4 border border-neutral-300 rounded-lg',
      },
    },
    onUpdate: ({ editor }) => {
      const html = editor.getHTML();
      setFormData(prev => ({ ...prev, companyDescription: html }));
    },
  });

  // Lấy dữ liệu công ty khi component được mount
  useEffect(() => {
    fetchCompanyProfile();
  }, []);

  // Cập nhật nội dung trình soạn thảo khi mô tả thay đổi
  useEffect(() => {
    if (editor && formData.companyDescription !== editor.getHTML()) {
      editor.commands.setContent(formData.companyDescription);
    }
  }, [editor, formData.companyDescription]);

  const fetchCompanyProfile = async () => {
    try {
      setLoading(true);
      const response = await axiosClient.get('/companies/me');
      const company = response.data;
      
      setFormData({
        companyName: company.companyName || '',
        companyWebsite: company.companyWebsite || '',
        companyEmail: company.companyEmail || '',
        companyAddress: company.companyAddress || '',
        companyPhone: company.companyPhone || '',
        companyDescription: company.companyDescription || '',
        logoUrl: company.logoURL || company.logoUrl || null,
      });
    } catch (error) {
      console.error('Error fetching company profile:', error);
      toast.error('Không thể tải thông tin công ty');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    // Xóa lỗi khi người dùng nhập lại
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleLogoUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // Xác thực loại file (chỉ cho phép ảnh)
    if (!file.type.startsWith('image/')) {
      toast.error('Vui lòng chọn file ảnh');
      return;
    }

    // Xác thực kích thước file (tối đa 5MB)
    if (file.size > 5 * 1024 * 1024) {
      toast.error('Kích thước ảnh không được vượt quá 5MB');
      return;
    }

    try {
      setUploadingLogo(true);
      
      const formData = new FormData();
      formData.append('file', file);

      const response = await axiosClient.patch('/companies/me/logo', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      // Backend trả về logoURL trong đối tượng data
      const responseData = response.data.data || response.data;
      const updatedLogoUrl = responseData.logoURL || responseData.logoUrl;
      
      setFormData(prev => ({ ...prev, logoUrl: updatedLogoUrl }));
      toast.success('Cập nhật logo thành công');
    } catch (error) {
      console.error('Error uploading logo:', error);
      toast.error('Không thể tải logo lên. Vui lòng thử lại.');
    } finally {
      setUploadingLogo(false);
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.companyName.trim()) {
      newErrors.companyName = 'Tên công ty là bắt buộc';
    }

    if (!formData.companyEmail.trim()) {
      newErrors.companyEmail = 'Email là bắt buộc';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.companyEmail)) {
      newErrors.companyEmail = 'Email không hợp lệ';
    }

    if (formData.companyPhone && !/^\d{10,11}$/.test(formData.companyPhone.replace(/\s/g, ''))) {
      newErrors.companyPhone = 'Số điện thoại phải có 10-11 chữ số';
    }

    if (formData.companyWebsite && !/^https?:\/\/.+/.test(formData.companyWebsite)) {
      newErrors.companyWebsite = 'Website phải bắt đầu bằng http:// hoặc https://';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      toast.error('Vui lòng kiểm tra lại thông tin');
      return;
    }

    try {
      setSaving(true);
      
      await axiosClient.put('/companies/me', {
        companyName: formData.companyName,
        companyWebsite: formData.companyWebsite,
        companyEmail: formData.companyEmail,
        companyAddress: formData.companyAddress,
        companyPhone: formData.companyPhone,
        companyDescription: formData.companyDescription,
      });

      toast.success('Cập nhật thông tin công ty thành công');
    } catch (error) {
      console.error('Error updating company profile:', error);
      const errorMsg = error.response?.data?.message || 'Không thể cập nhật thông tin';
      toast.error(errorMsg);
    } finally {
      setSaving(false);
    }
  };

  // Các nút thanh công cụ trình soạn thảo TipTap
  const EditorToolbar = () => {
    if (!editor) return null;

    return (
      <div className="flex flex-wrap gap-2 p-2 border-b border-neutral-300 bg-neutral-50 rounded-t-lg">
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleBold().run()}
          className={`px-3 py-1 text-sm font-medium rounded ${
            editor.isActive('bold') ? 'bg-primary text-white' : 'bg-white text-neutral-700 hover:bg-neutral-100'
          }`}
        >
          B
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleItalic().run()}
          className={`px-3 py-1 text-sm font-medium italic rounded ${
            editor.isActive('italic') ? 'bg-primary text-white' : 'bg-white text-neutral-700 hover:bg-neutral-100'
          }`}
        >
          I
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
          className={`px-3 py-1 text-sm font-medium rounded ${
            editor.isActive('heading', { level: 2 }) ? 'bg-primary text-white' : 'bg-white text-neutral-700 hover:bg-neutral-100'
          }`}
        >
          H2
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleHeading({ level: 3 }).run()}
          className={`px-3 py-1 text-sm font-medium rounded ${
            editor.isActive('heading', { level: 3 }) ? 'bg-primary text-white' : 'bg-white text-neutral-700 hover:bg-neutral-100'
          }`}
        >
          H3
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleBulletList().run()}
          className={`px-3 py-1 text-sm font-medium rounded ${
            editor.isActive('bulletList') ? 'bg-primary text-white' : 'bg-white text-neutral-700 hover:bg-neutral-100'
          }`}
        >
          • List
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().toggleOrderedList().run()}
          className={`px-3 py-1 text-sm font-medium rounded ${
            editor.isActive('orderedList') ? 'bg-primary text-white' : 'bg-white text-neutral-700 hover:bg-neutral-100'
          }`}
        >
          1. List
        </button>
      </div>
    );
  };

  // Cấu hình modules cho Quill
  const modules = {
    toolbar: [
      [{ header: [1, 2, 3, false] }],
      ['bold', 'italic', 'underline', 'strike'],
      [{ list: 'ordered' }, { list: 'bullet' }],
      [{ indent: '-1' }, { indent: '+1' }],
      ['link'],
      ['clean'],
    ],
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-neutral-50 flex items-center justify-center">
        <div className="text-center">
          <Loader2 className="w-12 h-12 text-primary animate-spin mx-auto mb-4" />
          <p className="text-neutral-600">Đang tải thông tin công ty...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-neutral-50">
      {/* Header */}
      <div className="bg-white border-b border-neutral-200">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <h1 className="text-3xl font-bold text-neutral-900">Hồ sơ công ty</h1>
          <p className="text-neutral-600 mt-1">Quản lý thông tin và hình ảnh công ty của bạn</p>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Logo Upload Section */}
          <div className="bg-white rounded-lg shadow-sm p-6 border border-neutral-200">
            <h2 className="text-lg font-bold text-neutral-900 mb-4">Logo công ty</h2>
            <div className="flex items-center gap-6">
              <div className="w-32 h-32 border-2 border-dashed border-neutral-300 rounded-lg flex items-center justify-center overflow-hidden bg-neutral-50">
                {formData.logoUrl ? (
                  <img
                    src={getBackendFileUrl(formData.logoUrl)}
                    alt="Company Logo"
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <Building2 className="w-12 h-12 text-neutral-400" />
                )}
              </div>
              <div>
                <label className="cursor-pointer inline-flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-600 transition-colors">
                  <Upload className="w-4 h-4" />
                  {uploadingLogo ? 'Đang tải lên...' : 'Tải logo lên'}
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleLogoUpload}
                    disabled={uploadingLogo}
                    className="hidden"
                  />
                </label>
                <p className="text-xs text-neutral-500 mt-2">
                  Định dạng: JPG, PNG. Kích thước tối đa: 5MB
                </p>
              </div>
            </div>
          </div>

          {/* Basic Information */}
          <div className="bg-white rounded-lg shadow-sm p-6 border border-neutral-200">
            <h2 className="text-lg font-bold text-neutral-900 mb-4">Thông tin cơ bản</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Company Name */}
              <div className="md:col-span-2">
                <label className="block text-sm font-semibold text-neutral-700 mb-2">
                  Tên công ty <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Building2 className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
                  <input
                    type="text"
                    name="companyName"
                    value={formData.companyName}
                    onChange={handleInputChange}
                    className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary focus:border-primary ${
                      errors.companyName ? 'border-red-500' : 'border-neutral-300'
                    }`}
                    placeholder="Nhập tên công ty"
                  />
                </div>
                {errors.companyName && (
                  <p className="text-red-500 text-sm mt-1">{errors.companyName}</p>
                )}
              </div>

              {/* Email */}
              <div>
                <label className="block text-sm font-semibold text-neutral-700 mb-2">
                  Email liên hệ <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
                  <input
                    type="email"
                    name="companyEmail"
                    value={formData.companyEmail}
                    onChange={handleInputChange}
                    readOnly
                    className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary focus:border-primary bg-gray-50 cursor-not-allowed ${
                      errors.companyEmail ? 'border-red-500' : 'border-neutral-300'
                    }`}
                    placeholder="contact@company.com"
                  />
                </div>
                {errors.companyEmail && (
                  <p className="text-red-500 text-sm mt-1">{errors.companyEmail}</p>
                )}
              </div>

              {/* Phone */}
              <div>
                <label className="block text-sm font-semibold text-neutral-700 mb-2">
                  Số điện thoại
                </label>
                <div className="relative">
                  <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
                  <input
                    type="tel"
                    name="companyPhone"
                    value={formData.companyPhone}
                    onChange={handleInputChange}
                    className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary focus:border-primary ${
                      errors.companyPhone ? 'border-red-500' : 'border-neutral-300'
                    }`}
                    placeholder="0123456789"
                  />
                </div>
                {errors.companyPhone && (
                  <p className="text-red-500 text-sm mt-1">{errors.companyPhone}</p>
                )}
              </div>

              {/* Website */}
              <div className="md:col-span-2">
                <label className="block text-sm font-semibold text-neutral-700 mb-2">
                  Website
                </label>
                <div className="relative">
                  <Globe className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
                  <input
                    type="url"
                    name="companyWebsite"
                    value={formData.companyWebsite}
                    onChange={handleInputChange}
                    className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary focus:border-primary ${
                      errors.companyWebsite ? 'border-red-500' : 'border-neutral-300'
                    }`}
                    placeholder="https://www.company.com"
                  />
                </div>
                {errors.companyWebsite && (
                  <p className="text-red-500 text-sm mt-1">{errors.companyWebsite}</p>
                )}
              </div>

              {/* Address */}
              <div className="md:col-span-2">
                <label className="block text-sm font-semibold text-neutral-700 mb-2">
                  Địa chỉ
                </label>
                <div className="relative">
                  <MapPin className="absolute left-3 top-3 w-5 h-5 text-neutral-400" />
                  <textarea
                    name="companyAddress"
                    value={formData.companyAddress}
                    onChange={handleInputChange}
                    rows={3}
                    className="w-full pl-10 pr-4 py-3 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-primary resize-none"
                    placeholder="Nhập địa chỉ công ty"
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Company Description */}
          <div className="bg-white rounded-lg shadow-sm p-6 border border-neutral-200">
            <h2 className="text-lg font-bold text-neutral-900 mb-4">Giới thiệu công ty</h2>
            <div className="border border-neutral-300 rounded-lg overflow-hidden">
              <EditorToolbar />
              <EditorContent editor={editor} />
            </div>
            <p className="text-xs text-neutral-500 mt-2">
              Mô tả chi tiết về công ty sẽ giúp thu hút ứng viên tiềm năng
            </p>
          </div>

          {/* Submit Button */}
          <div className="flex justify-end gap-4">
            <button
              type="button"
              onClick={() => fetchCompanyProfile()}
              className="px-6 py-3 border border-neutral-300 text-neutral-700 rounded-lg hover:bg-neutral-50 transition-colors font-medium"
            >
              Hủy thay đổi
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-6 py-3 bg-primary text-white rounded-lg hover:bg-primary-600 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            >
              {saving ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Đang lưu...
                </>
              ) : (
                <>
                  <Save className="w-5 h-5" />
                  Lưu thay đổi
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CompanyProfile;
