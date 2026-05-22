package my_sawit.authentication_manajemen_akun.common.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_sawit.authentication_manajemen_akun.domain.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.domain.model.RefreshToken;
import my_sawit.authentication_manajemen_akun.domain.model.Role;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.RefreshTokenRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.RoleRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class ProfilingDataSeeder implements CommandLineRunner {

    private static final String PASSWORD = "Password123!";
    private static final String LOCAL_PROVIDER = "LOCAL";
    private static final String GOOGLE_PROVIDER = "GOOGLE";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MandorProfileRepository mandorProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.profiling-seeder.enabled:false}")
    private boolean enabled;

    @Value("${app.profiling-seeder.mandor-count:20}")
    private int mandorCount;

    @Value("${app.profiling-seeder.buruh-count:200}")
    private int buruhCount;

    @Value("${app.profiling-seeder.supir-count:50}")
    private int supirCount;

    @Value("${app.profiling-seeder.google-user-count:10}")
    private int googleUserCount;

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        Role mandorRole = getRole("MANDOR");
        Role buruhRole = getRole("BURUH");
        Role supirRole = getRole("SUPIR");

        String encodedPassword = passwordEncoder.encode(PASSWORD);
        List<User> mandors = seedMandors(mandorRole, encodedPassword);
        seedBuruhan(buruhRole, encodedPassword, mandors);
        seedSupirs(supirRole, encodedPassword);
        seedGoogleUsers(buruhRole);

        log.info(
                "Profiling seeder ready. Password for local dummy users: {}. Mandor={}, Buruh={}, Supir={}, Google={}",
                PASSWORD,
                mandorCount,
                buruhCount,
                supirCount,
                googleUserCount
        );
    }

    private List<User> seedMandors(Role role, String encodedPassword) {
        List<User> mandors = new ArrayList<>();
        for (int i = 1; i <= mandorCount; i++) {
            String code = format(i);
            User mandor = findOrCreateUser(
                    "profiling.mandor" + code,
                    "Profiling Mandor " + code,
                    "profiling.mandor" + code + "@mysawit.test",
                    encodedPassword,
                    role,
                    LOCAL_PROVIDER,
                    null
            );
            ensureMandorProfile(mandor, "CERT-PROFILE-" + code);
            mandors.add(mandor);
        }
        return mandors;
    }

    private void seedBuruhan(Role role, String encodedPassword, List<User> mandors) {
        for (int i = 1; i <= buruhCount; i++) {
            String code = format(i);
            User mandor = mandors.get((i - 1) % mandors.size());
            findOrCreateUser(
                    "profiling.buruh" + code,
                    "Profiling Buruh " + code,
                    "profiling.buruh" + code + "@mysawit.test",
                    encodedPassword,
                    role,
                    LOCAL_PROVIDER,
                    mandor
            );
        }
    }

    private void seedSupirs(Role role, String encodedPassword) {
        for (int i = 1; i <= supirCount; i++) {
            String code = format(i);
            User supir = findOrCreateUser(
                    "profiling.supir" + code,
                    "Profiling Supir " + code,
                    "profiling.supir" + code + "@mysawit.test",
                    encodedPassword,
                    role,
                    LOCAL_PROVIDER,
                    null
            );
            ensureRefreshToken(supir, "profiling-refresh-supir-" + code);
        }
    }

    private void seedGoogleUsers(Role role) {
        for (int i = 1; i <= googleUserCount; i++) {
            String code = format(i);
            findOrCreateUser(
                    "profiling.google" + code,
                    "Profiling Google User " + code,
                    "profiling.google" + code + "@mysawit.test",
                    null,
                    role,
                    GOOGLE_PROVIDER,
                    null
            );
        }
    }

    private User findOrCreateUser(
            String username,
            String fullname,
            String email,
            String encodedPassword,
            Role role,
            String authProvider,
            User mandor
    ) {
        return userRepository.findByEmail(email)
                .map(existing -> updateMandorIfNeeded(existing, mandor))
                .orElseGet(() -> userRepository.save(User.builder()
                        .username(username)
                        .fullname(fullname)
                        .email(email)
                        .password(encodedPassword)
                        .role(role)
                        .authProvider(authProvider)
                        .mandor(mandor)
                        .build()));
    }

    private User updateMandorIfNeeded(User user, User mandor) {
        if (mandor != null && user.getMandor() == null) {
            user.setMandor(mandor);
            return userRepository.save(user);
        }
        return user;
    }

    private void ensureMandorProfile(User mandor, String nomorSertifikasi) {
        if (mandorProfileRepository.findByUser(mandor).isPresent()) {
            return;
        }

        mandorProfileRepository.save(MandorProfile.builder()
                .user(mandor)
                .nomorSertifikasi(nomorSertifikasi)
                .build());
    }

    private void ensureRefreshToken(User user, String token) {
        if (refreshTokenRepository.findByUser(user).isPresent()) {
            return;
        }

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(token + "-" + UUID.randomUUID())
                .expiryDate(Instant.now().plusSeconds(7L * 24L * 60L * 60L))
                .build());
    }

    private Role getRole(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + name));
    }

    private String format(int number) {
        return String.format("%03d", number);
    }
}
