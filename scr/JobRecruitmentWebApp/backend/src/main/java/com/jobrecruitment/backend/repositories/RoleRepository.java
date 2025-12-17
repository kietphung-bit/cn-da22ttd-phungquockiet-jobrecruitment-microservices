package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * RoleRepository - Repository interface cho Role entity
 * 
 * Mô tả:
 * - Cung cấp các phương thức truy vấn database cho Role
 * - Master data: ADM (Admin), DN (Employer), UV (Candidate)
 * - Seed data trong DataSeeder (Section 6)
 * 
 * Tham khảo: Section 3.2 - Role Table
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    /**
     * Tìm kiếm Role dựa trên roleCode
     * - Sử dụng trong: Registration, Role assignment
     * - Query: WHERE roleCode = :roleCode
     * @param roleCode Mã role (ADM/DN/UV)
     * @return Optional<Role> - Có thể empty nếu không tìm thấy
     */
    Optional<Role> findByRoleCode(String roleCode);
    
    /**
     * Kiểm tra roleCode đã tồn tại chưa
     * - Sử dụng trong: DataSeeder validation
     * - Query: SELECT COUNT(*) > 0 WHERE roleCode = :roleCode
     * @param roleCode Mã role cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     */
    boolean existsByRoleCode(String roleCode);
    
    /**
     * Kiểm tra roleName đã tồn tại chưa
     * - Sử dụng trong: Role creation validation
     * - Query: SELECT COUNT(*) > 0 WHERE roleName = :roleName
     * @param roleName Tên role cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     */
    boolean existsByRoleName(String roleName);
}
