package my_sawit.authentication_manajemen_akun.common.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_sawit.authentication_manajemen_akun.domain.model.Role;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.RoleRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class GeneralSeeder implements CommandLineRunner {

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
        seedRoles();
        seedAdminAccount();
    }

    private void seedRoles() {
        String[] roles = {"BURUH", "MANDOR", "ADMIN", "SUPIR"};

        for (String roleName : roles) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role newRole = Role.builder()
                        .name(roleName)
                        .build();
                roleRepository.save(newRole);

                log.info("Seeder: Role '{}' successfully added to database.", roleName);
            }
        }
    }

    private void seedAdminAccount() {
        if (!userRepository.existsByEmail(adminEmail)) {

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));

            User superAdmin = User.builder()
                    .username(adminUsername)
                    .fullname(adminFullname)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(adminRole)
                    .authProvider("LOCAL")
                    .build();

            userRepository.save(superAdmin);
            log.info("Seeder: Admin Utama successfully created! (Email: {})", adminEmail);
        }
    }
}
