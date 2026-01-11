import React, { useState, useEffect } from 'react';
import { User, Phone, Calendar, MapPin, Mail, Save } from 'lucide-react';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import axiosClient from '../../api/axiosClient';
import { useAuth } from '../../contexts/AuthContext';
import '../../styles/tiptap.css';

/**
 * CandidateProfile Component
 * Cho phép ứng viên xem và chỉnh sửa thông tin hồ sơ cá nhân
 * 
 * Tính năng:
 * - Form thông tin cá nhân (tên, email, điện thoại, ngày sinh, giới tính)
 * - Trình soạn thảo rich text cho mô tả và kinh nghiệm (Tiptap)
 * - Nhập kỹ năng dưới dạng tag
 * - Tích hợp API với /api/v1/candidates/me
 * 
 * Lưu ý: Dựa trên sơ đồ cơ sở dữ liệu (Candidate.java)
 * Trường: candidateName, candidateEmail, candidatePhone, candidateBirthdate,
 *         candidateGender, candidateDescription, candidateEducation, 
 *         candidateExp, candidateSkills
 * KHÔNG CÓ TRƯỜNG AVATAR trong cơ sở dữ liệu
 */
const CandidateProfile = () => {
  const { user, updateUser } = useAuth();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Trạng thái form - khớp chính xác với các trường của thực thể Candidate
  const [formData, setFormData] = useState({
    candidateName: '',
    candidateEmail: '',
    candidatePhone: '',
    candidateBirthdate: '',
    candidateGender: 'MALE',
    candidateDescription: '',
    candidateEducation: '',
    candidateExp: '',
    candidateSkills: '',
  });

  // Trạng thái tag kỹ năng
  const [skillInput, setSkillInput] = useState('');
  const [skills, setSkills] = useState([]);

  // Trình soạn thảo Tiptap cho candidateDescription
  const descriptionEditor = useEditor({
    extensions: [StarterKit],
    content: formData.candidateDescription,
    onUpdate: ({ editor }) => {
      setFormData((prev) => ({ ...prev, candidateDescription: editor.getHTML() }));
    },
  });

  // Trình soạn thảo Tiptap cho candidateExp
  const experienceEditor = useEditor({
    extensions: [StarterKit],
    content: formData.candidateExp,
    onUpdate: ({ editor }) => {
      setFormData((prev) => ({ ...prev, candidateExp: editor.getHTML() }));
    },
  });

  // Lấy thông tin hồ sơ ứng viên khi component mount
  useEffect(() => {
    fetchProfile();
  }, []);

  // Cập nhật editors khi dữ liệu form thay đổi từ API
  useEffect(() => {
    if (descriptionEditor && formData.candidateDescription) {
      descriptionEditor.commands.setContent(formData.candidateDescription);
    }
  }, [formData.candidateDescription, descriptionEditor]);

  useEffect(() => {
    if (experienceEditor && formData.candidateExp) {
      experienceEditor.commands.setContent(formData.candidateExp);
    }
  }, [formData.candidateExp, experienceEditor]);

  const fetchProfile = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await axiosClient.get('/candidates/me');
      const data = response.data;
      
      console.log('CandidateProfile API Response:', data);

      setFormData({
        candidateName: data.candidateName || '',
        candidateEmail: data.candidateEmail || '',
        candidatePhone: data.candidatePhone || '',
        candidateBirthdate: data.candidateBirthdate || '',
        candidateGender: data.candidateGender || 'MALE',
        candidateDescription: data.candidateDescription || '',
        candidateEducation: data.candidateEducation || '',
        candidateExp: data.candidateExp || '',
        candidateSkills: data.candidateSkills || '',
      });

      // Cập nhật kỹ năng từ chuỗi sang mảng
      if (data.candidateSkills) {
        setSkills(data.candidateSkills.split(',').map((s) => s.trim()).filter((s) => s));
      }
    } catch (err) {
      console.error('Error fetching profile:', err);
      setError('Không thể tải thông tin hồ sơ. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // Quản lý kỹ năng
  const handleSkillInputKeyDown = (e) => {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault();
      addSkill();
    }
  };

  const addSkill = () => {
    const trimmedSkill = skillInput.trim();
    if (trimmedSkill && !skills.includes(trimmedSkill)) {
      setSkills([...skills, trimmedSkill]);
      setSkillInput('');
    }
  };

  const removeSkill = (skillToRemove) => {
    setSkills(skills.filter((skill) => skill !== skillToRemove));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setSaving(true);

    try {
      // Cập nhật dữ liệu với kỹ năng dưới dạng chuỗi phân cách bằng dấu phẩy
      const updateData = {
        ...formData,
        candidateSkills: skills.join(', '),
      };

      const response = await axiosClient.put('/candidates/me', updateData);

      setSuccess('Cập nhật thông tin thành công!');

      // Cập nhật ngữ cảnh người dùng nếu tên hoặc email thay đổi
      if (updateUser && response.data) {
        updateUser({
          ...user,
          candidateName: response.data.candidateName,
          email: response.data.candidateEmail,
        });
      }

      // Xóa thông báo thành công sau 3 giây
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      console.error('Error updating profile:', err);
      setError(err.response?.data?.message || 'Cập nhật thất bại. Vui lòng thử lại.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm">
      {/* Header */}
      <div className="px-6 py-5 border-b border-neutral-200">
        <h1 className="text-2xl font-bold text-neutral-900">Quản lý tài khoản</h1>
        <p className="text-sm text-neutral-600 mt-1">
          Cập nhật thông tin cá nhân của bạn
        </p>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="p-6 space-y-8">
        {/* Success Message */}
        {success && (
          <div className="bg-green-50 border border-green-200 text-green-800 px-4 py-3 rounded-lg">
            {success}
          </div>
        )}

        {/* Error Message */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        {/* Avatar Upload */}
        <div className="flex flex-col items-center">
          <div className="w-32 h-32 rounded-full overflow-hidden bg-primary text-white text-5xl font-bold flex items-center justify-center shadow-lg border-4 border-white">
            {formData.candidateName ? formData.candidateName.charAt(0).toUpperCase() : 'U'}
          </div>
          <p className="text-lg font-semibold text-neutral-700 mt-3">
            {formData.candidateName || 'Chưa cập nhật tên'}
          </p>
          <p className="text-sm text-neutral-500">{formData.candidateEmail || 'Chưa cập nhật email'}</p>
        </div>

        {/* Personal Information */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Full Name */}
          <div>
            <label className="block text-sm font-medium text-neutral-700 mb-2">
              Họ và tên <span className="text-red-500">*</span>
            </label>
            <div className="relative">
              <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
              <input
                type="text"
                name="candidateName"
                value={formData.candidateName}
                onChange={handleInputChange}
                required
                className="w-full pl-10 pr-4 py-2.5 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                placeholder="Nguyễn Văn A"
              />
            </div>
          </div>

          {/* Email */}
          <div>
            <label className="block text-sm font-medium text-neutral-700 mb-2">
              Email <span className="text-red-500">*</span>
            </label>
            <div className="relative">
              <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
              <input
                type="email"
                name="candidateEmail"
                value={formData.candidateEmail}
                onChange={handleInputChange}
                required
                readOnly
                className="w-full pl-10 pr-4 py-2.5 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent bg-gray-50 cursor-not-allowed"
                placeholder="email@example.com"
              />
            </div>
          </div>

          {/* Phone */}
          <div>
            <label className="block text-sm font-medium text-neutral-700 mb-2">
              Số điện thoại <span className="text-red-500">*</span>
            </label>
            <div className="relative">
              <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
              <input
                type="tel"
                name="candidatePhone"
                value={formData.candidatePhone}
                onChange={handleInputChange}
                required
                pattern="[0-9]{10,11}"
                className="w-full pl-10 pr-4 py-2.5 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                placeholder="0901234567"
              />
            </div>
          </div>

          {/* Date of Birth */}
          <div>
            <label className="block text-sm font-medium text-neutral-700 mb-2">
              Ngày sinh <span className="text-red-500">*</span>
            </label>
            <div className="relative">
              <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
              <input
                type="date"
                name="candidateBirthdate"
                value={formData.candidateBirthdate}
                onChange={handleInputChange}
                required
                className="w-full pl-10 pr-4 py-2.5 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
              />
            </div>
          </div>

          {/* Gender */}
          <div>
            <label className="block text-sm font-medium text-neutral-700 mb-2">
              Giới tính <span className="text-red-500">*</span>
            </label>
            <select
              name="candidateGender"
              value={formData.candidateGender}
              onChange={handleInputChange}
              required
              className="w-full px-4 py-2.5 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
            >
              <option value="MALE">Nam</option>
              <option value="FEMALE">Nữ</option>
              <option value="OTHER">Khác</option>
            </select>
          </div>

          {/* Address */}
          <div className="md:col-span-2">
            <label className="block text-sm font-medium text-neutral-700 mb-2">
              Học vấn
            </label>
            <textarea
              name="candidateEducation"
              value={formData.candidateEducation}
              onChange={handleInputChange}
              rows="3"
              className="w-full px-4 py-2.5 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent resize-none"
              placeholder="Đại học Bách Khoa, Chuyên ngành Công nghệ Thông tin"
            />
          </div>
        </div>

        {/* Description */}
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-2">
            Mô tả bản thân
          </label>
          <div className="border border-neutral-300 rounded-lg overflow-hidden">
            <div className="bg-neutral-50 border-b border-neutral-300 p-2 flex gap-1">
              <button
                type="button"
                onClick={() => descriptionEditor?.chain().focus().toggleBold().run()}
                className={`px-3 py-1 rounded hover:bg-neutral-200 ${
                  descriptionEditor?.isActive('bold') ? 'bg-neutral-300 font-bold' : ''
                }`}
              >
                B
              </button>
              <button
                type="button"
                onClick={() => descriptionEditor?.chain().focus().toggleItalic().run()}
                className={`px-3 py-1 rounded hover:bg-neutral-200 italic ${
                  descriptionEditor?.isActive('italic') ? 'bg-neutral-300' : ''
                }`}
              >
                I
              </button>
              <button
                type="button"
                onClick={() => descriptionEditor?.chain().focus().toggleBulletList().run()}
                className={`px-3 py-1 rounded hover:bg-neutral-200 ${
                  descriptionEditor?.isActive('bulletList') ? 'bg-neutral-300' : ''
                }`}
              >
                • List
              </button>
              <button
                type="button"
                onClick={() => descriptionEditor?.chain().focus().toggleOrderedList().run()}
                className={`px-3 py-1 rounded hover:bg-neutral-200 ${
                  descriptionEditor?.isActive('orderedList') ? 'bg-neutral-300' : ''
                }`}
              >
                1. List
              </button>
            </div>
            <EditorContent
              editor={descriptionEditor}
              className="prose max-w-none p-4 min-h-[150px] focus:outline-none"
            />
          </div>
        </div>

        {/* Experience */}
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-2">
            Kinh nghiệm làm việc
          </label>
          <div className="border border-neutral-300 rounded-lg overflow-hidden">
            <div className="bg-neutral-50 border-b border-neutral-300 p-2 flex gap-1">
              <button
                type="button"
                onClick={() => experienceEditor?.chain().focus().toggleBold().run()}
                className={`px-3 py-1 rounded hover:bg-neutral-200 ${
                  experienceEditor?.isActive('bold') ? 'bg-neutral-300 font-bold' : ''
                }`}
              >
                B
              </button>
              <button
                type="button"
                onClick={() => experienceEditor?.chain().focus().toggleItalic().run()}
                className={`px-3 py-1 rounded hover:bg-neutral-200 italic ${
                  experienceEditor?.isActive('italic') ? 'bg-neutral-300' : ''
                }`}
              >
                I
              </button>
              <button
                type="button"
                onClick={() => experienceEditor?.chain().focus().toggleBulletList().run()}
                className={`px-3 py-1 rounded hover:bg-neutral-200 ${
                  experienceEditor?.isActive('bulletList') ? 'bg-neutral-300' : ''
                }`}
              >
                • List
              </button>
              <button
                type="button"
                onClick={() => experienceEditor?.chain().focus().toggleOrderedList().run()}
                className={`px-3 py-1 rounded hover:bg-neutral-200 ${
                  experienceEditor?.isActive('orderedList') ? 'bg-neutral-300' : ''
                }`}
              >
                1. List
              </button>
            </div>
            <EditorContent
              editor={experienceEditor}
              className="prose max-w-none p-4 min-h-[150px] focus:outline-none"
            />
          </div>
        </div>

        {/* Skills */}
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-2">
            Kỹ năng
          </label>
          <div className="space-y-3">
            {/* Skill Tags Display */}
            {skills.length > 0 && (
              <div className="flex flex-wrap gap-2">
                {skills.map((skill, index) => (
                  <span
                    key={index}
                    className="inline-flex items-center gap-1 px-3 py-1.5 bg-primary-50 text-primary-700 rounded-full text-sm font-medium"
                  >
                    {skill}
                    <button
                      type="button"
                      onClick={() => removeSkill(skill)}
                      className="hover:text-primary-900 transition-colors"
                    >
                      ×
                    </button>
                  </span>
                ))}
              </div>
            )}

            {/* Skill Input */}
            <div className="flex gap-2">
              <input
                type="text"
                value={skillInput}
                onChange={(e) => setSkillInput(e.target.value)}
                onKeyDown={handleSkillInputKeyDown}
                className="flex-1 px-4 py-2.5 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                placeholder="Nhập kỹ năng và nhấn Enter"
              />
              <button
                type="button"
                onClick={addSkill}
                className="px-6 py-2.5 bg-neutral-200 text-neutral-700 rounded-lg hover:bg-neutral-300 transition-colors font-medium"
              >
                Thêm
              </button>
            </div>
            <p className="text-xs text-neutral-500">
              Nhấn Enter hoặc dấu phẩy để thêm kỹ năng
            </p>
          </div>
        </div>

        {/* Submit Button */}
        <div className="flex justify-end pt-4 border-t border-neutral-200">
          <button
            type="submit"
            disabled={saving}
            className="flex items-center gap-2 px-8 py-3 bg-primary text-white rounded-lg hover:bg-primary-600 disabled:bg-neutral-300 disabled:cursor-not-allowed transition-colors font-medium"
          >
            {saving ? (
              <>
                <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
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
  );
};

export default CandidateProfile;
