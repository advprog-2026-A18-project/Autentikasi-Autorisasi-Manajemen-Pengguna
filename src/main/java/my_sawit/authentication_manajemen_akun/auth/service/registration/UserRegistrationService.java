package my_sawit.authentication_manajemen_akun.auth.service.registration;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.common.exception.BadRequestException;
import my_sawit.authentication_manajemen_akun.common.exception.ForbiddenException;
import my_sawit.authentication_manajemen_akun.common.helper.CheckerHelper;
import my_sawit.authentication_manajemen_akun.domain.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.domain.model.Role;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.RoleRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MANDOR = "MANDOR";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MandorProfileRepository mandorProfileRepository;

    @Transactional
    public RegisteredUser register(RegistrationCommand command) {
        if (ROLE_ADMIN.equalsIgnoreCase(command.role())) {
            throw new ForbiddenException("Registration as ADMIN is not allowed");
        }

        Role userRole = roleRepository.findByName(command.role().toUpperCase())
                .orElseThrow(() -> new BadRequestException("Role invalid: " + command.role()));

        CheckerHelper.NomorSertifikasiCheckResult sertifikasiCheck =
                CheckerHelper.validateNomorSertifikasiForMandor(
                        userRole,
                        command.nomorSertifikasi(),
                        mandorProfileRepository
                );

        if (!sertifikasiCheck.valid()) {
            throw new BadRequestException(sertifikasiCheck.message());
        }

        User user = User.builder()
                .username(command.username())
                .fullname(command.fullname())
                .email(command.email())
                .password(command.encodedPassword())
                .role(userRole)
                .authProvider(command.authProvider())
                .build();

        User savedUser = userRepository.save(user);

        if (ROLE_MANDOR.equalsIgnoreCase(userRole.getName())) {
            MandorProfile mandorProfile = MandorProfile.builder()
                    .user(savedUser)
                    .nomorSertifikasi(sertifikasiCheck.nomorSertifikasi())
                    .build();
            mandorProfileRepository.save(mandorProfile);
        }

        return new RegisteredUser(savedUser, sertifikasiCheck.nomorSertifikasi());
    }
}
