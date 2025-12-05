package com.jobrecruitment.backend.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Custom Validator Annotation for Working Age
 * Section 4.6.C - RBNS Rule
 * 
 * Validates that:
 * 1. Birthdate is in the past (< Current Date)
 * 2. Candidate is of working age (CurrentYear - BirthYear >= 18)
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
