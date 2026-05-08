package my_sawit.authentication_manajemen_akun.helper;

import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.model.User;
import my_sawit.authentication_manajemen_akun.repository.MandorProfileRepository;

import java.util.Optional;

public class ConvertResponseHandler {

    public static UserResponseDTO convertToUserResponseDTO(User user, MandorProfileRepository mandorProfileRepository ) {
        String nomorSertifikasi = null;

        if (user.getRole() != null && "MANDOR".equalsIgnoreCase(user.getRole().getName())) {
            Optional<MandorProfile> profile = mandorProfileRepository.findByUser(user);
            if (profile.isPresent()) {
                nomorSertifikasi = profile.get().getNomorSertifikasi();
            }
        }

        String namaMandor = (user.getMandor() != null) ? user.getMandor().getFullname() : null;

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

    public static UserResponseDTO convertToUserResponseDTO(User user) {

        String namaMandor = (user.getMandor() != null) ? user.getMandor().getFullname() : null;

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .nomorSertifikasi(null)
                .namaMandor(namaMandor)
                .build();
    }
}
