package com.jobrecruitment.backend.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * @interface WorkingAge - Custom annotation để validate tuổi lao động
 * 
 * Mô tả:
 * - Đánh dấu field cần validate tuổi lao động
 * - Sử dụng với Jakarta Validation (@Valid)
 * - Implementation: WorkingAgeValidator
 * 
 * Quy tắc validate:
 * 1. Ngày sinh phải là ngày quá khứ (< Current Date)
 * 2. Ứng viên phải đủ 18 tuổi (CurrentYear - BirthYear >= 18)
 * 
 * Sử dụng:
 * - @WorkingAge private LocalDate candidateBirthDate;
 * - @WorkingAge(minAge = 21) private LocalDate birthDate;
 * 
 * @see WorkingAgeValidator
 */
@Documented
@Constraint(validatedBy = WorkingAgeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface WorkingAge {
    
    String message() default "Ứng viên phải đủ 18 tuổi";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    int minAge() default 18;
}
