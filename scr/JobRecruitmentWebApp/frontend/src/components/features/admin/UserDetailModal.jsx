import React from 'react';
import { X, Mail, Phone, MapPin, Building2, User, Briefcase, Calendar, Shield } from 'lucide-react';

/**
 * UserDetailModal - Modal hiển thị chi tiết người dùng
 * 
 * Component này hiển thị thông tin đầy đủ của người dùng (Candidate hoặc Employer)
 * 
 * Props:
 * - user: Object chứa thông tin người dùng
 * - isOpen: Boolean điều khiển hiển thị modal
 * - onClose: Function đóng modal
 */
const UserDetailModal = ({ user, isOpen, onClose }) => {
  if (!isOpen || !user) return null;

  const isCandidate = user.roleCode === 'UV';
  const isEmployer = user.roleCode === 'DN';

  /**
   * Xử lý click vào backdrop để đóng modal
   */
  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  /**
   * Định dạng ngày
   */
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('vi-VN');
    } catch (e) {
      return dateString;
    }
  };

  /**
   * Lấy tên hiển thị của vai trò
   */
  const getRoleName = (roleCode) => {
    const roleMap = {
      'ADM': 'Quản trị viên',
      'DN': 'Nhà tuyển dụng',
      'UV': 'Ứng viên'
    };
    return roleMap[roleCode] || roleCode;
  };

  /**
   * Lấy badge trạng thái tài khoản
   */
  const getStatusBadge = () => {
    if (user.locked) {
      return (
        <span className="inline-flex items-center gap-1 px-3 py-1 bg-red-100 text-red-700 text-sm font-medium rounded-full">
          <Shield className="w-4 h-4" />
          Đã khóa
        </span>
      );
    }
    return (
      <span className="inline-flex items-center gap-1 px-3 py-1 bg-green-100 text-green-700 text-sm font-medium rounded-full">
        <Shield className="w-4 h-4" />
        Hoạt động
      </span>
    );
  };

  return (
    <div 
      className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4"
      onClick={handleBackdropClick}
    >
      <div className="bg-white rounded-lg shadow-xl max-w-3xl w-full max-h-[90vh] overflow-y-auto">
        {/* Modal Header */}
        <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between z-10">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Thông tin người dùng</h2>
            <p className="text-sm text-gray-500 mt-1">Mã: {user.userCode}</p>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-6 space-y-6">
          {/* Profile Header */}
          <div className="flex items-start gap-6">
            <div className="w-20 h-20 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
              {isEmployer ? (
                <Building2 className="w-10 h-10 text-blue-600" />
              ) : (
                <User className="w-10 h-10 text-blue-600" />
              )}
            </div>
            <div className="flex-1">
              <h3 className="text-xl font-bold text-gray-900 mb-2">
                {user.name || user.username}
              </h3>
              <div className="flex flex-wrap gap-3 mb-3">
                <span className="inline-flex items-center gap-1 px-3 py-1 bg-blue-100 text-blue-700 text-sm font-medium rounded-full">
                  {getRoleName(user.roleCode)}
                </span>
                {getStatusBadge()}
              </div>
            </div>
          </div>

          {/* Basic Information */}
          <div>
            <h4 className="text-lg font-semibold text-gray-900 mb-4">Thông tin cơ bản</h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="flex items-start gap-3">
                <Mail className="w-5 h-5 text-gray-400 mt-0.5 flex-shrink-0" />
                <div>
                  <p className="text-xs text-gray-500 mb-1">Email</p>
                  <p className="text-gray-900 font-medium break-all">
                    {user.email || user.username}
                  </p>
                </div>
              </div>

              {user.phone && (
                <div className="flex items-start gap-3">
                  <Phone className="w-5 h-5 text-gray-400 mt-0.5 flex-shrink-0" />
                  <div>
                    <p className="text-xs text-gray-500 mb-1">Số điện thoại</p>
                    <p className="text-gray-900 font-medium">{user.phone}</p>
                  </div>
                </div>
              )}

              {user.createdDate && (
                <div className="flex items-start gap-3">
                  <Calendar className="w-5 h-5 text-gray-400 mt-0.5 flex-shrink-0" />
                  <div>
                    <p className="text-xs text-gray-500 mb-1">Ngày tạo</p>
                    <p className="text-gray-900 font-medium">{formatDate(user.createdDate)}</p>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Employer Specific Info */}
          {isEmployer && (
            <>
              {user.companyName && (
                <div>
                  <h4 className="text-lg font-semibold text-gray-900 mb-4">Thông tin công ty</h4>
                  <div className="bg-blue-50 rounded-lg p-4 space-y-3">
                    <div className="flex items-start gap-3">
                      <Building2 className="w-5 h-5 text-blue-600 mt-0.5 flex-shrink-0" />
                      <div>
                        <p className="text-xs text-blue-600 mb-1">Tên công ty</p>
                        <p className="text-gray-900 font-semibold">{user.companyName}</p>
                      </div>
                    </div>
                    
                    {user.companyWebsite && (
                      <div className="flex items-start gap-3">
                        <MapPin className="w-5 h-5 text-blue-600 mt-0.5 flex-shrink-0" />
                        <div>
                          <p className="text-xs text-blue-600 mb-1">Website</p>
                          <a 
                            href={user.companyWebsite}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-blue-600 hover:text-blue-700 font-medium hover:underline"
                          >
                            {user.companyWebsite}
                          </a>
                        </div>
                      </div>
                    )}

                    {user.companyAddress && (
                      <div className="flex items-start gap-3">
                        <MapPin className="w-5 h-5 text-blue-600 mt-0.5 flex-shrink-0" />
                        <div>
                          <p className="text-xs text-blue-600 mb-1">Địa chỉ</p>
                          <p className="text-gray-900">{user.companyAddress}</p>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </>
          )}

          {/* Candidate Specific Info */}
          {isCandidate && (
            <>
              {(user.skills || user.experience) && (
                <div>
                  <h4 className="text-lg font-semibold text-gray-900 mb-4">Thông tin nghề nghiệp</h4>
                  <div className="bg-green-50 rounded-lg p-4 space-y-3">
                    {user.skills && (
                      <div className="flex items-start gap-3">
                        <Briefcase className="w-5 h-5 text-green-600 mt-0.5 flex-shrink-0" />
                        <div className="flex-1">
                          <p className="text-xs text-green-600 mb-2">Kỹ năng</p>
                          <p className="text-gray-900 whitespace-pre-line">{user.skills}</p>
                        </div>
                      </div>
                    )}

                    {user.experience && (
                      <div className="flex items-start gap-3">
                        <Calendar className="w-5 h-5 text-green-600 mt-0.5 flex-shrink-0" />
                        <div className="flex-1">
                          <p className="text-xs text-green-600 mb-2">Kinh nghiệm</p>
                          <p className="text-gray-900 whitespace-pre-line">{user.experience}</p>
                        </div>
                      </div>
                    )}

                    {user.education && (
                      <div className="flex items-start gap-3">
                        <User className="w-5 h-5 text-green-600 mt-0.5 flex-shrink-0" />
                        <div className="flex-1">
                          <p className="text-xs text-green-600 mb-2">Học vấn</p>
                          <p className="text-gray-900 whitespace-pre-line">{user.education}</p>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </>
          )}

          {/* Account Status */}
          <div>
            <h4 className="text-lg font-semibold text-gray-900 mb-4">Trạng thái tài khoản</h4>
            <div className="bg-gray-50 rounded-lg p-4">
              <div className="flex items-center gap-3">
                <Shield className={`w-6 h-6 ${user.locked ? 'text-red-600' : 'text-green-600'}`} />
                <div>
                  <p className="text-sm text-gray-600">
                    {user.locked ? (
                      <span className="text-red-600 font-semibold">
                        Tài khoản này đã bị khóa. Người dùng không thể đăng nhập vào hệ thống.
                      </span>
                    ) : (
                      <span className="text-green-600 font-semibold">
                        Tài khoản đang hoạt động bình thường. Người dùng có thể đăng nhập.
                      </span>
                    )}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="sticky bottom-0 bg-gray-50 border-t border-gray-200 px-6 py-4">
          <button
            onClick={onClose}
            className="w-full px-6 py-3 bg-white border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 transition-colors"
          >
            Đóng
          </button>
        </div>
      </div>
    </div>
  );
};

export default UserDetailModal;
