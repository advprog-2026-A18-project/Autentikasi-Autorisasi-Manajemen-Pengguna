package my_sawit.authentication_manajemen_akun.service;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.model.User;
import my_sawit.authentication_manajemen_akun.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MandorProfileRepository mandorProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> searchUsers(String name, String email, String role, int page, int size) {

        if (role != null && !role.isBlank()) {
            String roleUpper = role.toUpperCase();
            if (!roleUpper.equals("BURUH") && !roleUpper.equals("MANDOR") &&
                    !roleUpper.equals("SUPIR") && !roleUpper.equals("ADMIN")) {
                throw new IllegalArgumentException("Role tidak valid: " + role);
            }
            role = roleUpper;
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<User> usersPage = userRepository.searchUsers(name, email, role, pageable);

        return usersPage.map(this::convertToResponseDTO);
    }

    private UserResponseDTO convertToResponseDTO(User user) {
        String nomorSertifikasi = null;

        if (user.getRole() != null && "MANDOR".equalsIgnoreCase(user.getRole().getName())) {
            Optional<MandorProfile> profile = mandorProfileRepository.findByUser(user);
            if (profile.isPresent()) {
                nomorSertifikasi = profile.get().getNomorSertifikasi();
            }
        }
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .nomorSertifikasi(nomorSertifikasi)
                .build();
    }
}