import React, { useState, useEffect } from 'react';
import { Search, Building2, Loader2 } from 'lucide-react';
import CompanyCard from '../../components/common/CompanyCard';
import companyService from '../../services/company.service';

/**
 * CompanySearchPage Component
 * Cho phép người dùng tìm kiếm và duyệt các công ty
 * 
 * Tính năng:
 * - Tìm kiếm theo tên công ty
 * - Hiển thị lưới các công ty
 * - Phân trang
 * - Hỗ trợ tiếng Việt
 */
const CompanySearchPage = () => {
  // Quản lý trạng thái
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const pageSize = 12; // Số công ty trên mỗi trang

  // Lấy danh sách công ty từ API
  const fetchCompanies = async () => {
    try {
      setLoading(true);
      setError(null);

      const params = {
        page: currentPage - 1, // API sử dụng đánh số trang bắt đầu từ 0
        size: pageSize,
        sort: 'companyName,asc',
      };

      // Thêm từ khóa nếu có
      if (searchKeyword) {
        params.keyword = searchKeyword;
      }

      const response = await companyService.searchCompanies(params);
      
      // Chuyển đổi phản hồi API sang định dạng component
      const mappedCompanies = response.data.content.map(company => ({
        companyId: company.companyId,
        name: company.companyName || 'Tên công ty',
        logo: company.logoURL || null,
        jobCount: company.jobCount || 0,
        location: company.companyAddress || 'Chưa cập nhật',
      }));

      setCompanies(mappedCompanies);
      setTotalPages(response.data.totalPages || 1);
      setTotalElements(response.data.totalElements || 0);
    } catch (err) {
      console.error('Error fetching companies:', err);
      setError('Không thể tải danh sách công ty. Vui lòng thử lại sau.');
      setCompanies([]);
    } finally {
      setLoading(false);
    }
  };

  // Lấy danh sách công ty khi component được tải hoặc khi dependencies thay đổi
  useEffect(() => {
    fetchCompanies();
  }, [currentPage, searchKeyword]);

  // Xử lý khi người dùng gửi form tìm kiếm
  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentPage(1); // Đặt lại về trang đầu tiên khi tìm kiếm mới
    fetchCompanies();
  };

  // Xử lý khi người dùng thay đổi input tìm kiếm với hiệu ứng debounce
  const handleSearchChange = (e) => {
    const value = e.target.value;
    setSearchKeyword(value);
    setCurrentPage(1); // Đặt lại về trang đầu tiên khi tìm kiếm mới
  };

  // Xử lý phân trang
  const handlePageChange = (newPage) => {
    if (newPage >= 1 && newPage <= totalPages) {
      setCurrentPage(newPage);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  // Xóa tìm kiếm
  const handleClearSearch = () => {
    setSearchKeyword('');
    setCurrentPage(1);
  };

  // Thành phần phân trang
  const Pagination = () => {
    if (totalPages <= 1) return null;

    const pages = [];
    const maxPagesToShow = 5;
    let startPage = Math.max(1, currentPage - Math.floor(maxPagesToShow / 2));
    let endPage = Math.min(totalPages, startPage + maxPagesToShow - 1);

    if (endPage - startPage < maxPagesToShow - 1) {
      startPage = Math.max(1, endPage - maxPagesToShow + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    return (
      <div className="flex justify-center items-center gap-2 mt-8">
        <button
          onClick={() => handlePageChange(currentPage - 1)}
          disabled={currentPage === 1}
          className="px-4 py-2 border border-neutral-300 rounded-lg hover:bg-neutral-100 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Trước
        </button>

        {startPage > 1 && (
          <>
            <button
              onClick={() => handlePageChange(1)}
              className="px-4 py-2 border border-neutral-300 rounded-lg hover:bg-neutral-100"
            >
              1
            </button>
            {startPage > 2 && <span className="px-2">...</span>}
          </>
        )}

        {pages.map(page => (
          <button
            key={page}
            onClick={() => handlePageChange(page)}
            className={`px-4 py-2 rounded-lg ${
              currentPage === page
                ? 'bg-primary text-white'
                : 'border border-neutral-300 hover:bg-neutral-100'
            }`}
          >
            {page}
          </button>
        ))}

        {endPage < totalPages && (
          <>
            {endPage < totalPages - 1 && <span className="px-2">...</span>}
            <button
              onClick={() => handlePageChange(totalPages)}
              className="px-4 py-2 border border-neutral-300 rounded-lg hover:bg-neutral-100"
            >
              {totalPages}
            </button>
          </>
        )}

        <button
          onClick={() => handlePageChange(currentPage + 1)}
          disabled={currentPage === totalPages}
          className="px-4 py-2 border border-neutral-300 rounded-lg hover:bg-neutral-100 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Sau
        </button>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-neutral-50 py-8">
      <div className="container-custom">
        {/* Page Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-neutral-900 mb-2">
            Khám phá công ty
          </h1>
          <p className="text-neutral-600">
            Tìm kiếm và khám phá các công ty hàng đầu
          </p>
        </div>

        {/* Search Bar */}
        <div className="bg-white p-6 rounded-lg shadow-md mb-8">
          <form onSubmit={handleSearch} className="flex gap-4">
            <div className="flex-1 relative">
              <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 text-neutral-400 w-5 h-5" />
              <input
                type="text"
                value={searchKeyword}
                onChange={handleSearchChange}
                placeholder="Tìm kiếm theo tên công ty..."
                className="w-full pl-12 pr-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
              />
            </div>
            <button
              type="submit"
              className="px-8 py-3 bg-primary text-white rounded-lg font-semibold hover:bg-primary-600 transition-all duration-200 flex items-center gap-2"
            >
              <Search className="w-5 h-5" />
              Tìm Kiếm
            </button>
            {searchKeyword && (
              <button
                type="button"
                onClick={handleClearSearch}
                className="px-6 py-3 border border-neutral-300 rounded-lg hover:bg-neutral-100 transition-all duration-200"
              >
                Xóa
              </button>
            )}
          </form>
        </div>

        {/* Results Summary */}
        <div className="mb-6">
          {loading ? (
            <p className="text-neutral-600">Đang tải...</p>
          ) : error ? (
            <p className="text-red-600">{error}</p>
          ) : (
            <p className="text-neutral-700">
              Tìm thấy <span className="font-semibold">{totalElements}</span> công ty
              {searchKeyword && (
                <span> cho từ khóa "<span className="font-semibold">{searchKeyword}</span>"</span>
              )}
            </p>
          )}
        </div>

        {/* Loading State */}
        {loading && (
          <div className="flex justify-center items-center py-20">
            <Loader2 className="w-12 h-12 text-primary animate-spin" />
          </div>
        )}

        {/* Error State */}
        {error && !loading && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
            <p className="text-red-600 mb-4">{error}</p>
            <button
              onClick={fetchCompanies}
              className="px-6 py-2 bg-primary text-white rounded-lg hover:bg-primary-600"
            >
              Thử Lại
            </button>
          </div>
        )}

        {/* Companies Grid */}
        {!loading && !error && companies.length > 0 && (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
              {companies.map(company => (
                <CompanyCard key={company.companyId} company={company} />
              ))}
            </div>

            {/* Pagination */}
            <Pagination />
          </>
        )}

        {/* Empty State */}
        {!loading && !error && companies.length === 0 && (
          <div className="bg-white rounded-lg shadow-md p-12 text-center">
            <Building2 className="w-16 h-16 text-neutral-300 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-neutral-700 mb-2">
              Không tìm thấy công ty
            </h3>
            <p className="text-neutral-600 mb-6">
              {searchKeyword
                ? `Không có công ty nào khớp với từ khóa "${searchKeyword}"`
                : 'Chưa có công ty nào trong hệ thống'}
            </p>
            {searchKeyword && (
              <button
                onClick={handleClearSearch}
                className="px-6 py-2 bg-primary text-white rounded-lg hover:bg-primary-600"
              >
                Xóa Bộ Lọc
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default CompanySearchPage;
