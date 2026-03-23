package my_sawit.authentication_manajemen_akun.repository;

import my_sawit.authentication_manajemen_akun.model.RefreshToken;
import my_sawit.authentication_manajemen_akun.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);

    @Modifying
    void deleteByUser(User user);

}
