package my_sawit.authentication_manajemen_akun.seeder;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.model.Role;
import my_sawit.authentication_manajemen_akun.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        String[] roles = {"BURUH", "MANDOR", "ADMIN"};

        for (String roleName : roles) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role newRole = Role.builder()
                        .name(roleName)
                        .build();
                roleRepository.save(newRole);

                System.out.println("Seeder: Role '" + roleName + "' berhasil ditambahkan ke database.");
            }
        }
    }
}