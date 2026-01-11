/**
 * Table Export Utilities
 * 
 * Cung cấp chức năng xuất dữ liệu bảng sang các định dạng khác nhau:
 * - Sao chép vào bộ nhớ tạm
 * - Excel (định dạng CSV)
 * - PDF (sử dụng jsPDF và jspdf-autotable)
 * 
 * Sử dụng:
 * import { copyToClipboard, exportToExcel, exportToPDF } from '@/utils/tableExport';
 * 
 * Lưu ý: Cài đặt các thư viện cần thiết nếu bạn sử dụng chức năng xuất PDF:
 * npm install jspdf jspdf-autotable
 */

/**
 * Sao chép dữ liệu bảng vào bộ nhớ tạm
 * @param {Array} data - Mảng các đối tượng đại diện cho các hàng trong bảng
 * @param {Array} columns - Mảng các định nghĩa cột { header: string, accessor: string }
 */
export const copyToClipboard = (data, columns) => {
  try {
    // Tạo hàng tiêu đề
    const headers = columns.map(col => col.header).join('\t');
    
    // Tạo các hàng dữ liệu
    const rows = data.map(row => 
      columns.map(col => {
        const value = col.accessor(row);
        return value !== null && value !== undefined ? value : '';
      }).join('\t')
    ).join('\n');
    
    const textData = headers + '\n' + rows;
    
    // Sao chép vào bộ nhớ tạm
    navigator.clipboard.writeText(textData).then(() => {
      return true;
    }).catch(err => {
      // Trợ giúp cho các trình duyệt cũ hơn
      const textArea = document.createElement('textarea');
      textArea.value = textData;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
      return true;
    });
    
    return true;
  } catch (error) {
    console.error('Error copying to clipboard:', error);
    return false;
  }
};

/**
 * Xuất dữ liệu bảng sang Excel (định dạng CSV)
 * @param {Array} data - Mảng các đối tượng đại diện cho các hàng trong bảng
 * @param {Array} columns - Mảng các định nghĩa cột { header: string, accessor: string }
 * @param {string} filename - Tên tệp xuất (không bao gồm phần mở rộng)
 */
export const exportToExcel = (data, columns, filename = 'export') => {
  try {
    // Tạo nội dung CSV
    const headers = columns.map(col => `"${col.header}"`).join(',');
    
    const rows = data.map(row => 
      columns.map(col => {
        let value = col.accessor(row);
        // Xử lý giá trị null/undefined
        if (value === null || value === undefined) value = '';
        // Đặt dấu ngoặc kép và bao quanh bằng dấu ngoặc kép
        value = String(value).replace(/"/g, '""');
        return `"${value}"`;
      }).join(',')
    ).join('\n');
    
    const csvContent = headers + '\n' + rows;
    
    // Tạo blob và tải xuống
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    
    link.setAttribute('href', url);
    link.setAttribute('download', `${filename}.csv`);
    link.style.visibility = 'hidden';
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    return true;
  } catch (error) {
    console.error('Error exporting to Excel:', error);
    return false;
  }
};

/**
 * Xuất dữ liệu bảng sang PDF
 * @param {Array} data - Mảng các đối tượng đại diện cho các hàng trong bảng
 * @param {Array} columns - Mảng các định nghĩa cột { header: string, accessor: string }
 * @param {string} filename - Tên tệp xuất (không bao gồm phần mở rộng)
 * @param {Object} options - Các tùy chọn bổ sung (tiêu đề, hướng, v.v.)
 */
export const exportToPDF = async (data, columns, filename = 'export', options = {}) => {
  try {
    // Nhập động jsPDF và autoTable
    const jsPDF = (await import('jspdf')).default;
    const autoTable = (await import('jspdf-autotable')).default;
    
    const {
      title = 'Exported Data',
      orientation = 'landscape',
      pageSize = 'a4'
    } = options;
    
    // Tạo tài liệu PDF
    const doc = new jsPDF({
      orientation: orientation,
      unit: 'mm',
      format: pageSize
    });
    
    // Thêm tiêu đề
    doc.setFontSize(16);
    doc.text(title, 14, 15);
    
    // Chuẩn bị dữ liệu bảng
    const headers = columns.map(col => col.header);
    const rows = data.map(row => 
      columns.map(col => {
        const value = col.accessor(row);
        return value !== null && value !== undefined ? String(value) : '';
      })
    );
    
    // Thêm bảng vào PDF
    autoTable(doc, {
      head: [headers],
      body: rows,
      startY: 25,
      styles: { 
        fontSize: 8,
        cellPadding: 2
      },
      headStyles: {
        fillColor: [79, 70, 229], // Indigo-600
        textColor: 255,
        fontStyle: 'bold'
      },
      alternateRowStyles: {
        fillColor: [249, 250, 251] // Gray-50
      }
    });
    
    // Lưu PDF
    doc.save(`${filename}.pdf`);
    
    return true;
  } catch (error) {
    console.error('Error exporting to PDF:', error);
    // Nếu jsPDF chưa được cài đặt, hiển thị thông báo hữu ích
    if (error.message.includes('Cannot find module')) {
      console.error('Please install jsPDF: npm install jspdf jspdf-autotable');
    }
    return false;
  }
};

/**
 * Hàm lọc tìm kiếm đơn giản cho dữ liệu bảng
 * @param {Array} data - Mảng các đối tượng để tìm kiếm
 * @param {string} searchTerm - Thuật ngữ tìm kiếm
 * @param {Array} searchFields - Mảng các tên trường để tìm kiếm trong đó
 * @returns {Array} Dữ liệu đã lọc
 */
export const searchTableData = (data, searchTerm, searchFields) => {
  if (!searchTerm || !searchTerm.trim()) return data;
  
  const term = searchTerm.toLowerCase().trim();
  
  return data.filter(item => 
    searchFields.some(field => {
      const value = field.split('.').reduce((obj, key) => obj?.[key], item);
      return value && String(value).toLowerCase().includes(term);
    })
  );
};
