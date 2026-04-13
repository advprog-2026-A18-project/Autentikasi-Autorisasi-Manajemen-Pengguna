package my_sawit.authentication_manajemen_akun.service;


import my_sawit.authentication_manajemen_akun.model.RefreshToken;
import my_sawit.authentication_manajemen_akun.model.User;
import my_sawit.authentication_manajemen_akun.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.repository.RefreshTokenRepository;
import my_sawit.authentication_manajemen_akun.repository.UserRepository;
import my_sawit.authentication_manajemen_akun.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private MandorProfileRepository mandorProfileRepository;

    private RefreshTokenServiceImpl refreshTokenService;

    private User mockUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder()
                .id(userId)
                .username("burhan")
                .email("burhan@sawit.com")
                .build();
        refreshTokenService = new RefreshTokenServiceImpl(
                604800000L,
                refreshTokenRepository,
                userRepository,
                jwtUtils,
                mandorProfileRepository
        );
    }

    @Test
    void createRefreshToken_ShouldReturnValidToken() {
        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));
        when(refreshTokenRepository.findByUser(mockUser)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(token -> token.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(userId);

        assertNotNull(result);
        assertEquals(mockUser, result.getUser());
        assertNotNull(result.getToken());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));
    }

    @Test
    void createRefreshToken_ShouldUpdateExistingToken_WhenTokenAlreadyExists() {
        RefreshToken existingToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .token("old-token-string")
                .expiryDate(Instant.now().minusSeconds(100))
                .build();

        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));
        when(refreshTokenRepository.findByUser(mockUser)).thenReturn(Optional.of(existingToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(token -> token.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(userId);

        assertNotNull(result);
        assertEquals(mockUser, result.getUser());
        assertNotEquals("old-token-string", result.getToken());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));
    }

    @Test
    void verifyExpirationOfValidToken_ShouldReturnToken() {
        RefreshToken validToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        RefreshToken result = refreshTokenService.verifyExpiration(validToken);
        assertEquals(validToken, result);

    }

    @Test
    void createRefreshTokenFailedIfUserNotFound_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            refreshTokenService.createRefreshToken(userId);
        });
    }

    @Test
    void verifyExpirationFailedIfExpiredToken_ShouldThrowException() {
        RefreshToken expiredToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().minusSeconds(10))
                .build();

        assertThrows(RuntimeException.class, () -> {
            refreshTokenService.verifyExpiration(expiredToken);
        });

        verify(refreshTokenRepository, times(1)).delete(expiredToken);

    }


}
