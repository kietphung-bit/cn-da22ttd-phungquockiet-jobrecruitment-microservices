import React, { useState } from 'react';
import { 
  X, 
  MapPin, 
  Banknote, 
  Calendar, 
  Mail, 
  Phone, 
  User,
  Send,
  CheckCircle2
} from 'lucide-react';
import { toast } from 'react-hot-toast';

/**
 * TalentDetailModal - Modal hiển thị chi tiết ứng viên
 * 
 * Component này hiển thị thông tin chi tiết của ứng viên trong một modal popup.
 * Nhà tuyển dụng có thể xem đầy đủ thông tin và gửi lời mời ứng tuyển.
 * 
 * Props:
 * - post: Object chứa thông tin ứng viên
 * - isOpen: Boolean điều khiển hiển thị modal
 * - onClose: Function đóng modal
 * - onInvite: Function xử lý gửi lời mời (optional)
 */
const TalentDetailModal = ({ post, isOpen, onClose, onInvite }) => {
  const [isInviting, setIsInviting] = useState(false);
  const [hasInvited, setHasInvited] = useState(false);

  if (!isOpen || !post) return null;

  /**
   * Format location string
   * Handles: String, Array, JSON Array String, null/undefined
   */
  const formatLocation = (location) => {
    if (!location) return 'Toàn quốc';
    
    // Nếu đã là chuỗi (không phải chuỗi mảng JSON)
    if (typeof location === 'string') {
      // Kiểm tra xem có phải chuỗi mảng JSON không
      if (location.startsWith('[') && location.endsWith(']')) {
        try {
          const parsed = JSON.parse(location);
          if (Array.isArray(parsed) && parsed.length > 0) {
            return parsed.join(', ');
          }
        } catch (e) {
          // Nếu phân tích cú pháp thất bại, trả về nguyên bản
          return location;
        }
      }
      return location;
    }
    
    // Nếu là mảng
    if (Array.isArray(location)) {
      return location.length > 0 ? location.join(', ') : 'Toàn quốc';
    }
    
    return 'Toàn quốc';
  };

  /**
   * Định dạng mức lương
   */
  const formatSalary = (salary) => {
    if (!salary || salary === 0) return 'Thỏa thuận';
    if (typeof salary === 'string') return salary;
    return new Intl.NumberFormat('vi-VN', { 
      style: 'currency', 
      currency: 'VND' 
    }).format(salary);
  };

  /**
   * Xử lý hành động gửi lời mời ứng tuyển
   */
  const handleInviteClick = async () => {
    try {
      setIsInviting(true);
      
      // Nếu component cha cung cấp callback onInvite, sử dụng nó
      if (onInvite) {
        await onInvite(post.id);
      }
      
      // Hiển thị phản hồi thành công
      toast.success(`Đã gửi lời mời phỏng vấn thành công đến ${post.candidateName}!`);
      setHasInvited(true);
      
    } catch (error) {
      console.error('❌ Error inviting candidate:', error);
      toast.error('Gửi lời mời thất bại. Vui lòng thử lại!');
    } finally {
      setIsInviting(false);
    }
  };

  /**
   * Xử lý sự kiện click vào backdrop để đóng modal
   */
  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div 
      className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4"
      onClick={handleBackdropClick}
    >
      <div className="bg-white rounded-lg shadow-xl max-w-3xl w-full max-h-[90vh] overflow-y-auto">
        {/* Modal Header */}
        <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between z-10">
          <h2 className="text-2xl font-bold text-gray-900">Thông tin ứng viên</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-6 space-y-6">
          {/* Candidate Profile Header */}
          <div className="flex items-start gap-6">
            <div className="w-24 h-24 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
              <User className="w-12 h-12 text-blue-600" />
            </div>
            <div className="flex-1">
              <h3 className="text-2xl font-bold text-gray-900 mb-2">
                {post.candidateName}
              </h3>
              <div className="flex flex-wrap gap-4 text-sm text-gray-600">
                <div className="flex items-center gap-1">
                  <Calendar className="w-4 h-4" />
                  <span>Đăng ngày: {post.createdDate}</span>
                </div>
                {post.expiryDate && (
                  <div className="flex items-center gap-1">
                    <Calendar className="w-4 h-4" />
                    <span>Hết hạn: {post.expiryDate}</span>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Job Title */}
          <div>
            <h4 className="text-xl font-semibold text-gray-900 mb-2">
              {post.title}
            </h4>
          </div>

          {/* Key Information Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="flex items-center gap-3 p-4 bg-green-50 rounded-lg">
              <Banknote className="w-5 h-5 text-green-600 flex-shrink-0" />
              <div>
                <p className="text-xs text-gray-600 mb-1">Mức lương mong muốn</p>
                <p className="font-semibold text-green-600">
                  {formatSalary(post.desiredSalary)}
                </p>
              </div>
            </div>
            
            <div className="flex items-center gap-3 p-4 bg-blue-50 rounded-lg">
              <MapPin className="w-5 h-5 text-blue-600 flex-shrink-0" />
              <div>
                <p className="text-xs text-gray-600 mb-1">Địa điểm mong muốn</p>
                <p className="font-semibold text-blue-600">
                  {formatLocation(post.location)}
                </p>
              </div>
            </div>
          </div>

          {/* Skills Section */}
          {post.skills && post.skills.length > 0 && (
            <div>
              <h5 className="text-sm font-semibold text-gray-700 mb-3">Kỹ năng</h5>
              <div className="flex flex-wrap gap-2">
                {post.skills.map((skill, index) => (
                  <span 
                    key={index}
                    className="px-3 py-1.5 bg-blue-100 text-blue-700 text-sm font-medium rounded-full"
                  >
                    {skill}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Introduction Section */}
          <div>
            <h5 className="text-sm font-semibold text-gray-700 mb-3">Giới thiệu</h5>
            <div 
              className="prose prose-sm max-w-none text-gray-700 leading-relaxed"
              dangerouslySetInnerHTML={{ __html: post.introduction || '' }}
            />
          </div>

          {/* Contact Information */}
          {(post.candidateEmail || post.candidatePhone) && (
            <div>
              <h5 className="text-sm font-semibold text-gray-700 mb-3">Thông tin liên hệ</h5>
              <div className="bg-gray-50 rounded-lg p-4 space-y-3">
                {post.candidateEmail && (
                  <div className="flex items-center gap-3">
                    <Mail className="w-5 h-5 text-gray-400" />
                    <div>
                      <p className="text-xs text-gray-500">Email</p>
                      <a 
                        href={`mailto:${post.candidateEmail}`}
                        className="text-blue-600 hover:text-blue-700 font-medium"
                      >
                        {post.candidateEmail}
                      </a>
                    </div>
                  </div>
                )}
                {post.candidatePhone && (
                  <div className="flex items-center gap-3">
                    <Phone className="w-5 h-5 text-gray-400" />
                    <div>
                      <p className="text-xs text-gray-500">Số điện thoại</p>
                      <a 
                        href={`tel:${post.candidatePhone}`}
                        className="text-blue-600 hover:text-blue-700 font-medium"
                      >
                        {post.candidatePhone}
                      </a>
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div className="sticky bottom-0 bg-gray-50 border-t border-gray-200 px-6 py-4 flex gap-3">
          <button
            onClick={onClose}
            className="flex-1 px-6 py-3 bg-white border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 transition-colors"
          >
            Đóng
          </button>
          <button
            onClick={handleInviteClick}
            disabled={isInviting || hasInvited}
            className={`flex-1 px-6 py-3 rounded-lg font-medium flex items-center justify-center gap-2 transition-colors ${
              hasInvited
                ? 'bg-green-600 text-white cursor-not-allowed'
                : isInviting
                ? 'bg-blue-400 text-white cursor-wait'
                : 'bg-blue-600 hover:bg-blue-700 text-white'
            }`}
          >
            {hasInvited ? (
              <>
                <CheckCircle2 className="w-5 h-5" />
                Đã mời
              </>
            ) : isInviting ? (
              <>
                <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                Đang gửi...
              </>
            ) : (
              <>
                <Send className="w-5 h-5" />
                Mời ứng tuyển
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default TalentDetailModal;
