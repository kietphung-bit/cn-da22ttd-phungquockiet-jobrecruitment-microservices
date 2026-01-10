package com.jobrecruitment.backend.utils;

import java.security.SecureRandom;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

/**
 * CodeGenerator - Utility tạo mã tự động cho các Entity
 * 
 * Tham khảo: Section 4.5.A - Generation Algorithm, Section 4.5.B - Prefix Definitions
 * 
 * Định dạng mã: PREFIX + 8 CHỮ SỐ (Tổng cộng: 10 ký tự)
 * Ví dụ:
 * - Admin: AD00000001 (Cố định)
 * - Company: DN12345678
 * - Candidate: UV98765432
 * - Job: VL00112233
 * - CV: CV55667788
 * - Application: DX11223344
 * 
 * Thuật toán tạo mã:
 * 1. Tạo số ngẫu nhiên: SecureRandom.nextInt(0 đến 99999999)
 * 2. Độn số 0 đầu: String.format("%08d", randomNumber) -> Đảm bảo đủ 8 chữ số
 * 3. Kiểm tra tính duy nhất: Gọi uniquenessChecker (Predicate<String>)
 * 4. Nối chuỗi: PREFIX + PaddedNumber
 * 5. Nếu trùng lặp: Tạo lại (tối đa MAX_ATTEMPTS = 100 lần)
 * 
 * Kết quả:
 * - Mã duy nhất, không trùng lặp trong database
 * - Dễ đọc, dễ phân biệt theo PREFIX
 * - An toàn với SecureRandom (không dự đoán)
 * 
 * Danh sách PREFIX được sử dụng:
 * - AD: Admin (Cố định "AD00000001" cho Root Admin)
 * - DN: Doanh nghiệp (Company/Employer)
 * - UV: Ứng viên (Candidate/Job Seeker)
 * - VL: Việc làm (Job Posting)
 * - CV: Hồ sơ xin việc (Curriculum Vitae)
 * - DX: Đơn xét (Application)
 * - BV: Bài viết tìm việc (Seeking Post)
 * 
 * Lưu ý quan trọng:
 * - UserCode đồng bộ với CompanyCode/CandidateCode (Section 4.5.C)
 * - Admin luôn có mã cố định "AD00000001"
 * - Các mã khác được tạo ngẫu nhiên và kiểm tra tính duy nhất
 */
@Component
public class CodeGenerator {
    
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_ATTEMPTS = 100;
    private static final int CODE_LENGTH = 8;
    private static final int MAX_VALUE = 99999999;
    
    // Định nghĩa mã PREFIX 
    public static final String PREFIX_ADMIN = "AD";
    public static final String PREFIX_COMPANY = "DN";
    public static final String PREFIX_CANDIDATE = "UV";
    public static final String PREFIX_JOB = "VL";
    public static final String PREFIX_CV = "CV";
    public static final String PREFIX_APPLICATION = "DX";
    public static final String PREFIX_SEEKING_POST = "BV";
    
    /**
     * Tạo mã duy nhất với PREFIX chỉ định
     * 
     * Phương thức chính tạo mã cho tất cả các Entity (ngoại trừ Admin)
     * 
     * Thuật toán chi tiết:
     * 1. Bắt đầu vòng lặp tối đa MAX_ATTEMPTS (100 lần)
     * 2. Gọi generateCodeOnce(prefix) để tạo mã ngẫu nhiên
     *    - Gọi SecureRandom.nextInt(MAX_VALUE + 1) -> Số ngẫu nhiên từ 0-99999999
     *    - Độn số 0: String.format("%08d", randomNumber) -> Ví dụ: 55 -> "00000055"
     *    - Nối PREFIX + PaddedNumber -> Ví dụ: "DN" + "00000055" = "DN00000055"
     * 3. Kiểm tra tính duy nhất: uniquenessChecker.test(code)
     *    - uniquenessChecker là Predicate<String> được truyền vào từ Repository
     *    - Trả về true nếu mã đã tồn tại (trùng lặp)
     *    - Trả về false nếu mã chưa tồn tại (duy nhất)
     * 4. Nếu mã duy nhất (!uniquenessChecker.test(code)): Trả về mã
     * 5. Nếu mã trùng lặp: Tiếp tục vòng lặp, tạo mã mới
     * 6. Nếu sau MAX_ATTEMPTS vẫn không tạo được: Ném IllegalStateException
     * 
     * Ví dụ sử dụng:
     * <pre>
     * String companyCode = codeGenerator.generateCode(
     *     CodeGenerator.PREFIX_COMPANY,
     *     code -> companyRepository.existsByCompanyCode(code)
     * );
     * // Kết quả: "DN12345678" (duy nhất trong database)
     * </pre>
     * 
     * @param prefix PREFIX mã (DN, UV, VL, CV, DX)
     * @param uniquenessChecker Predicate kiểm tra mã đã tồn tại trong database (true = trùng, false = duy nhất)
     * @return Mã duy nhất (10 ký tự: PREFIX + 8 chữ số)
     * @throws IllegalStateException Nếu không tạo được mã duy nhất sau MAX_ATTEMPTS lần
     */
    public String generateCode(String prefix, Predicate<String> uniquenessChecker) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = generateCodeOnce(prefix);
            
            // Kiểm tra tính duy nhất
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
     * Tạo một mã duy nhất không kiểm tra tính duy nhất
     * 
     * Phương thức nội bộ (private) để tạo mã ngẫu nhiên
     * 
     * Quy trình:
     * 1. Tạo số ngẫu nhiên: RANDOM.nextInt(MAX_VALUE + 1)
     *    - MAX_VALUE = 99999999
     *    - Kết quả: Số từ 0 đến 99999999
     *    - Ví dụ: 12345678, 55, 99999999
     * 2. Độn số 0 đầu: String.format("%0" + CODE_LENGTH + "d", randomNumber)
     *    - CODE_LENGTH = 8
     *    - Ví dụ: 55 -> "00000055", 12345678 -> "12345678"
     * 3. Nối PREFIX + PaddedNumber
     *    - Ví dụ: "DN" + "00000055" = "DN00000055"
     * 
     * Lưu ý: Phương thức này KHÔNG kiểm tra tính duy nhất. Việc kiểm tra được thực hiện ở generateCode()
     * 
     * @param prefix PREFIX mã (DN, UV, VL, CV, DX)
     * @return Mã đã tạo (10 ký tự, chưa kiểm tra tính duy nhất)
     */
    private String generateCodeOnce(String prefix) {
        // Tạo số ngẫu nhiên từ 0 đến 99999999
        int randomNumber = RANDOM.nextInt(MAX_VALUE + 1);
        
        // Độn số 0 đầu để đảm bảo 8 chữ số
        String paddedNumber = String.format("%0" + CODE_LENGTH + "d", randomNumber);
        
        // Nối PREFIX + Padded Number
        return prefix + paddedNumber;
    }
    
    /**
     * Tạo mã Admin (Trường hợp đặc biệt - Section 4.5.C)
     * 
     * Lưu ý quan trọng:
     * - Mã Admin là CỐ ĐỊNH, KHÔNG ngẫu nhiên
     * - Luôn trả về "AD00000001" cho Root Admin
     * - Nếu cần nhiều Admin: Có thể tạo tuần tự (AD00000002, AD00000003...)
     * - Trong hệ thống hiện tại: Chỉ có 1 Root Admin duy nhất
     * 
     * Tác dụng:
     * - Đảm bảo Root Admin có mã cố định, dễ nhận biết
     * - Không bị thay đổi giữa các lần khởi tạo hệ thống
     * - Dùng trong DataSeeder khi seed dữ liệu ban đầu
     * 
     * @return Mã Admin cố định "AD00000001"
     */
    public String generateAdminCode() {
        return PREFIX_ADMIN + "00000001";
    }
    
    /**
     * Tạo mã Doanh nghiệp (Company Code)
     * 
     * Định dạng: DN + 8 chữ số (Ví dụ: DN12345678)
     * 
     * Lưu ý quan trọng (Section 4.5.C - UserCode Synchronization):
     * - CompanyCode phải đồng bộ với UserCode
     * - Quy trình đăng ký Employer:
     *   1. Tạo CompanyCode: DN12345678
     *   2. Lưu Company với CompanyCode = DN12345678
     *   3. Lưu User với UserCode = DN12345678 (GIỐNG CompanyCode)
     * - Mục đích: Dễ dàng liên kết User <-> Company qua mã duy nhất
     * 
     * Ví dụ sử dụng:
     * <pre>
     * String companyCode = codeGenerator.generateCompanyCode(
     *     code -> companyRepository.existsByCompanyCode(code)
     * );
     * company.setCompanyCode(companyCode);
     * user.setUserCode(companyCode); // Đồng bộ UserCode = CompanyCode
     * </pre>
     * 
     * @param uniquenessChecker Predicate kiểm tra mã đã tồn tại (true = trùng, false = duy nhất)
     * @return Mã Doanh nghiệp duy nhất (10 ký tự: DN + 8 chữ số)
     */
    public String generateCompanyCode(Predicate<String> uniquenessChecker) {
        return generateCode(PREFIX_COMPANY, uniquenessChecker);
    }
    
    /**
     * Tạo mã Ứng viên (Candidate Code)
     * 
     * Định dạng: UV + 8 chữ số (Ví dụ: UV98765432)
     * 
     * Lưu ý quan trọng (Section 4.5.C - UserCode Synchronization):
     * - CandidateCode phải đồng bộ với UserCode
     * - Quy trình đăng ký Candidate:
     *   1. Tạo CandidateCode: UV98765432
     *   2. Lưu Candidate với CandidateCode = UV98765432
     *   3. Lưu User với UserCode = UV98765432 (GIỐNG CandidateCode)
     * - Mục đích: Dễ dàng liên kết User <-> Candidate qua mã duy nhất
     * 
     * Ví dụ sử dụng:
     * <pre>
     * String candidateCode = codeGenerator.generateCandidateCode(
     *     code -> candidateRepository.existsByCandidateCode(code)
     * );
     * candidate.setCandidateCode(candidateCode);
     * user.setUserCode(candidateCode); // Đồng bộ UserCode = CandidateCode
     * </pre>
     * 
     * @param uniquenessChecker Predicate kiểm tra mã đã tồn tại (true = trùng, false = duy nhất)
     * @return Mã Ứng viên duy nhất (10 ký tự: UV + 8 chữ số)
     */
    public String generateCandidateCode(Predicate<String> uniquenessChecker) {
        return generateCode(PREFIX_CANDIDATE, uniquenessChecker);
    }
    
    /**
     * Tạo mã Công việc (Job Code)
     * 
     * Định dạng: VL + 8 chữ số (Ví dụ: VL00112233)
     * Tác dụng: Nhận diện duy nhất mỗi tin tuyển dụng
     * 
     * @param uniquenessChecker Predicate kiểm tra mã đã tồn tại (true = trùng, false = duy nhất)
     * @return Mã Công việc duy nhất (10 ký tự: VL + 8 chữ số)
     */
    public String generateJobCode(Predicate<String> uniquenessChecker) {
        return generateCode(PREFIX_JOB, uniquenessChecker);
    }
    
    /**
     * Tạo mã CV (CV Code)
     * 
     * Định dạng: CV + 8 chữ số (Ví dụ: CV55667788)
     * Tác dụng: Nhận diện duy nhất mỗi hồ sơ xin việc của ứng viên
     * Lưu ý: Mỗi ứng viên có thể có nhiều CV với các CVCode khác nhau
     * 
     * @param uniquenessChecker Predicate kiểm tra mã đã tồn tại (true = trùng, false = duy nhất)
     * @return Mã CV duy nhất (10 ký tự: CV + 8 chữ số)
     */
    public String generateCVCode(Predicate<String> uniquenessChecker) {
        return generateCode(PREFIX_CV, uniquenessChecker);
    }
    
    /**
     * Tạo mã Đơn ứng tuyển (Application Code)
     * 
     * Định dạng: DX + 8 chữ số (Ví dụ: DX11223344)
     * Tác dụng: Nhận diện duy nhất mỗi đơn ứng tuyển của ứng viên vào công việc
     * Lưu ý: Mỗi lần ứng tuyển tạo ra 1 ApplicationCode mới
     * 
     * @param uniquenessChecker Predicate kiểm tra mã đã tồn tại (true = trùng, false = duy nhất)
     * @return Mã Đơn ứng tuyển duy nhất (10 ký tự: DX + 8 chữ số)
     */
    public String generateApplicationCode(Predicate<String> uniquenessChecker) {
        return generateCode(PREFIX_APPLICATION, uniquenessChecker);
    }
}
