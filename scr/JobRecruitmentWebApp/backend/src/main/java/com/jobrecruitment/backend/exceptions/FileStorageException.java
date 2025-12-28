package com.jobrecruitment.backend.exceptions;

/**
 * FileStorageException - Custom exception cho các lỗi liên quan đến file storage
 * 
 * Sử dụng cho:
 * - File upload failed
 * - Invalid file extension
 * - File size exceeds limit
 * - File not found for deletion
 * - IO errors khi đọc/ghi file
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
