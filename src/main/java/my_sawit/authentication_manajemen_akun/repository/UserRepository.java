package my_sawit.authentication_manajemen_akun.repository;

import my_sawit.authentication_manajemen_akun.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR " +
            " LOWER(u.fullname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:roleName IS NULL OR u.role.name = :roleName)")
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("email") String email,
                           @Param("roleName") String roleName,
                           Pageable pageable);

}
