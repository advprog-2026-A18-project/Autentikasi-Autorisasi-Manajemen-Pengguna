package my_sawit.authentication_manajemen_akun.service;

import lombok.RequiredArgsConstructor;
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

    @Override
    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // will delete the old token if does exist
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDuration))
                .build();

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


}
