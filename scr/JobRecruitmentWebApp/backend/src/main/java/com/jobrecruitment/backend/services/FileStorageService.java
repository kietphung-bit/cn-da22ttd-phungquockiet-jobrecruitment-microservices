package com.jobrecruitment.backend.services;

import com.jobrecruitment.backend.exceptions.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * FileStorageService - Service xử lý upload và quản lý file
 * 
 * Chức năng chính:
 * 1. Upload file với validation (extension, size)
 * 2. Tạo tên file unique (UUID + original name)
 * 3. Lưu file vào thư mục uploads/{subFolder}/
 * 4. Xóa file khi entity bị xóa hoặc cập nhật
 * 
 * Cấu trúc thư mục:
 * - uploads/logos/        : Logo công ty
 * - uploads/cvs/          : CV files
 * 
 * Bảo mật:
 * - Kiểm tra extension để tránh upload file độc hại
 * - Sanitize filename để tránh path traversal attack
 * - Check file size để tránh DOS attack
 * 
 * Lưu ý:
 * - Thư mục uploads/ lưu local, không commit lên Git
 * - Production nên dùng cloud storage (AWS S3, Azure Blob, Cloudinary)
 * - Cân nhắc dùng virus scanner cho file upload
 */
@Service
@Slf4j
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-size:10485760}") // Default 10MB
    private long maxFileSize;

    // Các extension được phép
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final List<String> ALLOWED_DOCUMENT_EXTENSIONS = Arrays.asList("pdf", "docx", "doc");

    /**
     * Upload file và trả về URL để truy cập file
     * 
     * @param file MultipartFile từ request
     * @param subFolder Thư mục con (logos, cvs)
     * @return URL tương đối để truy cập file (ví dụ: uploads/logos/uuid-filename.png)
     * @throws FileStorageException Nếu file không hợp lệ hoặc lỗi khi lưu
     */
    public String storeFile(MultipartFile file, String subFolder) {
        // Bước xác minh 1: Kiểm tra file không rỗng
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File cannot be empty");
        }

        // Bước xác minh 2: Kiểm tra kích thước file
        if (file.getSize() > maxFileSize) {
            throw new FileStorageException(
                String.format("File size exceeds maximum allowed size of %d bytes", maxFileSize)
            );
        }

        // 3. Sanitization: Lấy tên file gốc và làm sạch
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        
        // 4. Bước xác minh: Kiểm tra tên file hợp lệ
        if (originalFilename.contains("..")) {
            throw new FileStorageException("Filename contains invalid path sequence: " + originalFilename);
        }

        // 5. Bước xác minh: Kiểm tra extension
        String extension = getFileExtension(originalFilename);
        validateFileExtension(extension, subFolder);

        // 6. Tính duy nhất: Tạo tên file unique với UUID
        String uniqueFilename = UUID.randomUUID().toString() + "-" + originalFilename;

        try {
            // 7. Tạo đường dẫn thư mục đích
            Path targetDirectory = Paths.get(uploadDir, subFolder);
            
            // 8. Tạo thư mục nếu chưa tồn tại
            if (!Files.exists(targetDirectory)) {
                Files.createDirectories(targetDirectory);
                log.info("Created directory: {}", targetDirectory.toAbsolutePath());
            }

            // 9. Tạo đường dẫn file đích
            Path targetLocation = targetDirectory.resolve(uniqueFilename);

            // 10. Copy file vào thư mục đích
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 11. Tạo URL truy cập (relative path)
            String fileUrl = subFolder + "/" + uniqueFilename;
            
            log.info("File uploaded successfully: {} -> {}", originalFilename, fileUrl);
            
            return fileUrl;

        } catch (IOException ex) {
            log.error("Failed to store file: {}", originalFilename, ex);
            throw new FileStorageException("Failed to store file: " + originalFilename, ex);
        }
    }

    /**
     * Xóa file khỏi hệ thống
     * 
     * @param fileUrl URL tương đối của file (ví dụ: logos/uuid-filename.png)
     * @throws FileStorageException Nếu không thể xóa file
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            log.warn("Attempted to delete file with empty URL");
            return;
        }

        try {
            // Tạo đường dẫn tuyệt đối từ URL tương đối
            Path filePath = Paths.get(uploadDir, fileUrl);

            // Kiểm tra file tồn tại
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", fileUrl);
            } else {
                log.warn("File not found for deletion: {}", fileUrl);
            }

        } catch (IOException ex) {
            log.error("Failed to delete file: {}", fileUrl, ex);
            throw new FileStorageException("Failed to delete file: " + fileUrl, ex);
        }
    }

    /**
     * Lấy extension từ tên file
     * 
     * @param filename Tên file
     * @return Extension (lowercase, không có dấu chấm)
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new FileStorageException("File must have an extension");
        }

        int lastDotIndex = filename.lastIndexOf('.');
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Validate extension dựa vào loại file (image hoặc document)
     * 
     * @param extension Extension của file
     * @param subFolder Thư mục đích (xác định loại file)
     * @throws FileStorageException Nếu extension không được phép
     */
    private void validateFileExtension(String extension, String subFolder) {
        List<String> allowedExtensions;

        // Xác định loại file dựa vào subFolder
        if ("logos".equalsIgnoreCase(subFolder)) {
            allowedExtensions = ALLOWED_IMAGE_EXTENSIONS;
        } else if ("cvs".equalsIgnoreCase(subFolder)) {
            allowedExtensions = ALLOWED_DOCUMENT_EXTENSIONS;
        } else {
            // Cho phép cả image và document cho các folder khác
            allowedExtensions = Arrays.asList(
                ALLOWED_IMAGE_EXTENSIONS.get(0), 
                ALLOWED_DOCUMENT_EXTENSIONS.get(0)
            );
        }

        // Kiểm tra extension có trong danh sách cho phép
        if (!allowedExtensions.contains(extension)) {
            throw new FileStorageException(
                String.format("File extension .%s is not allowed. Allowed extensions: %s", 
                    extension, allowedExtensions)
            );
        }
    }

    /**
     * Lấy đường dẫn tuyệt đối của file
     * (Dùng cho testing hoặc internal operations)
     * 
     * @param fileUrl URL tương đối của file
     * @return Path object
     */
    public Path getFilePath(String fileUrl) {
        return Paths.get(uploadDir, fileUrl);
    }

    /**
     * Kiểm tra file có tồn tại không
     * 
     * @param fileUrl URL tương đối của file
     * @return true nếu file tồn tại
     */
    public boolean fileExists(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return false;
        }
        
        Path filePath = Paths.get(uploadDir, fileUrl);
        return Files.exists(filePath);
    }
}
