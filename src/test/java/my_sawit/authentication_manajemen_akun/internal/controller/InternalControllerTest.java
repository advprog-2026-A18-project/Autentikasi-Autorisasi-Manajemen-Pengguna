package my_sawit.authentication_manajemen_akun.internal.controller;

import my_sawit.authentication_manajemen_akun.admin.service.AdminService;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.PagingResponseDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalController.class)
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
        PagingResponseDTO<UserResponseDTO> pagingData = PagingResponseDTO.<UserResponseDTO>builder()
                .content(List.of(mockUser))
                .currentPage(0)
                .totalPages(1)
                .totalElements(1)
                .build();

        when(adminService.searchUsers(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(ApiResponse.success("Successfully fetched users list", pagingData));

        mockMvc.perform(get("/internal/user/search")
                        .param("name", "Evan")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Successfully fetched users list"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].username").value("Evan Haryo"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void searchUsers_HappyPath_ReturnsEmptyMessage() throws Exception {
        PagingResponseDTO<UserResponseDTO> pagingData = PagingResponseDTO.<UserResponseDTO>builder()
                .content(List.of())
                .currentPage(0)
                .totalPages(0)
                .totalElements(0)
                .build();

        when(adminService.searchUsers(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(ApiResponse.success("No users fetched", pagingData));

        mockMvc.perform(get("/internal/user/search")
                        .param("name", "OrangTidakDikenal")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("No users fetched"))
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void searchUsers_UnhappyPath_ServiceThrowsException() throws Exception {
        when(adminService.searchUsers(any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Database timeout"));

        mockMvc.perform(get("/internal/user/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Database timeout"));
    }

    @Test
    void getUserDetail_HappyPath_ReturnsUser() throws Exception {
        when(adminService.getUserDetail(eq(mockUserId)))
                .thenReturn(ApiResponse.success("Berhasil mengambil detail pengguna", mockUser));

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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("User tidak ditemukan"));
    }
}
