package my_sawit.authentication_manajemen_akun.user.service;

import my_sawit.authentication_manajemen_akun.dto.request.UserUpdateRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;

public interface UserService {
    ApiResponse<UserResponseDTO> getMyProfile(String email);
    ApiResponse<UserResponseDTO> updateMyProfile(String email, UserUpdateRequestDTO request);
}