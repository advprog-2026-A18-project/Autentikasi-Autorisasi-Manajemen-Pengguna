package my_sawit.authentication_manajemen_akun.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_sawit.authentication_manajemen_akun.model.Role;
import my_sawit.authentication_manajemen_akun.model.User;
import my_sawit.authentication_manajemen_akun.repository.RoleRepository;
import my_sawit.authentication_manajemen_akun.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.fullname}")
    private String adminFullname;

    @Override
    public void run(String... args) throws Exception {
        String[] roles = {"BURUH", "MANDOR", "ADMIN", "SUPIR"};

        for (String roleName : roles) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role newRole = Role.builder()
                        .name(roleName)
                        .build();
                roleRepository.save(newRole);

                log.info("Seeder: Role '{}' berhasil ditambahkan ke database.", roleName);
            }
        }

        // SEEDNING akun ADMIN

        if (!userRepository.existsByEmail(adminEmail)) {

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role ADMIN tidak ditemukan"));

            // Buat akunnya
            User superAdmin = User.builder()
                    .username(adminUsername)
                    .fullname(adminFullname)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(adminRole)
                    .authProvider("LOCAL")
                    .build();

            userRepository.save(superAdmin);
            log.info("Seeder: Akun Admin Utama berhasil dibuat! (Email: {})", adminEmail);
        }

    }
}