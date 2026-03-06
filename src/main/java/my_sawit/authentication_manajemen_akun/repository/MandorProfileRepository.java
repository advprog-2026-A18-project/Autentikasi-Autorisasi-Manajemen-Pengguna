package my_sawit.authentication_manajemen_akun.repository;

import my_sawit.authentication_manajemen_akun.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MandorProfileRepository extends JpaRepository<MandorProfile, UUID> {

    boolean existsByNomorSertifikasi(String nomorSertifikasi);
    Optional<MandorProfile> findByUser(User user);
}
