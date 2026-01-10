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
    private final ApplicationRepository applicationRepository;
    private final SeekingPostRepository seekingPostRepository;
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
        seedApplications();
        seedSeekingPosts();
        
        log.info("=== DATA SEEDING COMPLETED ===");
        log.info("Total Users: {}", userRepository.count());
        log.info("Total Companies: {}", companyRepository.count());
        log.info("Total Candidates: {}", candidateRepository.count());
        log.info("Total Job Categories: {}", jobCategoryRepository.count());
        log.info("Total Jobs: {}", jobRepository.count());
        log.info("Total CVs: {}", cvRepository.count());
        log.info("Total Applications: {}", applicationRepository.count());
        log.info("Total Seeking Posts: {}", seekingPostRepository.count());
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

        // Doanh nghiệp 1: Tech Corp
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
        company1.setLogoURL("/uploads/logos/0724b97f-8f47-4f3b-a51e-646a148e39c8-FPTSoft.png");
        company1.setCompanyStatus(CompanyStatus.ACTIVE);
        companyRepository.save(company1);

        // Việc làm cho FPT Software
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

        Job job3 = new Job();
        job3.setCompany(company1);
        job3.setJobCategory(itCategory);
        job3.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job3.setJobTitle("DevOps Engineer");
        job3.setJobDescription("Chịu trách nhiệm triển khai và duy trì hệ thống CI/CD, quản lý hạ tầng cloud và đảm bảo tính sẵn sàng của dịch vụ.");
        job3.setJobRequirement("- Tối thiểu 4 năm kinh nghiệm DevOps\n- Thành thạo Docker, Kubernetes, AWS\n- Kinh nghiệm với Terraform, Ansible\n- Có khả năng làm việc nhóm và giải quyết sự cố");
        job3.setJobSalary(23000000.0);
        job3.setJobLocation("TP. Hồ Chí Minh");
        job3.setStartDate(LocalDate.now().minusDays(2));
        job3.setEndDate(LocalDate.now().plusDays(28));
        job3.setMaxCandidates(2);
        job3.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job3);

        Job job4 = new Job();
        job4.setCompany(company1);
        job4.setJobCategory(itCategory);
        job4.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job4.setJobTitle("Chuyên viên Phân tích Nghiệp vụ (BA)");
        job4.setJobDescription("Phân tích yêu cầu nghiệp vụ, làm việc với khách hàng và đội ngũ phát triển để đảm bảo sản phẩm đáp ứng đúng nhu cầu.");
        job4.setJobRequirement("- Tối thiểu 3 năm kinh nghiệm BA\n- Kỹ năng giao tiếp và phân tích tốt\n- Có kinh nghiệm làm việc với Agile/Scrum\n- Ưu tiên có chứng chỉ CBAP hoặc tương đương");
        job4.setJobSalary(18000000.0);
        job4.setJobLocation("TP. Hồ Chí Minh");
        job4.setStartDate(LocalDate.now().minusDays(1));       
        job4.setEndDate(LocalDate.now().plusDays(29));
        job4.setMaxCandidates(2);
        job4.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job4);

        Job job5 = new Job();
        job5.setCompany(company1);
        job5.setJobCategory(itCategory);
        job5.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job5.setJobTitle("Kỹ sư Kiểm thử Tự động (Automation Tester)");
        job5.setJobDescription("Phát triển và duy trì các kịch bản kiểm thử tự động để đảm bảo chất lượng phần mềm. Làm việc chặt chẽ với đội ngũ phát triển để tích hợp kiểm thử vào quy trình CI/CD.");
        job5.setJobRequirement("- Tối thiểu 3 năm kinh nghiệm kiểm thử tự động\n- Thành thạo Selenium, JUnit/TestNG\n- Kinh nghiệm với CI/CD (Jenkins, GitLab CI)\n- Có khả năng viết kịch bản kiểm thử hiệu quả");
        job5.setJobSalary(19000000.0);
        job5.setJobLocation("TP. Hồ Chí Minh");
        job5.setStartDate(LocalDate.now());
        job5.setEndDate(LocalDate.now().plusDays(30));
        job5.setMaxCandidates(2);      
        job5.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job5);

        log.info("✓ Created Employer 1: FPT Software - Username: techcorp@example.com | Password: company123");

        // Doanh nghiệp 2: VCCorp
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
        company2.setLogoURL("/uploads/logos/5a426017-cf11-490f-9a1c-cc226176bb0e-VCCorp-2.png");
        company2.setCompanyStatus(CompanyStatus.ACTIVE);
        companyRepository.save(company2);

        // Việc làm cho VCCorp
        JobCategory marketingCategory = jobCategoryRepository.findAll().stream()
                .filter(jc -> jc.getJcName().equals("Truyền thông & Marketing"))
                .findFirst()
                .orElseThrow();

        JobCategory salesCategory = jobCategoryRepository.findAll().stream()
                .filter(jc -> jc.getJcName().equals("Kinh doanh & Bán hàng"))
                .findFirst()
                .orElseThrow();

        Job job6 = new Job();
        job6.setCompany(company2);
        job6.setJobCategory(marketingCategory);
        job6.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job6.setJobTitle("Trưởng phòng Digital Marketing");
        job6.setJobDescription("Lãnh đạo các chiến dịch marketing kỹ thuật số và xây dựng chiến lược phát triển thương hiệu. Quản lý đội ngũ 5-7 nhân viên, điều phối các kênh Facebook, Google, TikTok Ads với ngân sách hàng tỷ đồng/tháng.");
        job6.setJobRequirement("- Tối thiểu 4 năm kinh nghiệm Digital Marketing\n- Thành thạo SEO, SEM, Social Media Marketing\n- Có kinh nghiệm quản lý đội ngũ\n- Có khả năng phân tích dữ liệu và báo cáo ROI\n- Ưu tiên có kinh nghiệm làm việc tại agency hoặc tập đoàn lớn");
        job6.setJobSalary(22000000.0);
        job6.setJobLocation("Hà Nội");
        job6.setStartDate(LocalDate.now().minusDays(7));
        job6.setEndDate(LocalDate.now().plusDays(23));
        job6.setMaxCandidates(1);
        job6.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job6);

        Job job7 = new Job();
        job7.setCompany(company2);
        job7.setJobCategory(salesCategory);
        job7.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job7.setJobTitle("Chuyên viên Phát triển Kinh doanh (B2B)");
        job7.setJobDescription("Mở rộng danh mục khách hàng doanh nghiệp và xây dựng các mối quan hệ đối tác chiến lược. Làm việc với các tập đoàn lớn trong lĩnh vực bất động sản, tài chính, FMCG.");
        job7.setJobRequirement("- Tối thiểu 2 năm kinh nghiệm bán hàng B2B\n- Có kỹ năng giao tiếp và thuyết phục tốt\n- Có thành tích bán hàng xuất sắc\n- Có mạng lưới quan hệ rộng là lợi thế\n- Chủ động, năng động và có tinh thần trách nhiệm cao");
        job7.setJobSalary(18000000.0);
        job7.setJobLocation("Hà Nội");
        job7.setStartDate(LocalDate.now().minusDays(2));
        job7.setEndDate(LocalDate.now().plusDays(28));
        job7.setMaxCandidates(2);
        job7.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job7);

        Job job8 = new Job();
        job8.setCompany(company2);
        job8.setJobCategory(marketingCategory);
        job8.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job8.setJobTitle("Chuyên viên Nội dung & SEO");
        job8.setJobDescription("Phát triển chiến lược nội dung và tối ưu hóa SEO cho các trang web và kênh truyền thông xã hội. Tạo nội dung hấp dẫn để thu hút và giữ chân khách hàng.");
        job8.setJobRequirement("- Tối thiểu 2 năm kinh nghiệm viết nội dung và SEO\n- Kỹ năng viết lách và biên tập tốt\n- Hiểu biết về công cụ SEO (Google Analytics, Ahrefs, SEMrush)\n- Có khả năng làm việc độc lập và theo nhóm\n- Sáng tạo và cập nhật xu hướng mới trong lĩnh vực nội dung số");
        job8.setJobSalary(15000000.0);
        job8.setJobLocation("Hà Nội");
        job8.setStartDate(LocalDate.now().minusDays(1));       
        job8.setEndDate(LocalDate.now().plusDays(29));
        job8.setMaxCandidates(2);
        job8.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job8);

        Job job9 = new Job();
        job9.setCompany(company2);     
        job9.setJobCategory(salesCategory);
        job9.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job9.setJobTitle("Nhân viên Kinh doanh Trực tiếp");
        job9.setJobDescription("Tiếp cận và tư vấn khách hàng tiềm năng về các giải pháp truyền thông và marketing của công ty. Tham gia các sự kiện, hội thảo để mở rộng mạng lưới khách hàng.");
        job9.setJobRequirement("- Tối thiểu 1 năm kinh nghiệm kinh doanh trực tiếp\n- Kỹ năng giao tiếp và thuyết phục tốt\n- Nhiệt huyết và có tinh thần cầu tiến\n- Có khả năng làm việc dưới áp lực cao\n- Ưu tiên có kinh nghiệm trong lĩnh vực truyền thông hoặc quảng cáo");
        job9.setJobSalary(13000000.0); 
        job9.setJobLocation("Hà Nội");
        job9.setStartDate(LocalDate.now());
        job9.setEndDate(LocalDate.now().plusDays(30));
        job9.setMaxCandidates(3);
        job9.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job9);

        Job job10 = new Job();
        job10.setCompany(company2);
        job10.setJobCategory(marketingCategory);
        job10.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job10.setJobTitle("Chuyên viên Quảng cáo Trực tuyến");
        job10.setJobDescription("Quản lý và tối ưu hóa các chiến dịch quảng cáo trực tuyến trên Google, Facebook và các nền tảng khác. Phân tích hiệu quả chiến dịch và đề xuất cải tiến để đạt được mục tiêu kinh doanh.");
        job10.setJobRequirement("- Tối thiểu 2 năm kinh nghiệm quảng cáo trực tuyến\n- Thành thạo Google Ads, Facebook Ads\n- Kỹ năng phân tích dữ liệu và tối ưu hóa chiến dịch\n- Có khả năng làm việc độc lập và theo nhóm\n- Cập nhật xu hướng mới trong lĩnh vực quảng cáo số");
        job10.setJobSalary(16000000.0);
        job10.setJobLocation("Hà Nội");
        job10.setStartDate(LocalDate.now());
        job10.setEndDate(LocalDate.now().plusDays(30));
        job10.setMaxCandidates(2);
        job10.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job10);
        
        log.info("✓ Created Employer 2: VCCorp - Username: marketingsolutions@example.com | Password: company123");

        String companyCode3 = codeGenerator.generateCode("DN", code -> 
                companyRepository.findByCompanyCode(code).isPresent());
        
        // Doanh nghiệp 3: Bosch Vietnam
        User user3 = new User();
        user3.setUserCode(companyCode3);
        user3.setUsername("recruitment@bosch.com");
        user3.setPassword(passwordEncoder.encode("company123"));
        user3.setRole(companyRole);
        userRepository.save(user3);

        Company company3 = new Company();
        company3.setUser(user3);
        company3.setCompanyCode(companyCode3);
        company3.setCompanyName("Bosch Vietnam");
        company3.setCompanyDescription("Bosch Vietnam is a leading technology and engineering company, providing innovative solutions in mobility, industrial technology, consumer goods, and energy and building technology.");
        company3.setCompanyAddress("Lầu 9, Tòa nhà Saigon Centre, 65 Lê Lợi, Quận 1, TP. Hồ Chí Minh");
        company3.setCompanyWebsite("https://www.bosch.vn");
        company3.setCompanyEmail("contact@bosch.com");
        company3.setLogoURL("/uploads/logos/bosch-logo.png");
        company3.setCompanyStatus(CompanyStatus.ACTIVE);
        companyRepository.save(company3);

        // Việc làm cho Bosch Vietnam
        Job job11 = new Job();
        job11.setCompany(company3);
        job11.setJobCategory(itCategory);
        job11.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job11.setJobTitle("Embedded Software Engineer");
        job11.setJobDescription("Develop and maintain embedded software for automotive applications. Work with cross-functional teams to deliver high-quality solutions.");
        job11.setJobRequirement("- Minimum 3 years experience in embedded software development\n- Proficient in C/C++ programming\n- Experience with real-time operating systems\n- Strong problem-solving skills");
        job11.setJobSalary(24000000.0);
        job11.setJobLocation("TP. Hồ Chí Minh");
        job11.setStartDate(LocalDate.now().minusDays(4));      
        job11.setEndDate(LocalDate.now().plusDays(26));
        job11.setMaxCandidates(2);
        job11.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job11);

        Job job12 = new Job();
        job12.setCompany(company3);
        job12.setJobCategory(itCategory);
        job12.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job12.setJobTitle("Data Scientist");
        job12.setJobDescription("Analyze large datasets to extract insights and build predictive models. Collaborate with engineering teams to implement data-driven solutions.");
        job12.setJobRequirement("- Strong background in statistics and machine learning\n- Proficient in Python and R\n- Experience with big data technologies\n- Excellent communication skills");
        job12.setJobSalary(22000000.0);
        job12.setJobLocation("TP. Hồ Chí Minh");
        job12.setStartDate(LocalDate.now().minusDays(3));      
        job12.setEndDate(LocalDate.now().plusDays(27));
        job12.setMaxCandidates(3);
        job12.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job12);

        Job job13 = new Job();
        job13.setCompany(company3);
        job13.setJobCategory(itCategory);
        job13.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job13.setJobTitle("Cloud Solutions Architect");
        job13.setJobDescription("Design and implement cloud-based solutions for enterprise clients. Provide technical leadership and guidance on cloud architecture best practices.");
        job13.setJobRequirement("- Extensive experience with cloud platforms (AWS, Azure, GCP)\n- Strong understanding of cloud architecture and design patterns\n- Excellent problem-solving and communication skills\n- Ability to lead technical teams");
        job13.setJobSalary(26000000.0);
        job13.setJobLocation("TP. Hồ Chí Minh");
        job13.setStartDate(LocalDate.now().minusDays(2));
        job13.setEndDate(LocalDate.now().plusDays(28));
        job13.setMaxCandidates(2);
        job13.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job13);

        Job job14 = new Job();
        job14.setCompany(company3);
        job14.setJobCategory(itCategory);
        job14.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job14.setJobTitle("AI Research Engineer");
        job14.setJobDescription("Conduct research and development in artificial intelligence and machine learning. Collaborate with academic and industry partners to advance AI technologies.");
        job14.setJobRequirement("- Strong research background in AI/ML\n- Proficient in Python and deep learning frameworks\n- Excellent analytical and problem-solving skills\n- Ability to publish research findings");
        job14.setJobSalary(28000000.0);
        job14.setJobLocation("TP. Hồ Chí Minh");
        job14.setStartDate(LocalDate.now().minusDays(1));
        job14.setEndDate(LocalDate.now().plusDays(29));
        job14.setMaxCandidates(1);
        job14.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job14);

        Job job15 = new Job();
        job15.setCompany(company3);
        job15.setJobCategory(itCategory);
        job15.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job15.setJobTitle("Cybersecurity Specialist");
        job15.setJobDescription("Implement and manage cybersecurity measures to protect company assets. Monitor security systems and respond to incidents.");
        job15.setJobRequirement("- Strong knowledge of cybersecurity principles and practices\n- Experience with security tools and technologies\n- Excellent problem-solving and communication skills\n- Ability to work under pressure");
        job15.setJobSalary(23000000.0);
        job15.setJobLocation("TP. Hồ Chí Minh");
        job15.setStartDate(LocalDate.now());
        job15.setEndDate(LocalDate.now().plusDays(30));
        job15.setMaxCandidates(2);
        job15.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job15);

        log.info("✓ Created Employer 3: Bosch Vietnam - Username: recruitment@bosch.vn | Password: company123");

        String companyCode4 = codeGenerator.generateCode("DN", code -> 
                companyRepository.findByCompanyCode(code).isPresent());
        
        // Doanh nghiệp 4: BEAT Vietnam
        User user4 = new User();
        user4.setUserCode(companyCode4);
        user4.setUsername("recruitment@beat.vn");
        user4.setPassword(passwordEncoder.encode("company123"));
        user4.setRole(companyRole);
        userRepository.save(user4);

        Company company4 = new Company();
        company4.setUser(user4);
        company4.setCompanyCode(companyCode4);
        company4.setCompanyName("BEAT Việt Nam");
        company4.setCompanyDescription("BEAT là nền tảng công nghệ cung cấp dịch vụ gọi xe và giao nhận hàng đầu, mang đến giải pháp di chuyển tiện lợi và an toàn cho người dùng.");
        company4.setCompanyAddress("Tầng 5, Tòa nhà Saigon Trade Center, 37 Tôn Đức Thắng, Quận 1, TP. Hồ Chí Minh");
        company4.setCompanyWebsite("https://www.beat.vn");
        company4.setCompanyEmail("contact@beat.vn");
        company4.setLogoURL("/uploads/logos/beat-logo.png");
        company4.setCompanyStatus(CompanyStatus.ACTIVE);
        companyRepository.save(company4);

        // Việc làm cho BEAT Vietnam
        Job job16 = new Job();
        job16.setCompany(company4);
        job16.setJobCategory(itCategory);
        job16.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job16.setJobTitle("Mobile App Developer (iOS/Android)");
        job16.setJobDescription("Phát triển và duy trì ứng dụng di động BEAT trên cả hai nền tảng iOS và Android. Hợp tác với đội ngũ thiết kế và backend để tạo ra trải nghiệm người dùng mượt mà và hiệu quả.");
        job16.setJobRequirement("- Tối thiểu 3 năm kinh nghiệm phát triển ứng dụng di động\n- Thành thạo Swift (iOS) và Kotlin/Java (Android)\n- Kinh nghiệm với RESTful APIs và tích hợp SDK\n- Có khả năng làm việc nhóm và giải quyết vấn đề");
        job16.setJobSalary(21000000.0);
        job16.setJobLocation("TP. Hồ Chí Minh");
        job16.setStartDate(LocalDate.now().minusDays(4));      
        job16.setEndDate(LocalDate.now().plusDays(26));
        job16.setMaxCandidates(2);
        job16.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job16);

        Job job17 = new Job();
        job17.setCompany(company4);
        job17.setJobCategory(itCategory);
        job17.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job17.setJobTitle("Backend Developer (Node.js)");
        job17.setJobDescription("Xây dựng và tối ưu hóa các dịch vụ backend cho nền tảng BEAT. Làm việc với cơ sở dữ liệu lớn và hệ thống phân tán để đảm bảo hiệu suất và khả năng mở rộng.");
        job17.setJobRequirement("- Tối thiểu 3 năm kinh nghiệm phát triển backend với Node.js\n- Thành thạo MongoDB, Redis, RabbitMQ\n- Kinh nghiệm xây dựng API RESTful và GraphQL\n- Hiểu biết về kiến trúc microservices");
        job17.setJobSalary(22000000.0);
        job17.setJobLocation("TP. Hồ Chí Minh");
        job17.setStartDate(LocalDate.now().minusDays(3));       
        job17.setEndDate(LocalDate.now().plusDays(27));
        job17.setMaxCandidates(2);
        job17.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job17);

        Job job18 = new Job();
        job18.setCompany(company4);
        job18.setJobCategory(itCategory);
        job18.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));        
        job18.setJobTitle("QA Engineer");
        job18.setJobDescription("Đảm bảo chất lượng phần mềm của nền tảng BEAT thông qua việc thiết kế và thực hiện các kịch bản kiểm thử. Hợp tác với đội ngũ phát triển để phát hiện và khắc phục lỗi.");
        job18.setJobRequirement("- Tối thiểu 2 năm kinh nghiệm kiểm thử phần mềm\n- Kinh nghiệm với các công cụ kiểm thử tự động (Selenium, JUnit)\n- Hiểu biết về quy trình phát triển phần mềm Agile\n- Kỹ năng phân tích và giải quyết vấn đề tốt");
        job18.setJobSalary(17000000.0);
        job18.setJobLocation("TP. Hồ Chí Minh");
        job18.setStartDate(LocalDate.now().minusDays(2));
        job18.setEndDate(LocalDate.now().plusDays(28));
        job18.setMaxCandidates(2);
        job18.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job18);

        Job job19 = new Job();
        job19.setCompany(company4);
        job19.setJobCategory(itCategory);
        job19.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job19.setJobTitle("UI/UX Designer");
        job19.setJobDescription("Thiết kế giao diện người dùng và trải nghiệm người dùng cho ứng dụng BEAT. Hợp tác với đội ngũ phát triển để tạo ra các thiết kế hấp dẫn và dễ sử dụng.");
        job19.setJobRequirement("- Tối thiểu 2 năm kinh nghiệm thiết kế UI/UX\n- Thành thạo các công cụ thiết kế (Figma, Adobe XD, Sketch)\n- Hiểu biết về nguyên tắc thiết kế và trải nghiệm người dùng\n- Kỹ năng giao tiếp và làm việc nhóm tốt");
        job19.setJobSalary(18000000.0);
        job19.setJobLocation("TP. Hồ Chí Minh");
        job19.setStartDate(LocalDate.now());
        job19.setEndDate(LocalDate.now().plusDays(30));
        job19.setMaxCandidates(2);
        job19.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job19);

        log.info("✓ Created Employer 4: BEAT Vietnam - Username: recruitment@beat.vn | Password: company123");

        String companyCode5 = codeGenerator.generateCode("DN", code -> 
                companyRepository.findByCompanyCode(code).isPresent());
        
        // Doanh nghiệp 5: MB Bank
        User user5 = new User();
        user5.setUserCode(companyCode5);
        user5.setUsername("recruitment@mbbank.com");
        user5.setPassword(passwordEncoder.encode("company123"));
        user5.setRole(companyRole);
        userRepository.save(user5);

        Company company5 = new Company();
        company5.setUser(user5);
        company5.setCompanyCode(companyCode5);
        company5.setCompanyName("Ngân hàng Quân Đội MB Bank");
        company5.setCompanyDescription("MB Bank là một trong những ngân hàng thương mại cổ phần lớn nhất Việt Nam, cung cấp các dịch vụ tài chính đa dạng bao gồm ngân hàng bán lẻ, ngân hàng doanh nghiệp và dịch vụ đầu tư.");
        company5.setCompanyAddress("Số 63 Lý Thường Kiệt, Phường 7, Quận Tân Bình, TP. Hồ Chí Minh");
        company5.setCompanyWebsite("https://www.mbbank.com.vn");
        company5.setCompanyEmail("contact@mbbank.com");
        company5.setLogoURL("/uploads/logos/mbbank-logo.png");
        company5.setCompanyStatus(CompanyStatus.ACTIVE);
        companyRepository.save(company5);

        // Việc làm cho MB Bank
        Job job20 = new Job();
        job20.setCompany(company5);
        job20.setJobCategory(itCategory);       
        job20.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job20.setJobTitle("Chuyên viên Phát triển Ứng dụng Ngân hàng Số");
        job20.setJobDescription("Phát triển và duy trì các ứng dụng ngân hàng số của MB Bank. Hợp tác với các đội ngũ kỹ thuật và kinh doanh để cung cấp trải nghiệm người dùng tốt nhất.");
        job20.setJobRequirement("- Tối thiểu 3 năm kinh nghiệm phát triển ứng dụng\n- Thành thạo Java, Spring Boot, React\n- Kinh nghiệm với cơ sở dữ liệu quan hệ và NoSQL\n- Hiểu biết về bảo mật ứng dụng ngân hàng");
        job20.setJobSalary(23000000.0);
        job20.setJobLocation("TP. Hồ Chí Minh");
        job20.setStartDate(LocalDate.now().minusDays(5));
        job20.setEndDate(LocalDate.now().plusDays(25));
        job20.setMaxCandidates(2);
        job20.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job20);

        log.info("✓ Created Employer 5: MB Bank - Username: recruitment@mbbank.com | Password: company123");

        log.info("✓ Created 20 active jobs");
    }

    private void seedCandidates() {
        if (candidateRepository.count() > 0) {
            log.info("Candidates already seeded. Skipping...");
            return;
        }

        log.info("Seeding candidates...");

        Role candidateRole = roleRepository.findByRoleCode("UV")
                .orElseThrow(() -> new RuntimeException("UV role not found"));

        // Ứng viên 1
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

        // Ứng viên 2
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

        // Lấy ứng viên qua người dùng
        User user1 = userRepository.findByUsername("nguyenvana@example.com")
                .orElseThrow(() -> new RuntimeException("Candidate user 1 not found"));
        Candidate candidate1 = candidateRepository.findByUserUserId(user1.getUserId())
                .orElseThrow(() -> new RuntimeException("Candidate 1 not found"));
        
        User user2 = userRepository.findByUsername("tranthib@example.com")
                .orElseThrow(() -> new RuntimeException("Candidate user 2 not found"));
        Candidate candidate2 = candidateRepository.findByUserUserId(user2.getUserId())
                .orElseThrow(() -> new RuntimeException("Candidate 2 not found"));

        // CV cho Ứng viên 1
        CV cv1 = new CV();
        cv1.setCandidate(candidate1);
        cv1.setCvCode(codeGenerator.generateCode("CV", code -> 
                cvRepository.findByCvCode(code).isPresent()));
        cv1.setCvFile("/uploads/cvs/nguyen_van_a_java_developer.pdf");
        cv1.setCvStatus(CVStatus.ACTIVE);
        cvRepository.save(cv1);

        // CV thứ hai cho Ứng viên 1 (ẩn)
        CV cv2 = new CV();
        cv2.setCandidate(candidate1);
        cv2.setCvCode(codeGenerator.generateCode("CV", code -> 
                cvRepository.findByCvCode(code).isPresent()));
        cv2.setCvFile("/uploads/cvs/nguyen_van_a_fullstack_developer_old.pdf");
        cv2.setCvStatus(CVStatus.HIDDEN);
        cvRepository.save(cv2);

        // CV cho Ứng viên 2
        CV cv3 = new CV();
        cv3.setCandidate(candidate2);
        cv3.setCvCode(codeGenerator.generateCode("CV", code -> 
                cvRepository.findByCvCode(code).isPresent()));
        cv3.setCvFile("/uploads/cvs/tran_thi_b_marketing_specialist.pdf");
        cv3.setCvStatus(CVStatus.ACTIVE);
        cvRepository.save(cv3);
        log.info("✓ Created 3 CVs (2 ACTIVE, 1 HIDDEN)");
    }


    private void seedApplications() {
        if (applicationRepository.count() > 0) {
            log.info("Applications already seeded. Skipping...");
            return;
        }

        log.info("Seeding applications...");

        // Lấy ứng viên qua người dùng
        User candidateUser1 = userRepository.findByUsername("nguyenvana@example.com")
                .orElseThrow(() -> new RuntimeException("Candidate user 1 not found"));
        Candidate candidate1 = candidateRepository.findByUserUserId(candidateUser1.getUserId())
                .orElseThrow(() -> new RuntimeException("Candidate 1 not found"));
        
        User candidateUser2 = userRepository.findByUsername("tranthib@example.com")
                .orElseThrow(() -> new RuntimeException("Candidate user 2 not found"));
        Candidate candidate2 = candidateRepository.findByUserUserId(candidateUser2.getUserId())
                .orElseThrow(() -> new RuntimeException("Candidate 2 not found"));

        // Lấy CVs
        CV cv1 = cvRepository.findAll().stream()
                .filter(cv -> cv.getCandidate().getCandidateId().equals(candidate1.getCandidateId()) 
                        && cv.getCvStatus() == CVStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Active CV for candidate 1 not found"));

        CV cv3 = cvRepository.findAll().stream()
                .filter(cv -> cv.getCandidate().getCandidateId().equals(candidate2.getCandidateId()) 
                        && cv.getCvStatus() == CVStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Active CV for candidate 2 not found"));

        // Lấy việc làm
        Job job1 = jobRepository.findAll().stream()
                .filter(j -> j.getJobTitle().contains("Java Senior"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Java job not found"));

        Job job3 = jobRepository.findAll().stream()
                .filter(j -> j.getJobTitle().contains("Digital Marketing"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Marketing job not found"));

        Job job4 = jobRepository.findAll().stream()
                .filter(j -> j.getJobTitle().contains("Kinh doanh"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Sales job not found"));

        // Đơn ứng tuyển 1: Ứng viên 1 ứng tuyển vào việc làm Java (ĐANG CHỜ)
        Application app1 = new Application();
        app1.setJob(job1);
        app1.setCv(cv1);
        app1.setApplicationCode(codeGenerator.generateCode("DX", code -> 
                applicationRepository.findByApplicationCode(code).isPresent()));
        app1.setApplyTime(java.time.LocalDateTime.now().minusDays(2));
        app1.setApplicationStatus(ApplicationStatus.PENDING);
        applicationRepository.save(app1);

        // Đơn ứng tuyển 2: Ứng viên 2 ứng tuyển vào việc làm Marketing (ĐANG CHỜ)
        Application app2 = new Application();
        app2.setJob(job3);
        app2.setCv(cv3);
        app2.setApplicationCode(codeGenerator.generateCode("DX", code -> 
                applicationRepository.findByApplicationCode(code).isPresent()));
        app2.setApplyTime(java.time.LocalDateTime.now().minusDays(1));
        app2.setApplicationStatus(ApplicationStatus.PENDING);
        applicationRepository.save(app2);

        // Đơn ứng tuyển 3: Ứng viên 2 ứng tuyển vào việc làm Kinh doanh (ĐÃ DUYỆT)
        Application app3 = new Application();
        app3.setJob(job4);
        app3.setCv(cv3);
        app3.setApplicationCode(codeGenerator.generateCode("DX", code -> 
                applicationRepository.findByApplicationCode(code).isPresent()));
        app3.setApplyTime(java.time.LocalDateTime.now().minusDays(3));
        app3.setApplicationStatus(ApplicationStatus.APPROVED);
        applicationRepository.save(app3);

        log.info("✓ Created 3 applications (2 PENDING, 1 APPROVED)");
        log.info("  - Nguyen Van A → Java job (PENDING)");
        log.info("  - Tran Thi B → Marketing job (PENDING)");
        log.info("  - Tran Thi B → Sales job (APPROVED)");
    }

    private void seedSeekingPosts() {
        if (seekingPostRepository.count() > 0) {
            log.info("Seeking posts already seeded. Skipping...");
            return;
        }

        log.info("Seeding seeking posts...");

        // Lấy ứng viên qua email (qua quan hệ Người dùng → Ứng viên)
        User user1 = userRepository.findByUsername("nguyenvana@example.com")
                .orElseThrow(() -> new RuntimeException("User nguyenvana@example.com not found"));
        Candidate candidate1 = candidateRepository.findByUserUserId(user1.getUserId())
                .orElseThrow(() -> new RuntimeException("Candidate 1 not found"));
        
        User user2 = userRepository.findByUsername("tranthib@example.com")
                .orElseThrow(() -> new RuntimeException("User tranthib@example.com not found"));
        Candidate candidate2 = candidateRepository.findByUserUserId(user2.getUserId())
                .orElseThrow(() -> new RuntimeException("Candidate 2 not found"));

        // Bài đăng tìm việc 1 - Senior Java Developer
        SeekingPost seekingPost1 = new SeekingPost();
        seekingPost1.setCandidate(candidate1);
        seekingPost1.setSkPostCode(codeGenerator.generateCode("BV", code -> 
                seekingPostRepository.findBySkPostCode(code).isPresent()));
        seekingPost1.setSkPostTitle("Senior Java Developer Seeking Opportunities in Product Companies");
        seekingPost1.setSkPostIntro("Experienced backend developer with 5+ years expertise in Java/Spring Boot ecosystem. " +
                "Seeking challenging opportunities in product companies where I can contribute to large-scale systems. " +
                "Strong background in microservices architecture, cloud technologies, and agile methodologies. " +
                "Passionate about clean code, system design, and mentoring junior developers. Open to remote or hybrid positions.");
        seekingPost1.setSkPostSkills("Java, Spring Boot, PostgreSQL, Docker, Kubernetes, Microservices, Redis, Kafka, AWS, Git");
        seekingPost1.setDesiredSalary(2500.0);
        seekingPost1.setDesiredLocation("Ho Chi Minh City");
        seekingPost1.setExpiryDate(LocalDate.now().plusMonths(3));
        seekingPost1.setSkPostStatus(SeekingPostStatus.ACTIVE);
        seekingPostRepository.save(seekingPost1);

        log.info("✓ Created Seeking Post 1: Senior Java Developer - Nguyen Van A (Code: {})", seekingPost1.getSkPostCode());

        // Bài đăng tìm việc 2 - Chuyên gia Digital Marketing
        SeekingPost seekingPost2 = new SeekingPost();
        seekingPost2.setCandidate(candidate2);
        seekingPost2.setSkPostCode(codeGenerator.generateCode("BV", code -> 
                seekingPostRepository.findBySkPostCode(code).isPresent()));
        seekingPost2.setSkPostTitle("Digital Marketing Specialist with 3+ Years Experience");
        seekingPost2.setSkPostIntro("Creative marketing professional with proven track record in SEO/SEM and social media management. " +
                "3 years experience managing multi-channel digital campaigns with measurable ROI-driven results. " +
                "Specialized in content strategy, performance analytics, and brand building. Currently looking for growth opportunities " +
                "in dynamic companies where I can lead marketing initiatives and drive business growth through digital channels.");
        seekingPost2.setSkPostSkills("SEO, SEM, Facebook Ads, Google Analytics, Content Marketing, Social Media Management, Copywriting, Email Marketing");
        seekingPost2.setDesiredSalary(1000.0);
        seekingPost2.setDesiredLocation("Ha Noi");
        seekingPost2.setExpiryDate(LocalDate.now().plusMonths(2));
        seekingPost2.setSkPostStatus(SeekingPostStatus.ACTIVE);
        seekingPostRepository.save(seekingPost2);

        log.info("✓ Created Seeking Post 2: Digital Marketing Specialist - Tran Thi B (Code: {})", seekingPost2.getSkPostCode());

        // Bài đăng tìm việc 3 - Full Stack Developer (ẨN - ứng viên chuyển sang backend only)
        SeekingPost seekingPost3 = new SeekingPost();
        seekingPost3.setCandidate(candidate1);
        seekingPost3.setSkPostCode(codeGenerator.generateCode("BV", code -> 
                seekingPostRepository.findBySkPostCode(code).isPresent()));
        seekingPost3.setSkPostTitle("Full Stack Developer Open to New Roles");
        seekingPost3.setSkPostIntro("Versatile developer proficient in both frontend and backend technologies with strong problem-solving skills. " +
                "Experience with React, Node.js, and Spring Boot. Passionate about building scalable applications and modern UI/UX. " +
                "Interested in startups and innovative tech companies where I can wear multiple hats and contribute to product development.");
        seekingPost3.setSkPostSkills("JavaScript, React, Node.js, Spring Boot, MongoDB, MySQL, AWS, CI/CD, Docker, REST APIs");
        seekingPost3.setDesiredSalary(2000.0);
        seekingPost3.setDesiredLocation("Da Nang");
        seekingPost3.setExpiryDate(LocalDate.now().plusMonths(1));
        seekingPost3.setSkPostStatus(SeekingPostStatus.HIDDEN);
        seekingPostRepository.save(seekingPost3);

        log.info("✓ Created Seeking Post 3: Full Stack Developer - Nguyen Van A (HIDDEN) (Code: {})", seekingPost3.getSkPostCode());

        log.info("✓ Created 3 seeking posts (2 ACTIVE, 1 HIDDEN)");
    }
        
}
