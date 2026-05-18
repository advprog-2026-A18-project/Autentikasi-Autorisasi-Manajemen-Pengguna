package my_sawit.authentication_manajemen_akun.common.mapper;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.domain.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserResponseMapper {

    private static final String ROLE_MANDOR = "MANDOR";

    private final MandorProfileRepository mandorProfileRepository;

    public UserResponseDTO toDto(User user) {
        return toDto(user, fetchNomorSertifikasi(user));
    }

    public UserResponseDTO toDto(User user, String nomorSertifikasi) {
        String namaMandor = user.getMandor() != null
                ? user.getMandor().getFullname()
                : null;

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .nomorSertifikasi(nomorSertifikasi)
                .namaMandor(namaMandor)
                .build();
    }

    public UserResponseDTO toDtoWithoutMandorProfile(User user) {
        return toDto(user, null);
    }

    private String fetchNomorSertifikasi(User user) {
        if (user.getRole() == null || !ROLE_MANDOR.equalsIgnoreCase(user.getRole().getName())) {
            return null;
        }

        return mandorProfileRepository.findByUser(user)
                .map(MandorProfile::getNomorSertifikasi)
                .orElse(null);
    }
}
