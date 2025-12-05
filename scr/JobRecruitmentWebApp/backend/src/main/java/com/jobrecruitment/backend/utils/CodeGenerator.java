package com.jobrecruitment.backend.utils;

import java.security.SecureRandom;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

/**
 * Code Generator Utility
 * Implements Section 4.5.A - Generation Algorithm
 * Format: PREFIX + 8 DIGITS (Total: 10 characters)
 * 
 * Logic:
 * 1. Generate random integer between 0 and 99999999
 * 2. Pad with leading zeros to ensure 8 digits
 * 3. Check for uniqueness in database
 * 4. Concatenate PREFIX + Padded Number
 */
@Component
public class CodeGenerator {
    
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_ATTEMPTS = 100;
    private static final int CODE_LENGTH = 8;
    private static final int MAX_VALUE = 99999999;
    
    // Prefix Definitions (Section 4.5.B)
    public static final String PREFIX_ADMIN = "AD";
    public static final String PREFIX_COMPANY = "DN";
    public static final String PREFIX_CANDIDATE = "UV";
    public static final String PREFIX_JOB = "VL";
    public static final String PREFIX_CV = "CV";
    public static final String PREFIX_APPLICATION = "DX";
    
    /**
     * Generate a unique code with the specified prefix
     * 
     * @param prefix The code prefix (DN, UV, VL, CV, DX)
     * @param uniquenessChecker Predicate to check if code already exists in database
     * @return Generated unique code (10 characters)
     * @throws IllegalStateException if unable to generate unique code after MAX_ATTEMPTS
     */
    public String generateCode(String prefix, Predicate<String> uniquenessChecker) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = generateCodeOnce(prefix);
            
            // Check uniqueness
            if (!uniquenessChecker.test(code)) {
                return code;
            }
        }
        
        throw new IllegalStateException(
            String.format("Failed to generate unique code with prefix '%s' after %d attempts", 
                prefix, MAX_ATTEMPTS)
        );
    }
    
    /**
     * Generate a single code without uniqueness check
     * 
     * @param prefix The code prefix
     * @return Generated code
     */
    private String generateCodeOnce(String prefix) {
        // Generate random integer between 0 and 99999999
        int randomNumber = RANDOM.nextInt(MAX_VALUE + 1);
        
        // Pad with leading zeros to ensure 8 digits
        String paddedNumber = String.format("%0" + CODE_LENGTH + "d", randomNumber);
        
        // Concatenate PREFIX + Padded Number
        return prefix + paddedNumber;
    }
    
    /**
     * Generate Admin Code (Special Case - Section 4.5.C)
     * 
     * @return Fixed admin code "AD00000001"
     */
    public String generateAdminCode() {
        return PREFIX_ADMIN + "00000001";
    }
    
    /**
     * Generate Company Code
     * Format: DN + 8 digits
     * 
     * @param uniquenessChecker Predicate to check if code exists
     * @return Generated unique company code
     */
    public String generateCompanyCode(Predicate<String> uniquenessChecker) {
        return generateCode(PREFIX_COMPANY, uniquenessChecker);
    }
    
    /**
     * Generate Candidate Code
     * Format: UV + 8 digits
     * 
     * @param uniquenessChecker Predicate to check if code exists
     * @return Generated unique candidate code
     */
    public String generateCandidateCode(Predicate<String> uniquenessChecker) {
        return generateCode(PREFIX_CANDIDATE, uniquenessChecker);
    }
    
    /**
     * Generate Job Code
     * Format: VL + 8 digits
     * 
     * @param uniquenessChecker Predicate to check if code exists
     * @return Generated unique job code
     */
    public String generateJobCode(Predicate<String> uniquenessChecker) {
        return generateCode(PREFIX_JOB, uniquenessChecker);
    }
    
    /**
     * Generate CV Code
     * Format: CV + 8 digits
     * 
     * @param uniquenessChecker Predicate to check if code exists
     * @return Generated unique CV code
     */
    public String generateCVCode(Predicate<String> uniquenessChecker) {
        return generateCode(PREFIX_CV, uniquenessChecker);
    }
    
    /**
     * Generate Application Code
     * Format: DX + 8 digits
     * 
     * @param uniquenessChecker Predicate to check if code exists
     * @return Generated unique application code
     */
    public String generateApplicationCode(Predicate<String> uniquenessChecker) {
        return generateCode(PREFIX_APPLICATION, uniquenessChecker);
    }
}
