package my_sawit.authentication_manajemen_akun.service;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import my_sawit.authentication_manajemen_akun.model.RefreshToken;
import my_sawit.authentication_manajemen_akun.model.User;
import my_sawit.authentication_manajemen_akun.repository.RefreshTokenRepository;
import my_sawit.authentication_manajemen_akun.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${app.jwt.refreshExpirationMs:604800000}")
    private final Long refreshTokenDuration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final MandorProfileRepository mandorProfileRepository;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
            throw new RuntimeException("Refresh token expired. Please login again.");
        }
        return token;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    @Override
    @Transactional
    public AuthResponseDTO refreshAccessToken(String requestRefreshToken) {
        return findByToken(requestRefreshToken)
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {

                    String newAccessToken = jwtUtils.generateToken(user.getEmail(), user.getRole().getName());

                    String nomorSertifikasi = null;
                    if ("MANDOR".equalsIgnoreCase(user.getRole().getName())) {
                        Optional<MandorProfile> mandorProfile = mandorProfileRepository.findByUser(user);
                        if (mandorProfile.isPresent()) {
                            nomorSertifikasi = mandorProfile.get().getNomorSertifikasi();
                        }
                    }

                    UserResponseDTO profileDTO = UserResponseDTO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .fullname(user.getFullname())
                            .email(user.getEmail())
                            .role(user.getRole().getName())
                            .nomorSertifikasi(nomorSertifikasi)
                            .build();

                    return AuthResponseDTO.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(requestRefreshToken)
                            .user(profileDTO)
                            .build();

                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

}
