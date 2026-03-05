package my_sawit.authentication_manajemen_akun.seeder;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.model.Role;
import my_sawit.authentication_manajemen_akun.model.User;
import my_sawit.authentication_manajemen_akun.repository.RoleRepository;
import my_sawit.authentication_manajemen_akun.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String[] roles = {"BURUH", "MANDOR", "ADMIN", "SUPIR"};

        for (String roleName : roles) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role newRole = Role.builder()
                        .name(roleName)
                        .build();
                roleRepository.save(newRole);

                System.out.println("Seeder: Role '" + roleName + "' berhasil ditambahkan ke database.");
            }
        }

        // SEEDNING akun ADMIN
        String adminEmail = "admin@mysawit.com";
        String adminUsername = "admin_utama";

        if (!userRepository.existsByEmail(adminEmail)) {

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role ADMIN tidak ditemukan"));

            // Buat akunnya
            User superAdmin = User.builder()
                    .username(adminUsername)
                    .fullname("Super Administrator")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("AdminRahasia123!"))
                    .role(adminRole)
                    .authProvider("LOCAL")
                    .build();

            userRepository.save(superAdmin);
            System.out.println("Seeder: Akun Admin Utama berhasil dibuat! (Email: " + adminEmail + ")");
        }

    }
}