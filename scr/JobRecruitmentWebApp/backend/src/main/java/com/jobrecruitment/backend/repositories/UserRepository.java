package com.jobrecruitment.backend.repositories;

import com.jobrecruitment.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository - Repository interface cho User entity
 * 
 * Mô tả:
 * - Cung cấp các phương thức truy vấn database cho User
 * - Kế thừa JpaRepository: CRUD operations tự động
 * - Custom methods: Tìm kiếm theo username, userCode, đếm theo role
 * 
 * Tham khảo: Section 3.1 - User Table
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Tìm kiếm User dựa trên username (email)
     * - Sử dụng trong: Login, Registration validation
     * - Query: WHERE username = :username
     * @param username Email đăng ký
     * @return Optional<User> - Có thể empty nếu không tìm thấy
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Tìm kiếm User dựa trên userCode
     * - Sử dụng trong: Profile retrieval
     * - Query: WHERE userCode = :userCode
     * @param userCode Mã người dùng (AD/DN/UV + 8 số)
     * @return Optional<User> - Có thể empty nếu không tìm thấy
     */
    Optional<User> findByUserCode(String userCode);
    
    /**
     * Kiểm tra username đã tồn tại chưa
     * - Sử dụng trong: Registration validation (prevent duplicate)
     * - Query: SELECT COUNT(*) > 0 WHERE username = :username
     * @param username Email cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     */
    boolean existsByUsername(String username);
    
    /**
     * Kiểm tra userCode đã tồn tại chưa
     * - Sử dụng trong: Code generation validation
     * - Query: SELECT COUNT(*) > 0 WHERE userCode = :userCode
     * @param userCode Mã cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     */
    boolean existsByUserCode(String userCode);
    
    /**
     * Đếm số lượng User theo role
     * - Sử dụng trong: Admin Dashboard statistics
     * - JPQL Query: JOIN với Role entity
     * @param roleCode Mã role (ADM/DN/UV)
     * @return Số lượng User có role đó
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.roleCode = :roleCode")
    long countByRoleCode(String roleCode);
}
