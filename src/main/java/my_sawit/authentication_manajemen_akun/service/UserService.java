package my_sawit.authentication_manajemen_akun.service;

import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;

public interface UserService {
    UserResponseDTO getMyProfile(String email);
}