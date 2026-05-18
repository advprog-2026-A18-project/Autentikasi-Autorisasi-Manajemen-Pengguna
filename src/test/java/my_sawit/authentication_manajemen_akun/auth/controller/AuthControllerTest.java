package my_sawit.authentication_manajemen_akun.auth.controller;

import my_sawit.authentication_manajemen_akun.auth.service.LocalAuthService;
import my_sawit.authentication_manajemen_akun.auth.service.OAuthService;
import my_sawit.authentication_manajemen_akun.auth.service.RefreshTokenService;
import my_sawit.authentication_manajemen_akun.dto.request.GoogleAuthRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.LoginRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.RegisterRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import my_sawit.authentication_manajemen_akun.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocalAuthService localAuthService;

    @MockitoBean
    private OAuthService<GoogleAuthRequestDTO> googleAuthService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private JwtUtils jwtUtils;

    private AuthResponseDTO dummyAuthData;

    @BeforeEach
    void setUp() {
        dummyAuthData = AuthResponseDTO.builder()
                .accessToken("dummy.jwt.token")
                .build();
    }

    @Test
    void testRegister_ShouldReturn201() throws Exception {
        ApiResponse<AuthResponseDTO> mockResponse =
                new ApiResponse<>(201, "Registration succeed", dummyAuthData);

        when(localAuthService.register(any(RegisterRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "budi",
                                  "fullname": "Budi Buruh",
                                  "email": "budi@sawit.com",
                                  "password": "Password123",
                                  "role": "BURUH"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Registration succeed"))
                .andExpect(jsonPath("$.data.accessToken").value("dummy.jwt.token"));
    }

    @Test
    void testLogin_ShouldReturn200() throws Exception {
        ApiResponse<AuthResponseDTO> mockResponse =
                new ApiResponse<>(200, "Login succeed", dummyAuthData);

        when(localAuthService.login(any(LoginRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "budi@sawit.com",
                                  "password": "Password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Login succeed"))
                .andExpect(jsonPath("$.data.accessToken").value("dummy.jwt.token"));
    }

    @Test
    void testGoogleLogin_Success_ShouldReturn200() throws Exception {
        ApiResponse<AuthResponseDTO> mockResponse =
                new ApiResponse<>(200, "Google Auth succeed", dummyAuthData);

        when(googleAuthService.authenticate(any(GoogleAuthRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idToken": "valid-google-token",
                                  "role": "BURUH"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Google Auth succeed"))
                .andExpect(jsonPath("$.data.accessToken").value("dummy.jwt.token"));
    }

    @Test
    void testGoogleLogin_Failed_ShouldReturnErrorStatus() throws Exception {
        ApiResponse<AuthResponseDTO> mockResponse =
                new ApiResponse<>(401, "Invalid Google Token", null);

        when(googleAuthService.authenticate(any(GoogleAuthRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idToken": "invalid-google-token",
                                  "role": "BURUH"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value("Invalid Google Token"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
