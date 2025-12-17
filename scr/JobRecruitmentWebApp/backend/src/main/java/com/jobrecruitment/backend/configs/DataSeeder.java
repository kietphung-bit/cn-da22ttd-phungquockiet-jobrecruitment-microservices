package com.jobrecruitment.backend.configs;

import com.jobrecruitment.backend.entities.*;
import com.jobrecruitment.backend.enums.*;
import com.jobrecruitment.backend.repositories.*;
import com.jobrecruitment.backend.utils.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Thành phần Data Seeding
 * Tạo dữ liệu kiểm thử toàn diện vào cơ sở dữ liệu khi khởi động ứng dụng
 * 
 * Dữ liệu được tạo bao gồm:
 * - 3 Roles (ADM, DN, UV)
 * - 1 Admin User
 * - 3 Job Categories (IT, Marketing, Sales)
 * - 2 Employers with 2 Jobs each
 * - 2 Candidates
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CandidateRepository candidateRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final JobRepository jobRepository;
    private final CVRepository cvRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodeGenerator codeGenerator;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedAdmin();
        seedJobCategories();
        seedEmployersAndJobs();
        seedCandidates();
        seedCVs();
        
        log.info("=== DATA SEEDING COMPLETED ===");
        log.info("Total Users: {}", userRepository.count());
        log.info("Total Companies: {}", companyRepository.count());
        log.info("Total Candidates: {}", candidateRepository.count());
        log.info("Total Job Categories: {}", jobCategoryRepository.count());
        log.info("Total Jobs: {}", jobRepository.count());
        log.info("Total CVs: {}", cvRepository.count());
    }

    private void seedRoles() {
        if (roleRepository.count() > 0) {
            log.info("Roles already seeded. Skipping...");
            return;
        }

        log.info("Seeding roles...");

        Role adminRole = new Role();
        adminRole.setRoleCode("ADM");
        adminRole.setRoleName("Quản trị viên");
        roleRepository.save(adminRole);

        Role companyRole = new Role();
        companyRole.setRoleCode("DN");
        companyRole.setRoleName("Nhà tuyển dụng");
        roleRepository.save(companyRole);

        Role candidateRole = new Role();
        candidateRole.setRoleCode("UV");
        candidateRole.setRoleName("Ứng viên");
        roleRepository.save(candidateRole);

        log.info("✓ Created 3 roles");
    }

    private void seedAdmin() {
        if (userRepository.findByUsername("admin@jobrecruitment.com").isPresent()) {
            log.info("Admin already exists. Skipping...");
            return;
        }

        log.info("Seeding admin user...");

        Role adminRole = roleRepository.findByRoleCode("ADM")
                .orElseThrow(() -> new RuntimeException("ADM role not found"));

        User admin = new User();
        admin.setUserCode("AD00000001");
        admin.setUsername("admin@jobrecruitment.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(adminRole);
        userRepository.save(admin);

        log.info("✓ Created Admin User - Username: admin@jobrecruitment.com | Password: admin123");
    }

    private void seedJobCategories() {
        if (jobCategoryRepository.count() > 0) {
            log.info("Job categories already seeded. Skipping...");
            return;
        }

        log.info("Seeding job categories...");

        JobCategory itCategory = new JobCategory();
        itCategory.setJcName("Công nghệ thông tin");
        itCategory.setJcDescription("Phát triển phần mềm, Lập trình, Hệ thống IT và Công nghệ");
        itCategory.setJcBaseSalary(15000000.0);
        jobCategoryRepository.save(itCategory);

        JobCategory marketingCategory = new JobCategory();
        marketingCategory.setJcName("Truyền thông & Marketing");
        marketingCategory.setJcDescription("Marketing, Quảng cáo, Truyền thông và Quản lý thương hiệu");
        marketingCategory.setJcBaseSalary(12000000.0);
        jobCategoryRepository.save(marketingCategory);

        JobCategory salesCategory = new JobCategory();
        salesCategory.setJcName("Kinh doanh & Bán hàng");
        salesCategory.setJcDescription("Kinh doanh, Phát triển thị trường và Quan hệ khách hàng");
        salesCategory.setJcBaseSalary(10000000.0);
        jobCategoryRepository.save(salesCategory);

        log.info("✓ Created 3 job categories");
    }

    private void seedEmployersAndJobs() {
        if (companyRepository.count() > 0) {
            log.info("Employers already seeded. Skipping...");
            return;
        }

        log.info("Seeding employers and jobs...");

        Role companyRole = roleRepository.findByRoleCode("DN")
                .orElseThrow(() -> new RuntimeException("DN role not found"));

        // Employer 1: Tech Corp
        String companyCode1 = codeGenerator.generateCode("DN", code -> 
                companyRepository.findByCompanyCode(code).isPresent());
        
        User user1 = new User();
        user1.setUserCode(companyCode1);
        user1.setUsername("techcorp@example.com");
        user1.setPassword(passwordEncoder.encode("company123"));
        user1.setRole(companyRole);
        userRepository.save(user1);

        Company company1 = new Company();
        company1.setUser(user1);
        company1.setCompanyCode(companyCode1);
        company1.setCompanyName("Công ty Công nghệ FPT Software");
        company1.setCompanyDescription("Công ty công nghệ hàng đầu Việt Nam chuyên về phát triển phần mềm, giải pháp IT và chuyển đổi số");
        company1.setCompanyAddress("Tòa nhà FPT, Đường Số 13, Khu Công nghệ cao, Quận 9, TP. Hồ Chí Minh");
        company1.setCompanyWebsite("https://fptsoftware.com");
        company1.setCompanyEmail("hr@fptsoftware.com");
        company1.setLogoURL("https://example.com/logos/fpt.png");
        company1.setCompanyStatus(CompanyStatus.ACTIVE);
        companyRepository.save(company1);

        // Jobs for FPT Software
        JobCategory itCategory = jobCategoryRepository.findAll().stream()
                .filter(jc -> jc.getJcName().equals("Công nghệ thông tin"))
                .findFirst()
                .orElseThrow();

        Job job1 = new Job();
        job1.setCompany(company1);
        job1.setJobCategory(itCategory);
        job1.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job1.setJobTitle("Tuyển dụng Lập trình viên Java Senior");
        job1.setJobDescription("Chúng tôi đang tìm kiếm lập trình viên Java giàu kinh nghiệm để tham gia đội ngũ phát triển backend. Bạn sẽ làm việc với các dự án lớn cho khách hàng quốc tế, sử dụng công nghệ hiện đại và quy trình Agile.");
        job1.setJobRequirement("- Tối thiểu 5 năm kinh nghiệm Java/Spring Boot\n- Thành thạo PostgreSQL, MySQL\n- Kinh nghiệm Microservices, Docker, Kubernetes\n- Có khả năng làm việc nhóm và giao tiếp tốt");
        job1.setJobSalary(25000000.0);
        job1.setJobLocation("TP. Hồ Chí Minh");
        job1.setStartDate(LocalDate.now().minusDays(5));
        job1.setEndDate(LocalDate.now().plusDays(25));
        job1.setMaxCandidates(2);
        job1.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job1);

        Job job2 = new Job();
        job2.setCompany(company1);
        job2.setJobCategory(itCategory);
        job2.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job2.setJobTitle("Frontend Developer (React/Next.js)");
        job2.setJobDescription("Tham gia đội ngũ frontend để xây dựng các ứng dụng web hiện đại. Cơ hội làm việc với các dự án sản phẩm cho thị trường Nhật Bản và Châu Âu.");
        job2.setJobRequirement("- Tối thiểu 3 năm kinh nghiệm React, Next.js\n- Thành thạo TypeScript, TailwindCSS\n- Có kinh nghiệm làm việc với RESTful API\n- Ưu tiên có kinh nghiệm UI/UX");
        job2.setJobSalary(20000000.0);
        job2.setJobLocation("TP. Hồ Chí Minh");
        job2.setStartDate(LocalDate.now().minusDays(3));
        job2.setEndDate(LocalDate.now().plusDays(27));
        job2.setMaxCandidates(3);
        job2.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job2);

        log.info("✓ Created Employer 1: FPT Software - Username: techcorp@example.com | Password: company123");

        // Employer 2: VCCorp
        String companyCode2 = codeGenerator.generateCode("DN", code -> 
                companyRepository.findByCompanyCode(code).isPresent());
        
        User user2 = new User();
        user2.setUserCode(companyCode2);
        user2.setUsername("marketingsolutions@example.com");
        user2.setPassword(passwordEncoder.encode("company123"));
        user2.setRole(companyRole);
        userRepository.save(user2);

        Company company2 = new Company();
        company2.setUser(user2);
        company2.setCompanyCode(companyCode2);
        company2.setCompanyName("Công ty TNHH Truyền thông VCCorp");
        company2.setCompanyDescription("Công ty truyền thông hàng đầu Việt Nam, cung cấp dịch vụ marketing tổng thể bao gồm digital marketing, quảng cáo và quản lý thương hiệu");
        company2.setCompanyAddress("Tầng 18, Tòa nhà VCCorp Tower, 19 Nguyễn Trãi, Quận 1, TP. Hà Nội");
        company2.setCompanyWebsite("https://vccorp.vn");
        company2.setCompanyEmail("tuyendung@vccorp.vn");
        company2.setLogoURL("https://example.com/logos/vccorp.png");
        company2.setCompanyStatus(CompanyStatus.ACTIVE);
        companyRepository.save(company2);

        // Jobs for VCCorp
        JobCategory marketingCategory = jobCategoryRepository.findAll().stream()
                .filter(jc -> jc.getJcName().equals("Truyền thông & Marketing"))
                .findFirst()
                .orElseThrow();

        JobCategory salesCategory = jobCategoryRepository.findAll().stream()
                .filter(jc -> jc.getJcName().equals("Kinh doanh & Bán hàng"))
                .findFirst()
                .orElseThrow();

        Job job3 = new Job();
        job3.setCompany(company2);
        job3.setJobCategory(marketingCategory);
        job3.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job3.setJobTitle("Trưởng phòng Digital Marketing");
        job3.setJobDescription("Lãnh đạo các chiến dịch marketing kỹ thuật số và xây dựng chiến lược phát triển thương hiệu. Quản lý đội ngũ 5-7 nhân viên, điều phối các kênh Facebook, Google, TikTok Ads với ngân sách hàng tỷ đồng/tháng.");
        job3.setJobRequirement("- Tối thiểu 4 năm kinh nghiệm Digital Marketing\n- Thành thạo SEO, SEM, Social Media Marketing\n- Có kinh nghiệm quản lý đội ngũ\n- Có khả năng phân tích dữ liệu và báo cáo ROI\n- Ưu tiên có kinh nghiệm làm việc tại agency hoặc tập đoàn lớn");
        job3.setJobSalary(22000000.0);
        job3.setJobLocation("Hà Nội");
        job3.setStartDate(LocalDate.now().minusDays(7));
        job3.setEndDate(LocalDate.now().plusDays(23));
        job3.setMaxCandidates(1);
        job3.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job3);

        Job job4 = new Job();
        job4.setCompany(company2);
        job4.setJobCategory(salesCategory);
        job4.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job4.setJobTitle("Chuyên viên Phát triển Kinh doanh (B2B)");
        job4.setJobDescription("Mở rộng danh mục khách hàng doanh nghiệp và xây dựng các mối quan hệ đối tác chiến lược. Làm việc với các tập đoàn lớn trong lĩnh vực bất động sản, tài chính, FMCG.");
        job4.setJobRequirement("- Tối thiểu 2 năm kinh nghiệm bán hàng B2B\n- Có kỹ năng giao tiếp và thuyết phục tốt\n- Có thành tích bán hàng xuất sắc\n- Có mạng lưới quan hệ rộng là lợi thế\n- Chủ động, năng động và có tinh thần trách nhiệm cao");
        job4.setJobSalary(18000000.0);
        job4.setJobLocation("Hà Nội");
        job4.setStartDate(LocalDate.now().minusDays(2));
        job4.setEndDate(LocalDate.now().plusDays(28));
        job4.setMaxCandidates(2);
        job4.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job4);

        log.info("✓ Created Employer 2: VCCorp - Username: marketingsolutions@example.com | Password: company123");
        log.info("✓ Created 4 active jobs");
    }

    private void seedCandidates() {
        if (candidateRepository.count() > 0) {
            log.info("Candidates already seeded. Skipping...");
            return;
        }

        log.info("Seeding candidates...");

        Role candidateRole = roleRepository.findByRoleCode("UV")
                .orElseThrow(() -> new RuntimeException("UV role not found"));

        // Candidate 1
        String candidateCode1 = codeGenerator.generateCode("UV", code -> 
                candidateRepository.findByCandidateCode(code).isPresent());
        
        User candidateUser1 = new User();
        candidateUser1.setUserCode(candidateCode1);
        candidateUser1.setUsername("nguyenvana@example.com");
        candidateUser1.setPassword(passwordEncoder.encode("candidate123"));
        candidateUser1.setRole(candidateRole);
        userRepository.save(candidateUser1);

        Candidate candidate1 = new Candidate();
        candidate1.setUser(candidateUser1);
        candidate1.setCandidateCode(candidateCode1);
        candidate1.setCandidateName("Nguyen Van A");
        candidate1.setCandidateDescription("Experienced software engineer with passion for backend development");
        candidate1.setCandidateGender(Gender.MALE);
        candidate1.setCandidateBirthdate(LocalDate.of(1995, 5, 15));
        candidate1.setCandidatePhone("0901234567");
        candidate1.setCandidateEmail("nguyenvana@example.com");
        candidate1.setCandidateEducation("Bachelor of Computer Science - HCMUT");
        candidate1.setCandidateExp("5 years in Java/Spring Boot development");
        candidate1.setCandidateSkills("Java, Spring Boot, PostgreSQL, Docker, Microservices");
        candidateRepository.save(candidate1);

        log.info("✓ Created Candidate 1: Nguyen Van A - Username: nguyenvana@example.com | Password: candidate123");

        // Candidate 2
        String candidateCode2 = codeGenerator.generateCode("UV", code -> 
                candidateRepository.findByCandidateCode(code).isPresent());
        
        User candidateUser2 = new User();
        candidateUser2.setUserCode(candidateCode2);
        candidateUser2.setUsername("tranthib@example.com");
        candidateUser2.setPassword(passwordEncoder.encode("candidate123"));
        candidateUser2.setRole(candidateRole);
        userRepository.save(candidateUser2);

        Candidate candidate2 = new Candidate();
        candidate2.setUser(candidateUser2);
        candidate2.setCandidateCode(candidateCode2);
        candidate2.setCandidateName("Tran Thi B");
        candidate2.setCandidateDescription("Creative marketing professional with strong digital presence");
        candidate2.setCandidateGender(Gender.FEMALE);
        candidate2.setCandidateBirthdate(LocalDate.of(1997, 8, 22));
        candidate2.setCandidatePhone("0909876543");
        candidate2.setCandidateEmail("tranthib@example.com");
        candidate2.setCandidateEducation("Bachelor of Marketing - UEH");
        candidate2.setCandidateExp("3 years in digital marketing and content creation");
        candidate2.setCandidateSkills("SEO, SEM, Facebook Ads, Google Analytics, Content Strategy");
        candidateRepository.save(candidate2);

        log.info("✓ Created Candidate 2: Tran Thi B - Username: tranthib@example.com | Password: candidate123");
    }

    private void seedCVs() {
        if (cvRepository.count() > 0) {
            log.info("CVs already seeded. Skipping...");
            return;
        }

        log.info("Seeding CVs...");

        // Get candidates via users
        User user1 = userRepository.findByUsername("nguyenvana@example.com")
                .orElseThrow(() -> new RuntimeException("Candidate user 1 not found"));
        Candidate candidate1 = candidateRepository.findByUserUserId(user1.getUserId())
                .orElseThrow(() -> new RuntimeException("Candidate 1 not found"));
        
        User user2 = userRepository.findByUsername("tranthib@example.com")
                .orElseThrow(() -> new RuntimeException("Candidate user 2 not found"));
        Candidate candidate2 = candidateRepository.findByUserUserId(user2.getUserId())
                .orElseThrow(() -> new RuntimeException("Candidate 2 not found"));

        // CV for Candidate 1
        CV cv1 = new CV();
        cv1.setCandidate(candidate1);
        cv1.setCvCode(codeGenerator.generateCode("CV", code -> 
                cvRepository.findByCvCode(code).isPresent()));
        cv1.setCvFile("/uploads/cvs/nguyen_van_a_java_developer.pdf");
        cv1.setCvStatus(CVStatus.ACTIVE);
        cvRepository.save(cv1);

        // Second CV for Candidate 1 (hidden)
        CV cv2 = new CV();
        cv2.setCandidate(candidate1);
        cv2.setCvCode(codeGenerator.generateCode("CV", code -> 
                cvRepository.findByCvCode(code).isPresent()));
        cv2.setCvFile("/uploads/cvs/nguyen_van_a_fullstack_developer_old.pdf");
        cv2.setCvStatus(CVStatus.HIDDEN);
        cvRepository.save(cv2);

        // CV for Candidate 2
        CV cv3 = new CV();
        cv3.setCandidate(candidate2);
        cv3.setCvCode(codeGenerator.generateCode("CV", code -> 
                cvRepository.findByCvCode(code).isPresent()));
        cv3.setCvFile("/uploads/cvs/tran_thi_b_marketing_specialist.pdf");
        cv3.setCvStatus(CVStatus.ACTIVE);
        cvRepository.save(cv3);

        log.info("✓ Created 3 CVs (2 ACTIVE, 1 HIDDEN)");
    }
}
