package my_sawit.authentication_manajemen_akun.service;

import my_sawit.authentication_manajemen_akun.dto.request.LoginRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.RegisterRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import my_sawit.authentication_manajemen_akun.model.MandorProfile;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalAuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private MandorProfileRepository mandorProfileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private LocalAuthServiceImpl authService;

    private RegisterRequestDTO registerReq;
    private LoginRequestDTO loginReq;
    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        mockRole = Role.builder().id(UUID.randomUUID()).name("BURUH").build();

        mockUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .fullname("Test User")
                .email("test@example.com")
                .password("hashedPassword")
                .role(mockRole)
                .authProvider("LOCAL")
                .build();

        registerReq = RegisterRequestDTO.builder()
                .username("testuser")
                .fullname("Test User")
                .email("test@example.com")
                .password("password123")
                .role("BURUH")
                .build();

        loginReq = LoginRequestDTO.builder()
                .email("test@example.com")
                .password("password123")
                .build();
    }

    // ==========================================
    // TESTS UNTUK REGISTER
    // ==========================================

    @Test
    void register_WhenUsernameExists_ShouldReturn400() {
        when(userRepository.existsByUsername(registerReq.getUsername())).thenReturn(true);

        ApiResponse<AuthResponseDTO> response = authService.register(registerReq);

        assertEquals(400, response.getStatusCode());
        assertEquals("Username is already used", response.getMessage());
    }

    @Test
    void register_WhenEmailExists_ShouldReturn400() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(registerReq.getEmail())).thenReturn(true);

        ApiResponse<AuthResponseDTO> response = authService.register(registerReq);

        assertEquals(400, response.getStatusCode());
        assertEquals("Email is already registered", response.getMessage());
    }

    @Test
    void register_WhenRoleIsAdmin_ShouldReturn403() {
        registerReq.setRole("ADMIN");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        ApiResponse<AuthResponseDTO> response = authService.register(registerReq);

        assertEquals(403, response.getStatusCode());
        assertEquals("Registration as ADMIN is not allowed", response.getMessage());
    }

    @Test
    void register_WhenRoleNotFound_ShouldThrowException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(registerReq));
        assertTrue(exception.getMessage().contains("Role tidak valid"));
    }

    @Test
    void register_SuccessBuruh_ShouldReturn201() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("BURUH")).thenReturn(Optional.of(mockRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtUtils.generateToken(anyString(), anyString())).thenReturn("mockJwtToken");

        ApiResponse<AuthResponseDTO> response = authService.register(registerReq);

        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getData().getAccessToken());
        assertEquals("BURUH", response.getData().getUser().getRole());
        assertNull(response.getData().getUser().getNomorSertifikasi());
        verify(mandorProfileRepository, never()).save(any());
    }

    @Test
    void register_WhenRoleMandorAndNomorSertifikasiBlank_ShouldReturn400() {
        registerReq.setRole("MANDOR");
        registerReq.setNomorSertifikasi("");
        mockRole.setName("MANDOR");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("MANDOR")).thenReturn(Optional.of(mockRole));

        ApiResponse<AuthResponseDTO> response = authService.register(registerReq);

        assertEquals(400, response.getStatusCode());
        assertEquals("Mandor must fill Nomor Sertifikasi", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void register_WhenRoleMandorAndNomorSertifikasiExists_ShouldReturn400() {
        registerReq.setRole("MANDOR");
        registerReq.setNomorSertifikasi("MND123");
        mockRole.setName("MANDOR");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("MANDOR")).thenReturn(Optional.of(mockRole));
        when(mandorProfileRepository.existsByNomorSertifikasi(anyString())).thenReturn(true);

        ApiResponse<AuthResponseDTO> response = authService.register(registerReq);

        assertEquals(400, response.getStatusCode());
        assertEquals("Nomor Sertifikasi is already registered", response.getMessage());
    }

    @Test
    void register_SuccessMandor_ShouldReturn201() {
        registerReq.setRole("MANDOR");
        registerReq.setNomorSertifikasi("MND123");
        mockRole.setName("MANDOR");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("MANDOR")).thenReturn(Optional.of(mockRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(mandorProfileRepository.existsByNomorSertifikasi(anyString())).thenReturn(false);
        when(jwtUtils.generateToken(anyString(), anyString())).thenReturn("mockJwtToken");

        ApiResponse<AuthResponseDTO> response = authService.register(registerReq);

        assertEquals(201, response.getStatusCode());
        assertEquals("MND123", response.getData().getUser().getNomorSertifikasi());
        verify(mandorProfileRepository, times(1)).save(any(MandorProfile.class));
    }

    // ==========================================
    // TESTS UNTUK LOGIN
    // ==========================================

    @Test
    void login_WhenUserNotFound_ShouldReturn401() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        ApiResponse<AuthResponseDTO> response = authService.login(loginReq);

        assertEquals(401, response.getStatusCode());
        assertEquals("Incorrect email or password", response.getMessage());
    }

    @Test
    void login_WhenPasswordIsNull_ShouldReturn400() {
        mockUser.setPassword(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        ApiResponse<AuthResponseDTO> response = authService.login(loginReq);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getMessage().contains("Google Auth"));
    }

    @Test
    void login_WhenPasswordDoesNotMatch_ShouldReturn401() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        ApiResponse<AuthResponseDTO> response = authService.login(loginReq);

        assertEquals(401, response.getStatusCode());
        assertEquals("Incorrect email or password", response.getMessage());
    }

    @Test
    void login_SuccessBuruh_ShouldReturn200() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtils.generateToken(anyString(), anyString())).thenReturn("mockJwtToken");

        ApiResponse<AuthResponseDTO> response = authService.login(loginReq);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getData().getAccessToken());
        assertNull(response.getData().getUser().getNomorSertifikasi());
    }

    @Test
    void login_SuccessMandorWithProfile_ShouldReturn200() {
        mockRole.setName("MANDOR");
        MandorProfile profile = MandorProfile.builder().nomorSertifikasi("MND123").build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(mandorProfileRepository.findByUser(mockUser)).thenReturn(Optional.of(profile));
        when(jwtUtils.generateToken(anyString(), anyString())).thenReturn("mockJwtToken");

        ApiResponse<AuthResponseDTO> response = authService.login(loginReq);

        assertEquals(200, response.getStatusCode());
        assertEquals("MND123", response.getData().getUser().getNomorSertifikasi());
    }

    @Test
    void login_SuccessMandorWithoutProfile_ShouldReturn200() {
        mockRole.setName("MANDOR");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(mandorProfileRepository.findByUser(mockUser)).thenReturn(Optional.empty());
        when(jwtUtils.generateToken(anyString(), anyString())).thenReturn("mockJwtToken");

        ApiResponse<AuthResponseDTO> response = authService.login(loginReq);

        assertEquals(200, response.getStatusCode());
        assertNull(response.getData().getUser().getNomorSertifikasi());
    }
}