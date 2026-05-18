package my_sawit.authentication_manajemen_akun.user.service;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.request.UserUpdateRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.common.helper.ConvertResponseHandler;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MandorProfileRepository mandorProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserResponseDTO> getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        UserResponseDTO data =  ConvertResponseHandler.convertToUserResponseDTO(user, mandorProfileRepository);
        return ApiResponse.success("Successfully fetched profile", data);
    }

    @Override
    @Transactional
    public ApiResponse<UserResponseDTO> updateMyProfile(String email, UserUpdateRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (!request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username is already registered by someone else");
            }
            user.setUsername(request.getUsername());
        }

        user.setFullname(request.getFullname());

        User updatedUser = userRepository.save(user);

        UserResponseDTO data = ConvertResponseHandler.convertToUserResponseDTO(updatedUser, mandorProfileRepository);

        return ApiResponse.success("Successfully updated profile", data);
    }


}