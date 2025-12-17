package com.jobrecruitment.backend.exceptions;

/**
 * ValidationException - Nội dung exception khi dữ liệu validation thất bại
 * 
 * Mô tả:
 * - Ném khi dữ liệu không hợp lệ (business logic validation)
 * - Khác với Jakarta Validation: Đây là validation logic phức tạp
 * - GlobalExceptionHandler xử lý và trả về HTTP 400 BAD REQUEST
 * 
 * Trường hợp sử dụng:
 * - Email đã tồn tại (duplicate check)
 * - Phạm vi lương không hợp lệ (minSalary > maxSalary)
 * - Định dạng số điện thoại sai (không phải 10-11 số)
 * - Ngày không hợp lệ (StartDate > EndDate)
 * - Người dùng chưa đủ 18 tuổi (WorkingAge validator)
 * 
 * Fields:
 * - field: Tên field bị lỗi (optional)
 * - rejectedValue: Giá trị bị từ chối (optional)
 * - message: Thông báo lỗi
 */
public class ValidationException extends RuntimeException {
    
    /**
     * Tên field bị lỗi (ví dụ: "email", "minSalary")
     */
    private String field;
    
    /**
     * Giá trị bị từ chối (ví dụ: "invalid@email", -1000)
     */
    private Object rejectedValue;
    
    /**
     * Constructor đơn giản: Chỉ có message
     * 
     * Sử dụng:
     * - throw new ValidationException("Email already exists");
     * - throw new ValidationException("Invalid salary range");
     * 
     * @param message Thông báo lỗi
     */
    public ValidationException(String message) {
        super(message);
    }
    
    /**
     * Constructor chi tiết: Bao gồm field và rejected value
     * 
     * Sử dụng:
     * - throw new ValidationException("email", "test@test.com", "Email already exists");
     * - throw new ValidationException("minSalary", -1000, "Salary must be positive");
     * 
     * @param field Tên field bị lỗi
     * @param rejectedValue Giá trị bị từ chối
     * @param message Thông báo lỗi
     */
    public ValidationException(String field, Object rejectedValue, String message) {
        super(message);
        this.field = field;
        this.rejectedValue = rejectedValue;
    }
    
    public String getField() {
        return field;
    }
    
    public Object getRejectedValue() {
        return rejectedValue;
    }
}
