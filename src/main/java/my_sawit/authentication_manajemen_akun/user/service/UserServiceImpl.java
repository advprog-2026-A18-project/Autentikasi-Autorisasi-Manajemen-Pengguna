package my_sawit.authentication_manajemen_akun.user.service;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.request.UserUpdateRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.common.helper.ConvertResponseHandler;
import my_sawit.authentication_manajemen_akun.model.User;
import my_sawit.authentication_manajemen_akun.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MandorProfileRepository mandorProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profil tidak ditemukan"));

        return ConvertResponseHandler.convertToUserResponseDTO(user, mandorProfileRepository);
    }

    @Override
    @Transactional
    public UserResponseDTO updateMyProfile(String email, UserUpdateRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profil tidak ditemukan"));

        if (!request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username sudah digunakan oleh pengguna lain");
            }
            user.setUsername(request.getUsername());
        }

        user.setFullname(request.getFullname());

        User updatedUser = userRepository.save(user);

        return ConvertResponseHandler.convertToUserResponseDTO(updatedUser, mandorProfileRepository);
    }


}