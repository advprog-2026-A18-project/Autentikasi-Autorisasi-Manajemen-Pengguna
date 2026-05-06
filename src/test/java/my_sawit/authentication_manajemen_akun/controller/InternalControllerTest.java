package my_sawit.authentication_manajemen_akun.controller;

import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import my_sawit.authentication_manajemen_akun.security.JwtUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(InternalController.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private JwtUtils jwtUtils;

    private UserResponseDTO mockUser;
    private UUID mockUserId;

    @BeforeEach
    void setUp() {
        mockUserId = UUID.randomUUID();
        mockUser = UserResponseDTO.builder()
                .id(mockUserId)
                .username("Evan Haryo")
                .email("evan@sawit.com")
                .role("ADMIN")
                .build();
    }

    @Test
    void searchUsers_HappyPath_ReturnsData() throws Exception {
        Page<UserResponseDTO> mockPage = new PageImpl<>(List.of(mockUser), PageRequest.of(0, 10), 1);

        when(adminService.searchUsers(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(mockPage);

        mockMvc.perform(get("/internal/user/search")
                        .param("name", "Evan")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Berhasil mengambil daftar pengguna"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].username").value("Evan Haryo"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void searchUsers_HappyPath_ReturnsEmptyMessage() throws Exception {
        Page<UserResponseDTO> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(adminService.searchUsers(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/internal/user/search")
                        .param("name", "OrangTidakDikenal")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("No users fetched")) // Validasi logika ternary if
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void searchUsers_UnhappyPath_ServiceThrowsException() throws Exception {
        when(adminService.searchUsers(any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Database timeout"));

        mockMvc.perform(get("/internal/user/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserDetail_HappyPath_ReturnsUser() throws Exception {
        when(adminService.getUserDetail(eq(mockUserId))).thenReturn(mockUser);

        mockMvc.perform(get("/internal/user/{userId}", mockUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Berhasil mengambil detail pengguna"))
                .andExpect(jsonPath("$.data.id").value(mockUserId.toString()))
                .andExpect(jsonPath("$.data.username").value("Evan Haryo"));
    }

    @Test
    void getUserDetail_UnhappyPath_InvalidUUIDFormat() throws Exception {
        String invalidUuid = "ini-bukan-uuid";

        mockMvc.perform(get("/internal/user/{userId}", invalidUuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserDetail_UnhappyPath_UserNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        when(adminService.getUserDetail(eq(randomId)))
                .thenThrow(new IllegalArgumentException("User tidak ditemukan"));

        mockMvc.perform(get("/internal/user/{userId}", randomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}