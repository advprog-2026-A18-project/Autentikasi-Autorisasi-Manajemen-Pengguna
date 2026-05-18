package my_sawit.authentication_manajemen_akun.common.helper;

import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.domain.model.RefreshToken;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.security.JwtUtils;
import my_sawit.authentication_manajemen_akun.auth.service.RefreshTokenService;

public class ConvertResponseHandler {

    public static AuthResponseDTO convertToAuthResponseDTO(
            User user,
            String nomorSertifikasi,
            RefreshTokenService refreshTokenService,
            JwtUtils jwtUtils
    ) {
        String namaMandor = fetchNamaMandor(user);

        UserResponseDTO profileDTO = UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .nomorSertifikasi(nomorSertifikasi)
                .namaMandor(namaMandor)
                .build();

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().getName(), user.getId().toString());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return AuthResponseDTO.builder()
                .accessToken(token)
                .refreshToken(refreshToken.getToken())
                .user(profileDTO)
                .build();
    }

    public static AuthResponseDTO convertToAuthResponseDTO(
            User user,
            String nomorSertifikasi,
            String requestRefreshToken,
            JwtUtils jwtUtils
    ) {
        String namaMandor = fetchNamaMandor(user);

        UserResponseDTO profileDTO = UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .nomorSertifikasi(nomorSertifikasi)
                .namaMandor(namaMandor)
                .build();

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().getName(), user.getId().toString());

        return AuthResponseDTO.builder()
                .accessToken(token)
                .refreshToken(requestRefreshToken)
                .user(profileDTO)
                .build();
    }

    private static String fetchNamaMandor(User user) {
        return (user.getMandor() != null) ? user.getMandor().getFullname() : null;
    }



}
