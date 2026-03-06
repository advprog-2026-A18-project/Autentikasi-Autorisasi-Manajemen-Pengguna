package my_sawit.authentication_manajemen_akun.service;

import my_sawit.authentication_manajemen_akun.dto.request.LoginRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.RegisterRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;

public interface AuthStrategy {
    ApiResponse<AuthResponseDTO> register(RegisterRequestDTO request);
    ApiResponse<AuthResponseDTO> login(LoginRequestDTO request);
}
