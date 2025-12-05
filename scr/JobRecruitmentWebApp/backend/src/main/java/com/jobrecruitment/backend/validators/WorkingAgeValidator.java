package com.jobrecruitment.backend.validators;

import java.time.LocalDate;
import java.time.Period;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator Implementation for Working Age
 * Section 4.6.C - RBNS Rule
 */
public class WorkingAgeValidator implements ConstraintValidator<WorkingAge, LocalDate> {
    
    private int minAge;
    
    @Override
    public void initialize(WorkingAge constraintAnnotation) {
        this.minAge = constraintAnnotation.minAge();
    }
    
    @Override
    public boolean isValid(LocalDate birthdate, ConstraintValidatorContext context) {
        // Null values are handled by @NotNull annotation
        if (birthdate == null) {
            return true;
        }
        
        LocalDate today = LocalDate.now();
        
        // Check if birthdate is in the past
        if (!birthdate.isBefore(today)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Ngày sinh phải là ngày trong quá khứ")
                   .addConstraintViolation();
            return false;
        }
        
        // Check if candidate is of working age (CurrentYear - BirthYear >= 18)
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
