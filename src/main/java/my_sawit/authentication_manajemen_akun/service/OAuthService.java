package my_sawit.authentication_manajemen_akun.service;

import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;

public interface OAuthService<T> {
    ApiResponse<AuthResponseDTO> authenticate(T request);
}
