package my_sawit.authentication_manajemen_akun.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import my_sawit.authentication_manajemen_akun.dto.request.GoogleAuthRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import my_sawit.authentication_manajemen_akun.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.model.RefreshToken;
import my_sawit.authentication_manajemen_akun.model.Role;
import my_sawit.authentication_manajemen_akun.model.User;
import my_sawit.authentication_manajemen_akun.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.repository.RoleRepository;
import my_sawit.authentication_manajemen_akun.repository.UserRepository;
import my_sawit.authentication_manajemen_akun.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private MandorProfileRepository mandorProfileRepository;
    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private GoogleIdToken mockIdToken;
    @Mock
    private GoogleIdToken.Payload mockPayload;

    @Spy
    @InjectMocks
    private GoogleAuthServiceImpl authService;

    private GoogleAuthRequestDTO request;
    private User mockUser;
    private Role mockRole;
    private RefreshToken mockRefreshToken;

    @BeforeEach
    void setUp() {
        request = GoogleAuthRequestDTO.builder()
                .idToken("dummy.google.token")
                .build();

        mockRole = Role.builder().id(UUID.randomUUID()).name("BURUH").build();

        mockUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .fullname("Test User")
                .email("test@google.com")
                .role(mockRole)
                .authProvider("GOOGLE")
                .build();

        mockRefreshToken = RefreshToken.builder()
                .token("mock-refresh-token-uuid")
                .build();
    }

    // helper method untuk fake respon dari server Google
    private void mockGoogleTokenSuccess() throws Exception {
        doReturn(mockIdToken).when(authService).verifyGoogleToken(anyString());
        when(mockIdToken.getPayload()).thenReturn(mockPayload);
        when(mockPayload.getEmail()).thenReturn("test@google.com");
        when(mockPayload.get("name")).thenReturn("Test User");
    }

    // VERIFIKASI GOOGLE GAGAL

    @Test
    void authenticate_WhenTokenInvalid_ShouldReturn401() throws Exception {
        doReturn(null).when(authService).verifyGoogleToken(anyString());

        ApiResponse<AuthResponseDTO> response = authService.authenticate(request);

        assertEquals(401, response.getStatusCode());
        assertEquals("Invalid Google Token", response.getMessage());
    }

    @Test
    void authenticate_WhenGoogleThrowsException_ShouldReturn500() throws Exception {
        doThrow(new RuntimeException("Google API Error")).when(authService).verifyGoogleToken(anyString());

        ApiResponse<AuthResponseDTO> response = authService.authenticate(request);

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getMessage().contains("Error while verification"));
    }

    // LOGIN (PENGGUNA SUDAH ADA)

    @Test
    void authenticate_LoginSuccess_AsBuruh_ShouldReturn200() throws Exception {
        mockGoogleTokenSuccess();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(jwtUtils.generateToken(anyString(), anyString())).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(mockRefreshToken);

        ApiResponse<AuthResponseDTO> response = authService.authenticate(request);

        assertEquals(200, response.getStatusCode());
        assertEquals("mock-refresh-token-uuid", response.getData().getRefreshToken());
        assertNull(response.getData().getUser().getNomorSertifikasi());
        verify(mandorProfileRepository, never()).findByUser(any());
    }

    @Test
    void authenticate_LoginSuccess_AsMandorWithProfile_ShouldReturn200() throws Exception {
        mockGoogleTokenSuccess();
        mockRole.setName("MANDOR");
        MandorProfile profile = MandorProfile.builder().nomorSertifikasi("MND123").build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(mandorProfileRepository.findByUser(mockUser)).thenReturn(Optional.of(profile));
        when(jwtUtils.generateToken(anyString(), anyString())).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(mockRefreshToken);

        ApiResponse<AuthResponseDTO> response = authService.authenticate(request);

        assertEquals(200, response.getStatusCode());
        assertEquals("mock-refresh-token-uuid", response.getData().getRefreshToken());
        assertEquals("MND123", response.getData().getUser().getNomorSertifikasi());
    }

    @Test
    void authenticate_LoginSuccess_AsMandorWithoutProfile_ShouldReturn200() throws Exception {
        mockGoogleTokenSuccess();
        mockRole.setName("MANDOR");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(mandorProfileRepository.findByUser(mockUser)).thenReturn(Optional.empty());
        when(jwtUtils.generateToken(anyString(), anyString())).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(mockRefreshToken);

        ApiResponse<AuthResponseDTO> response = authService.authenticate(request);

        assertEquals(200, response.getStatusCode());
        assertEquals("mock-refresh-token-uuid", response.getData().getRefreshToken());
        assertNull(response.getData().getUser().getNomorSertifikasi());
    }

    // REGISTER (PENGGUNA BARU)

    @Test
    void authenticate_Register_WhenRoleNull_ShouldReturn400() throws Exception {
        mockGoogleTokenSuccess();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        request.setRole(null);

        ApiResponse<AuthResponseDTO> response = authService.authenticate(request);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getMessage().contains("Please select one of the roles"));
    }

    @Test
    void authenticate_Register_WhenRoleAdmin_ShouldReturn403() throws Exception {
        mockGoogleTokenSuccess();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        request.setRole("ADMIN");

        ApiResponse<AuthResponseDTO> response = authService.authenticate(request);

        assertEquals(403, response.getStatusCode());
        assertEquals("Registration as ADMIN is not allowed", response.getMessage());
    }

    @Test
    void authenticate_Register_WhenRoleInvalid_ShouldCatchExceptionAndReturn500() throws Exception {
        mockGoogleTokenSuccess();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        request.setRole("INVALID_ROLE");
        when(roleRepository.findByName(anyString())).thenReturn(Optional.empty());

        ApiResponse<AuthResponseDTO> response = authService.authenticate(request);

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getMessage().contains("Role invalid: INVALID_ROLE"));
    }

    @Test
    void authenticate_Register_AsMandor_WhenNomorSertifikasiBlank_ShouldReturn400() throws Exception {
        mockGoogleTokenSuccess();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        request.setRole("MANDOR");
        request.setNomorSertifikasi("");
        mockRole.setName("MANDOR");
        when(roleRepository.findByName("MANDOR")).thenReturn(Optional.of(mockRole));

        ApiResponse<AuthResponseDTO> response = authService.authenticate(request);

        assertEquals(400, response.getStatusCode());
        assertEquals("Mandor must fill Nomor Sertifikasi", response.getMessage());
    }

    @Test
    void authenticate_Register_AsMandor_WhenNomorSertifikasiExists_ShouldReturn400() throws Exception {
        mockGoogleTokenSuccess();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        request.setRole("MANDOR");
        request.setNomorSertifikasi("MND123");
        mockRole.setName("MANDOR");
        when(roleRepository.findByName("MANDOR")).thenReturn(Optional.of(mockRole));
        when(mandorProfileRepository.existsByNomorSertifikasi(anyString())).thenReturn(true);

        ApiResponse<AuthResponseDTO> response = authService.authenticate(request);

        assertEquals(400, response.getStatusCode());
        assertEquals("Nomor Sertifikasi is already registered", response.getMessage());
    }

    @Test
    void authenticate_RegisterSuccess_AsMandor_ShouldReturn200() throws Exception {
        mockGoogleTokenSuccess();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        request.setRole("MANDOR");
        request.setNomorSertifikasi("MND123");
        mockRole.setName("MANDOR");

        when(roleRepository.findByName("MANDOR")).thenReturn(Optional.of(mockRole));
        when(mandorProfileRepository.existsByNomorSertifikasi(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtUtils.generateToken(anyString(), anyString())).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(mockRefreshToken);

        ApiResponse<AuthResponseDTO> response = authService.authenticate(request);

        assertEquals(200, response.getStatusCode());
        verify(mandorProfileRepository, times(1)).save(any(MandorProfile.class));
        assertEquals("mock-refresh-token-uuid", response.getData().getRefreshToken());
        assertEquals("MND123", response.getData().getUser().getNomorSertifikasi());
    }

    @Test
    void authenticate_RegisterSuccess_AsBuruh_ShouldReturn200() throws Exception {
        mockGoogleTokenSuccess();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        request.setRole("BURUH");

        when(roleRepository.findByName("BURUH")).thenReturn(Optional.of(mockRole));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtUtils.generateToken(anyString(), anyString())).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(mockRefreshToken);

        ApiResponse<AuthResponseDTO> response = authService.authenticate(request);

        assertEquals(200, response.getStatusCode());
        verify(mandorProfileRepository, never()).save(any());
        assertEquals("mock-refresh-token-uuid", response.getData().getRefreshToken());
        assertEquals("BURUH", response.getData().getUser().getRole());
    }
}