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
                refreshTokenRepository,
                userRepository,
                jwtUtils,
                mandorProfileRepository
        );
        org.springframework.test.util.ReflectionTestUtils.setField(
                refreshTokenService,
                "refreshTokenDuration",
                604800000L
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

    @Test
    void findByToken_ShouldReturnToken_WhenExists() {
        String tokenString = "some-random-token";
        RefreshToken mockToken = RefreshToken.builder().token(tokenString).build();

        when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(mockToken));

        Optional<RefreshToken> result = refreshTokenService.findByToken(tokenString);

        assertTrue(result.isPresent());
        assertEquals(tokenString, result.get().getToken());
    }

    @Test
    void deleteByToken_ShouldCallRepositoryDelete_WhenTokenExists() {
        String tokenString = "token-to-delete";
        RefreshToken mockToken = RefreshToken.builder().token(tokenString).build();

        when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(mockToken));

        refreshTokenService.deleteByToken(tokenString);

        verify(refreshTokenRepository, times(1)).delete(mockToken);
    }

    @Test
    void refreshAccessToken_ShouldReturnNewTokens_ForNonMandor() {
        String oldToken = "old-refresh-token";
        String newAccessToken = "new-access-token-jwt";

        my_sawit.authentication_manajemen_akun.model.Role roleBuruh =
                new my_sawit.authentication_manajemen_akun.model.Role();
        roleBuruh.setName("BURUH");

        User userBuruh = User.builder()
                .id(UUID.randomUUID())
                .email("buruh@sawit.com")
                .role(roleBuruh)
                .build();

        RefreshToken validRefreshToken = RefreshToken.builder()
                .token(oldToken)
                .user(userBuruh)
                .expiryDate(Instant.now().plusSeconds(1000))
                .build();

        when(refreshTokenRepository.findByToken(oldToken)).thenReturn(Optional.of(validRefreshToken));
        when(jwtUtils.generateToken(userBuruh.getEmail(), roleBuruh.getName(), userBuruh.getId().toString())).thenReturn(newAccessToken);

        // Act
        my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO result =
                refreshTokenService.refreshAccessToken(oldToken);

        // Assert
        assertNotNull(result);
        assertEquals(newAccessToken, result.getAccessToken());
        assertEquals(oldToken, result.getRefreshToken());
        assertEquals("BURUH", result.getUser().getRole());
        assertNull(result.getUser().getNomorSertifikasi()); // Karena bukan mandor
    }

    @Test
    void refreshAccessToken_ShouldReturnNewTokens_WithSertifikasi_ForMandor() {
        String oldToken = "old-refresh-token";
        String newAccessToken = "new-access-token-jwt";

        my_sawit.authentication_manajemen_akun.model.Role roleMandor =
                new my_sawit.authentication_manajemen_akun.model.Role();
        roleMandor.setName("MANDOR");

        User userMandor = User.builder()
                .id(UUID.randomUUID())
                .email("mandor@sawit.com")
                .role(roleMandor)
                .build();

        RefreshToken validRefreshToken = RefreshToken.builder()
                .token(oldToken)
                .user(userMandor)
                .expiryDate(Instant.now().plusSeconds(1000))
                .build();

        my_sawit.authentication_manajemen_akun.model.MandorProfile mandorProfile =
                my_sawit.authentication_manajemen_akun.model.MandorProfile.builder()
                        .nomorSertifikasi("MNDR-12345")
                        .build();

        when(refreshTokenRepository.findByToken(oldToken)).thenReturn(Optional.of(validRefreshToken));
        when(jwtUtils.generateToken(userMandor.getEmail(), roleMandor.getName(), userMandor.getId().toString())).thenReturn(newAccessToken);
        when(mandorProfileRepository.findByUser(userMandor)).thenReturn(Optional.of(mandorProfile));

        my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO result =
                refreshTokenService.refreshAccessToken(oldToken);

        assertNotNull(result);
        assertEquals("MANDOR", result.getUser().getRole());
        assertEquals("MNDR-12345", result.getUser().getNomorSertifikasi()); // Harus ada datanya
    }

    @Test
    void refreshAccessToken_ShouldThrowException_WhenTokenNotFoundInDB() {
        String invalidToken = "token-ngasal";

        when(refreshTokenRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            refreshTokenService.refreshAccessToken(invalidToken);
        });

        assertEquals("Refresh token is not in database!", exception.getMessage());
    }

}
