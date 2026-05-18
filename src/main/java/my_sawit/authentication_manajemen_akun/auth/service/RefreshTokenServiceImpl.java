package my_sawit.authentication_manajemen_akun.auth.service;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.common.exception.NotFoundException;
import my_sawit.authentication_manajemen_akun.common.exception.UnauthorizedException;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.domain.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.domain.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import my_sawit.authentication_manajemen_akun.domain.model.RefreshToken;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.RefreshTokenRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static my_sawit.authentication_manajemen_akun.common.helper.ConvertResponseHandler.convertToAuthResponseDTO;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${app.jwt.refreshExpirationMs:604800000}")
    private Long refreshTokenDuration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final MandorProfileRepository mandorProfileRepository;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);

        RefreshToken refreshToken;
        if (existingToken.isPresent()) {
            refreshToken = existingToken.get();
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDuration));
        } else {
            refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(UUID.randomUUID().toString())
                    .expiryDate(Instant.now().plusMillis(refreshTokenDuration))
                    .build();
        }

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new UnauthorizedException("Refresh token expired. Please login again.");
        }
        return token;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
        return ApiResponse.success("Successfully logout", null);
    }

    @Override
    @Transactional
    public ApiResponse<AuthResponseDTO> refreshAccessToken(String requestRefreshToken) {
        AuthResponseDTO authData = findByToken(requestRefreshToken)
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String nomorSertifikasi = null;
                    if ("MANDOR".equalsIgnoreCase(user.getRole().getName())) {
                        Optional<MandorProfile> mandorProfile = mandorProfileRepository.findByUser(user);
                        if (mandorProfile.isPresent()) {
                            nomorSertifikasi = mandorProfile.get().getNomorSertifikasi();
                        }
                    }
                    return convertToAuthResponseDTO(user, nomorSertifikasi, requestRefreshToken, jwtUtils);
                }).orElseThrow(() -> new UnauthorizedException("Refresh token is not in database!"));
        return ApiResponse.success("Token refreshed successfully", authData);
    }

}
