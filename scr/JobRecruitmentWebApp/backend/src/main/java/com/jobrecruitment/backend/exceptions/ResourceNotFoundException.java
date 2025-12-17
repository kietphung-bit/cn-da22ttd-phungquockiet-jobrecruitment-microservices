package com.jobrecruitment.backend.exceptions;

/**
 * ResourceNotFoundException - Nội dung exception khi không tìm thấy resource
 * 
 * Mô tả:
 * - Ném khi không tìm thấy resource trong database
 * - GlobalExceptionHandler xử lý và trả về HTTP 404 NOT FOUND
 * - Auto-generate message: "User not found with userId: '123'"
 * 
 * Trường hợp sử dụng:
 * - Tìm job theo ID nhưng không tồn tại
 * - Tìm company theo ID nhưng đã bị xóa
 * - Tìm user theo username nhưng không có trong database
 * - Tìm candidate theo candidateCode nhưng không tìm thấy
 * 
 * Fields:
 * - resourceName: Tên resource (ví dụ: "User", "Job", "Company")
 * - fieldName: Tên field tìm kiếm (ví dụ: "userId", "jobCode")
 * - fieldValue: Giá trị tìm kiếm (ví dụ: 123, "JOB12345678")
 */
public class ResourceNotFoundException extends RuntimeException {
    
    /**
     * Tên resource không tìm thấy (ví dụ: "User", "Job", "Company")
     */
    private String resourceName;
    
    /**
     * Tên field tìm kiếm (ví dụ: "userId", "jobCode", "username")
     */
    private String fieldName;
    
    /**
     * Giá trị tìm kiếm (ví dụ: 123, "JOB12345678", "admin@example.com")
     */
    private Object fieldValue;
    
    /**
     * Constructor chi tiết: Auto-generate message
     * 
     * Sử dụng:
     * - throw new ResourceNotFoundException("User", "userId", 123);
     *   -> Message: "User not found with userId: '123'"
     * - throw new ResourceNotFoundException("Job", "jobCode", "JOB12345678");
     *   -> Message: "Job not found with jobCode: 'JOB12345678'"
     * 
     * @param resourceName Tên resource (User, Job, Company...)
     * @param fieldName Tên field tìm kiếm (userId, jobCode...)
     * @param fieldValue Giá trị tìm kiếm
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
    
    /**
     * Constructor đơn giản: Custom message
     * 
     * Sử dụng:
     * - throw new ResourceNotFoundException("User not found");
     * - throw new ResourceNotFoundException("Job has been deleted");
     * 
     * @param message Thông báo lỗi tùy chỉnh
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public Object getFieldValue() {
        return fieldValue;
    }
}
