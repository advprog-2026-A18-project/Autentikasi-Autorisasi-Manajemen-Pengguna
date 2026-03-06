package my_sawit.authentication_manajemen_akun.controller;

import my_sawit.authentication_manajemen_akun.dto.request.GoogleAuthRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.LoginRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.RegisterRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import my_sawit.authentication_manajemen_akun.service.AuthStrategy;
import my_sawit.authentication_manajemen_akun.service.GoogleAuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthStrategy authStrategy;

    @Mock
    private GoogleAuthServiceImpl googleAuthService;

    @InjectMocks
    private AuthController authController;

    private AuthResponseDTO dummyAuthData;

    @BeforeEach
    void setUp() {
        dummyAuthData = AuthResponseDTO.builder()
                .accessToken("dummy.jwt.token")
                .build();

    }

    @Test
    void testRegister_ShouldReturn201() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        ApiResponse<AuthResponseDTO> mockResponse = new ApiResponse<>(201, "Registration succeed", dummyAuthData);
        when(authStrategy.register(any(RegisterRequestDTO.class))).thenReturn(mockResponse);


        ResponseEntity<ApiResponse<AuthResponseDTO>> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(201, response.getBody().getStatusCode());
        assertEquals("Registration succeed", response.getBody().getMessage());
    }

    @Test
    void testLogin_ShouldReturn200() {
        LoginRequestDTO request = new LoginRequestDTO();
        ApiResponse<AuthResponseDTO> mockResponse = new ApiResponse<>(200, "Login succeed", dummyAuthData);
        when(authStrategy.login(any(LoginRequestDTO.class))).thenReturn(mockResponse);

        ResponseEntity<ApiResponse<AuthResponseDTO>> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals("Login succeed", response.getBody().getMessage());
    }

    @Test
    void testGoogleLogin_Success_ShouldReturn200() {
        GoogleAuthRequestDTO request = new GoogleAuthRequestDTO();
        ApiResponse<AuthResponseDTO> mockResponse = new ApiResponse<>(200, "Google Auth succeed", dummyAuthData);
        when(googleAuthService.authenticate(any(GoogleAuthRequestDTO.class))).thenReturn(mockResponse);

        ResponseEntity<ApiResponse<AuthResponseDTO>> response = authController.googleLogin(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
    }

    @Test
    void testGoogleLogin_Failed_ShouldReturnErrorStatus() {
        GoogleAuthRequestDTO request = new GoogleAuthRequestDTO();
        ApiResponse<AuthResponseDTO> mockResponse = new ApiResponse<>(401, "Invalid Google Token", null);
        when(googleAuthService.authenticate(any(GoogleAuthRequestDTO.class))).thenReturn(mockResponse);

        ResponseEntity<ApiResponse<AuthResponseDTO>> response = authController.googleLogin(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()); // 401
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatusCode());
    }


}