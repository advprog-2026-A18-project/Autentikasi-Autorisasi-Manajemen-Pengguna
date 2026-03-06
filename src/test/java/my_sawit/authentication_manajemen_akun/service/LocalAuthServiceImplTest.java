package my_sawit.authentication_manajemen_akun.service;

import my_sawit.authentication_manajemen_akun.dto.request.RegisterRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
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
    private LocalAuthServiceImpl localAuthService;

    private RegisterRequestDTO validBuruhRequest;
    private Role roleBuruh;
    private User savedUser;

    @BeforeEach
    void setUp(){
        validBuruhRequest = RegisterRequestDTO.builder()
                .username("budi_sawit")
                .fullname("Budi S")
                .email("budi@sawit.com")
                .password("Password123!")
                .role("BURUH")
                .build();

        roleBuruh = new Role(UUID.randomUUID(), "BURUH");

        savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("budi_sawit")
                .fullname("Budi S")
                .email("budi@sawit.com")
                .role(roleBuruh)
                .build();
    }

    @Test
    void whenRegisterValidBuruh_thenReturnSuccessAndToken(){
        when(userRepository.existsByUsername(validBuruhRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(validBuruhRequest.getEmail())).thenReturn(false);

        when(roleRepository.findByName("BURUH")).thenReturn(Optional.of(roleBuruh));
        when(passwordEncoder.encode(validBuruhRequest.getPassword())).thenReturn("hashed_password_rahasia");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(jwtUtils.generateToken(anyString(), anyString())).thenReturn("dummy-jwt-token-12345");

        ApiResponse<AuthResponseDTO> response = localAuthService.register(validBuruhRequest);

        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        assertEquals("Registration succeed, You are authenticated", response.getMessage());

        assertNotNull(response.getData());
        assertEquals("dummy-jwt-token-12345", response.getData().getAccessToken());

        assertEquals("budi_sawit", response.getData().getUser().getUsername());
        assertEquals("BURUH", response.getData().getUser().getRole());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void whenRegisterWithExistingEmail_thenReturnBadRequest() {

        when(userRepository.existsByUsername(validBuruhRequest.getUsername())).thenReturn(false);

        when(userRepository.existsByEmail(validBuruhRequest.getEmail())).thenReturn(true);

        ApiResponse<AuthResponseDTO> response = localAuthService.register(validBuruhRequest);

        assertEquals(400, response.getStatusCode());
        assertEquals("Email is already registered", response.getMessage());
        assertNull(response.getData());

        verify(userRepository, never()).save(any(User.class));
    }

}
