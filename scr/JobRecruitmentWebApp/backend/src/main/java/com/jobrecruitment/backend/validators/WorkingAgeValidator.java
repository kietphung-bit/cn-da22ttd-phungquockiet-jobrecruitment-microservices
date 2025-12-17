package com.jobrecruitment.backend.validators;

import java.time.LocalDate;
import java.time.Period;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * WorkingAgeValidator - Implementation của @WorkingAge annotation
 * 
 * Mô tả:
 * - Kiểm tra ngày sinh có hợp lệ không (phải là quá khứ)
 * - Kiểm tra ứng viên đã đủ 18 tuổi chưa (hoặc minAge tùy chỉnh)
 * - Trả về custom error message cho từng trường hợp lỗi
 * 
 * Logic validate:
 * 1. Nếu birthdate == null: Pass (xử lý bởi @NotNull)
 * 2. Nếu birthdate >= today: Lỗi "Ngày sinh phải là ngày trong quá khứ"
 * 3. Nếu age < minAge: Lỗi "Ứng viên phải đủ {minAge} tuổi (hiện tại: {age} tuổi)"
 * 4. Otherwise: Pass
 * 
 * Tham khảo:
 * - Section 4.6.C - RBNS Rule (Registration Business Validation)
 * 
 * @see WorkingAge
 */
public class WorkingAgeValidator implements ConstraintValidator<WorkingAge, LocalDate> {
    
    /**
     * Tuổi tối thiểu (mặc định: 18 tuổi)
     */
    private int minAge;
    
    /**
     * Khởi tạo validator với minAge từ annotation
     * 
     * @param constraintAnnotation @WorkingAge annotation chứa minAge config
     */
    @Override
    public void initialize(WorkingAge constraintAnnotation) {
        this.minAge = constraintAnnotation.minAge();
    }
    
    /**
     * Kiểm tra ngày sinh có hợp lệ không
     * 
     * Logic:
     * 1. Nếu birthdate == null: Return true (xử lý bởi @NotNull)
     * 2. Nếu birthdate >= today: Return false (lỗi "Ngày sinh phải là quá khứ")
     * 3. Tính tuổi: age = Period.between(birthdate, today).getYears()
     * 4. Nếu age < minAge: Return false (lỗi "Phải đủ {minAge} tuổi")
     * 5. Otherwise: Return true
     * 
     * @param birthdate Ngày sinh cần validate
     * @param context ConstraintValidatorContext để build custom error message
     * @return true nếu hợp lệ, false nếu không hợp lệ
     */
    @Override
    public boolean isValid(LocalDate birthdate, ConstraintValidatorContext context) {
        // Bước 1: Nếu null thì pass (xử lý bởi @NotNull annotation)
        if (birthdate == null) {
            return true;
        }
        
        LocalDate today = LocalDate.now();
        
        // Bước 2: Kiểm tra ngày sinh phải là quá khứ (< today)
        if (!birthdate.isBefore(today)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Ngày sinh phải là ngày trong quá khứ")
                   .addConstraintViolation();
            return false;
        }
        
        // Bước 3: Kiểm tra tuổi >= minAge (mặc định 18 tuổi)
        int age = Period.between(birthdate, today).getYears();
        if (age < minAge) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                String.format("Ứng viên phải đủ %d tuổi (hiện tại: %d tuổi)", minAge, age))
                   .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
