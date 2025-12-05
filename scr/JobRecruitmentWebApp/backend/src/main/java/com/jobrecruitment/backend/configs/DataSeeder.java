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
 * Data Seeding Component
 * Seeds comprehensive test data into database on application startup
 * 
 * Phase 5: Complete Data Seeding
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
    private final PasswordEncoder passwordEncoder;
    private final CodeGenerator codeGenerator;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedAdmin();
        seedJobCategories();
        seedEmployersAndJobs();
        seedCandidates();
        
        log.info("=== DATA SEEDING COMPLETED ===");
        log.info("Total Users: {}", userRepository.count());
        log.info("Total Companies: {}", companyRepository.count());
        log.info("Total Candidates: {}", candidateRepository.count());
        log.info("Total Job Categories: {}", jobCategoryRepository.count());
        log.info("Total Jobs: {}", jobRepository.count());
    }

    private void seedRoles() {
        if (roleRepository.count() > 0) {
            log.info("Roles already seeded. Skipping...");
            return;
        }

        log.info("Seeding roles...");

        Role adminRole = new Role();
        adminRole.setRoleCode("ADM");
        adminRole.setRoleName("Admin");
        roleRepository.save(adminRole);

        Role companyRole = new Role();
        companyRole.setRoleCode("DN");
        companyRole.setRoleName("Doanh nghiệp");
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
        itCategory.setJcName("IT");
        itCategory.setJcDescription("Information Technology and Software Development");
        itCategory.setJcBaseSalary(15000000.0);
        jobCategoryRepository.save(itCategory);

        JobCategory marketingCategory = new JobCategory();
        marketingCategory.setJcName("Marketing");
        marketingCategory.setJcDescription("Marketing, Advertising, and Brand Management");
        marketingCategory.setJcBaseSalary(12000000.0);
        jobCategoryRepository.save(marketingCategory);

        JobCategory salesCategory = new JobCategory();
        salesCategory.setJcName("Sales");
        salesCategory.setJcDescription("Sales, Business Development, and Customer Relations");
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
        company1.setCompanyName("Tech Corp Vietnam");
        company1.setCompanyDescription("Leading technology company specializing in software development and IT solutions");
        company1.setCompanyAddress("123 Nguyen Hue Street, District 1, Ho Chi Minh City");
        company1.setCompanyWebsite("https://techcorp.vn");
        company1.setCompanyEmail("hr@techcorp.vn");
        company1.setLogoURL("https://example.com/logos/techcorp.png");
        company1.setCompanyStatus(CompanyStatus.ACTIVE);
        companyRepository.save(company1);

        // Jobs for Tech Corp
        JobCategory itCategory = jobCategoryRepository.findAll().stream()
                .filter(jc -> jc.getJcName().equals("IT"))
                .findFirst()
                .orElseThrow();

        Job job1 = new Job();
        job1.setCompany(company1);
        job1.setJobCategory(itCategory);
        job1.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job1.setJobTitle("Senior Java Developer");
        job1.setJobDescription("We are looking for an experienced Java developer to join our backend team");
        job1.setJobRequirement("5+ years Java/Spring Boot, PostgreSQL, Microservices architecture");
        job1.setJobSalary(25000000.0);
        job1.setJobLocation("Ho Chi Minh City");
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
        job2.setJobTitle("Frontend React Developer");
        job2.setJobDescription("Join our frontend team to build modern web applications");
        job2.setJobRequirement("3+ years React, TypeScript, TailwindCSS experience");
        job2.setJobSalary(20000000.0);
        job2.setJobLocation("Ho Chi Minh City");
        job2.setStartDate(LocalDate.now().minusDays(3));
        job2.setEndDate(LocalDate.now().plusDays(27));
        job2.setMaxCandidates(3);
        job2.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job2);

        log.info("✓ Created Employer 1: Tech Corp - Username: techcorp@example.com | Password: company123");

        // Employer 2: Marketing Solutions
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
        company2.setCompanyName("Marketing Solutions Ltd");
        company2.setCompanyDescription("Full-service marketing agency providing digital and traditional marketing services");
        company2.setCompanyAddress("456 Le Loi Boulevard, District 3, Ho Chi Minh City");
        company2.setCompanyWebsite("https://marketingsolutions.vn");
        company2.setCompanyEmail("contact@marketingsolutions.vn");
        company2.setLogoURL("https://example.com/logos/marketing.png");
        company2.setCompanyStatus(CompanyStatus.ACTIVE);
        companyRepository.save(company2);

        // Jobs for Marketing Solutions
        JobCategory marketingCategory = jobCategoryRepository.findAll().stream()
                .filter(jc -> jc.getJcName().equals("Marketing"))
                .findFirst()
                .orElseThrow();

        JobCategory salesCategory = jobCategoryRepository.findAll().stream()
                .filter(jc -> jc.getJcName().equals("Sales"))
                .findFirst()
                .orElseThrow();

        Job job3 = new Job();
        job3.setCompany(company2);
        job3.setJobCategory(marketingCategory);
        job3.setJobCode(codeGenerator.generateCode("VL", code -> 
                jobRepository.findByJobCode(code).isPresent()));
        job3.setJobTitle("Digital Marketing Manager");
        job3.setJobDescription("Lead our digital marketing campaigns and strategy");
        job3.setJobRequirement("4+ years digital marketing, SEO, SEM, Social Media Marketing");
        job3.setJobSalary(22000000.0);
        job3.setJobLocation("Ho Chi Minh City");
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
        job4.setJobTitle("Business Development Executive");
        job4.setJobDescription("Expand our client base and build strategic partnerships");
        job4.setJobRequirement("2+ years B2B sales, excellent communication skills, proven track record");
        job4.setJobSalary(18000000.0);
        job4.setJobLocation("Ho Chi Minh City");
        job4.setStartDate(LocalDate.now().minusDays(2));
        job4.setEndDate(LocalDate.now().plusDays(28));
        job4.setMaxCandidates(2);
        job4.setJobStatus(JobStatus.ACTIVE);
        jobRepository.save(job4);

        log.info("✓ Created Employer 2: Marketing Solutions - Username: marketingsolutions@example.com | Password: company123");
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
}
