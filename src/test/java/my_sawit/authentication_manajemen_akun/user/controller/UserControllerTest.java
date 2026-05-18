package my_sawit.authentication_manajemen_akun.user.controller;

import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.security.JwtUtils;
import my_sawit.authentication_manajemen_akun.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtils jwtUtils;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = () -> "budi@sawit.com";
    }

    @Test
    void getMyProfile_ShouldReturn200AndProfileData() throws Exception {
        UserResponseDTO mockResponse = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .email("budi@sawit.com")
                .fullname("Budi Buruh")
                .role("BURUH")
                .build();

        when(userService.getMyProfile("budi@sawit.com"))
                .thenReturn(ApiResponse.success("Successfully fetched profile", mockResponse));

        mockMvc.perform(get("/users/me")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Successfully fetched profile"))
                .andExpect(jsonPath("$.data.email").value("budi@sawit.com"))
                .andExpect(jsonPath("$.data.fullname").value("Budi Buruh"))
                .andExpect(jsonPath("$.data.role").value("BURUH"));
    }

    @Test
    void getMyProfile_ShouldReturnBadRequest_WhenProfileNotFound() throws Exception {
        when(userService.getMyProfile("budi@sawit.com"))
                .thenThrow(new RuntimeException("Profile not found"));

        mockMvc.perform(get("/users/me")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Profile not found"));
    }

    @Test
    void updateMyProfile_ShouldReturn200AndUpdatedProfileData() throws Exception {
        UserResponseDTO mockResponse = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .email("budi@sawit.com")
                .fullname("Budi Santoso")
                .username("budi_santoso")
                .role("BURUH")
                .build();

        when(userService.updateMyProfile(eq("budi@sawit.com"), any()))
                .thenReturn(ApiResponse.success("Successfully updated profile", mockResponse));

        mockMvc.perform(put("/users/me")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullname": "Budi Santoso",
                                  "username": "budi_santoso"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Successfully updated profile"))
                .andExpect(jsonPath("$.data.fullname").value("Budi Santoso"))
                .andExpect(jsonPath("$.data.username").value("budi_santoso"));
    }

    @Test
    void updateMyProfile_ShouldReturnBadRequest_WhenUsernameTaken() throws Exception {
        when(userService.updateMyProfile(eq("budi@sawit.com"), any()))
                .thenThrow(new IllegalArgumentException("Username is already registered by someone else"));

        mockMvc.perform(put("/users/me")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullname": "Budi",
                                  "username": "mandor_agus"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Username is already registered by someone else"));
    }

    @Test
    void updateMyProfile_ShouldReturnBadRequest_WhenRequestInvalid() throws Exception {
        mockMvc.perform(put("/users/me")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullname": "",
                                  "username": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Data validation failed. Please check your input again."))
                .andExpect(jsonPath("$.data.fullname").value("Nama lengkap tidak boleh kosong"))
                .andExpect(jsonPath("$.data.username").value("Username tidak boleh kosong"));
    }
}
