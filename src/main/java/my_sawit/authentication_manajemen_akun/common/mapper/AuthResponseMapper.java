package my_sawit.authentication_manajemen_akun.common.mapper;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.security.JwtUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthResponseMapper {

    private final JwtUtils jwtUtils;
    private final UserResponseMapper userResponseMapper;

    public AuthResponseDTO toDto(User user, String refreshToken) {
        return toDto(user, null, refreshToken);
    }

    public AuthResponseDTO toDto(User user, String nomorSertifikasi, String refreshToken) {
        UserResponseDTO profileDTO = nomorSertifikasi == null
                ? userResponseMapper.toDto(user)
                : userResponseMapper.toDto(user, nomorSertifikasi);

        String accessToken = jwtUtils.generateToken(
                user.getEmail(),
                user.getRole().getName(),
                user.getId().toString()
        );

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(profileDTO)
                .build();
    }
}
