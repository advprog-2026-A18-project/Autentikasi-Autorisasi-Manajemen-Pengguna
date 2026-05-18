package my_sawit.authentication_manajemen_akun.admin.controller;

import my_sawit.authentication_manajemen_akun.admin.service.AdminService;
import my_sawit.authentication_manajemen_akun.common.exception.BadRequestException;
import my_sawit.authentication_manajemen_akun.common.exception.ForbiddenException;
import my_sawit.authentication_manajemen_akun.common.exception.NotFoundException;
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

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private JwtUtils jwtUtils;

    private UserResponseDTO mockUserBuruh;
    private UserResponseDTO mockUserMandor;

    @BeforeEach
    void setUp() {
        mockUserBuruh = UserResponseDTO.builder()
                .fullname("Budi Santoso")
                .role("BURUH")
                .build();

        mockUserMandor = UserResponseDTO.builder()
                .fullname("Andi Mandor")
                .role("MANDOR")
                .build();
    }

    @Test
    void searchUsers_WithoutParams_ShouldReturnSuccess() throws Exception {
        PagingResponseDTO<UserResponseDTO> pagingData = PagingResponseDTO.<UserResponseDTO>builder()
                .content(List.of(mockUserBuruh))
                .currentPage(0)
                .totalPages(1)
                .totalElements(1)
                .build();

        when(adminService.searchUsers(isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(ApiResponse.success("Successfully fetched users list", pagingData));

        mockMvc.perform(get("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Successfully fetched users list"))
                .andExpect(jsonPath("$.data.content[0].fullname").value("Budi Santoso"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void searchUsers_WithParams_ShouldReturnFilteredData() throws Exception {
        PagingResponseDTO<UserResponseDTO> pagingData = PagingResponseDTO.<UserResponseDTO>builder()
                .content(List.of(mockUserMandor))
                .currentPage(1)
                .totalPages(1)
                .totalElements(1)
                .build();

        when(adminService.searchUsers(eq("Andi"), eq("andi@sawit.com"), eq("MANDOR"), eq(1), eq(5)))
                .thenReturn(ApiResponse.success("Successfully fetched users list", pagingData));

        mockMvc.perform(get("/admin/users")
                        .param("name", "Andi")
                        .param("email", "andi@sawit.com")
                        .param("role", "MANDOR")
                        .param("page", "1")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.content[0].fullname").value("Andi Mandor"))
                .andExpect(jsonPath("$.data.content[0].role").value("MANDOR"));
    }

    @Test
    void searchUsers_EmptyData_ShouldReturnNoUsersFetchedMessage() throws Exception {
        PagingResponseDTO<UserResponseDTO> pagingData = PagingResponseDTO.<UserResponseDTO>builder()
                .content(List.of())
                .currentPage(0)
                .totalPages(0)
                .totalElements(0)
                .build();

        when(adminService.searchUsers(eq("Fiktif"), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(ApiResponse.success("No users fetched", pagingData));

        mockMvc.perform(get("/admin/users")
                        .param("name", "Fiktif")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("No users fetched"))
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void searchUsers_WithInvalidRole_ShouldReturnBadRequest() throws Exception {
        when(adminService.searchUsers(isNull(), isNull(), eq("HACKER"), eq(0), eq(10)))
                .thenThrow(new BadRequestException("Role invalid: HACKER"));

        mockMvc.perform(get("/admin/users")
                        .param("role", "HACKER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Role invalid: HACKER"));
    }

    @Test
    void assignMandor_ShouldReturnSuccessResponse() throws Exception {
        UUID buruhId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();

        UserResponseDTO mockAssignedBuruh = UserResponseDTO.builder()
                .fullname("Budi Santoso")
                .role("BURUH")
                .namaMandor("Andi Mandor")
                .build();

        when(adminService.assignMandor(buruhId, mandorId))
                .thenReturn(ApiResponse.success("Successfully assigned Mandor", mockAssignedBuruh));

        mockMvc.perform(put("/admin/users/{buruhId}/assign-mandor/{mandorId}", buruhId, mandorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Successfully assigned Mandor"))
                .andExpect(jsonPath("$.data.namaMandor").value("Andi Mandor"));
    }

    @Test
    void unassignMandor_ShouldReturnSuccessResponse() throws Exception {
        UUID buruhId = UUID.randomUUID();

        UserResponseDTO mockUnassignedBuruh = UserResponseDTO.builder()
                .fullname("Budi Santoso")
                .role("BURUH")
                .namaMandor(null)
                .build();

        when(adminService.unassignMandor(buruhId))
                .thenReturn(ApiResponse.success("Successfully unassign mandor", mockUnassignedBuruh));

        mockMvc.perform(put("/admin/users/{buruhId}/unassign-mandor", buruhId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Successfully unassign mandor"))
                .andExpect(jsonPath("$.data.namaMandor").doesNotExist());
    }

    @Test
    void assignMandor_ShouldReturnBadRequest_WhenServiceThrowsError() throws Exception {
        UUID supirId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();

        when(adminService.assignMandor(supirId, mandorId))
                .thenThrow(new BadRequestException("The user who will be assigned has to have BURUH role."));

        mockMvc.perform(put("/admin/users/{buruhId}/assign-mandor/{mandorId}", supirId, mandorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The user who will be assigned has to have BURUH role."));
    }

    @Test
    void unassignMandor_ShouldReturnBadRequest_WhenIdNotFound() throws Exception {
        UUID fiktifId = UUID.randomUUID();

        when(adminService.unassignMandor(fiktifId))
                .thenThrow(new NotFoundException("Data Buruh not found"));

        mockMvc.perform(put("/admin/users/{buruhId}/unassign-mandor", fiktifId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Data Buruh not found"));
    }

    @Test
    void deleteUser_ShouldReturnSuccessResponse() throws Exception {
        UUID targetId = UUID.randomUUID();
        Principal mockPrincipal = () -> "admin@sawit.com";

        when(adminService.deleteUser(targetId, "admin@sawit.com"))
                .thenReturn(ApiResponse.success("Successfully deleted users", null));

        mockMvc.perform(delete("/admin/users/{userId}", targetId)
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Successfully deleted users"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void deleteUser_ShouldReturnBadRequest_WhenTryingToDeleteSelf() throws Exception {
        UUID targetId = UUID.randomUUID();
        Principal mockPrincipal = () -> "admin@sawit.com";

        when(adminService.deleteUser(targetId, "admin@sawit.com"))
                .thenThrow(new ForbiddenException("Admin can't be deleted."));

        mockMvc.perform(delete("/admin/users/{userId}", targetId)
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.message").value("Admin can't be deleted."));
    }

    @Test
    void deleteUser_ShouldReturnBadRequest_WhenUserNotFound() throws Exception {
        UUID fiktifId = UUID.randomUUID();
        Principal mockPrincipal = () -> "admin@sawit.com";

        when(adminService.deleteUser(fiktifId, "admin@sawit.com"))
                .thenThrow(new NotFoundException("User data not found"));

        mockMvc.perform(delete("/admin/users/{userId}", fiktifId)
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("User data not found"));
    }

    @Test
    void getUserDetail_ShouldReturnSuccessResponse() throws Exception {
        UUID targetId = UUID.randomUUID();
        UserResponseDTO expectedResponse = UserResponseDTO.builder()
                .id(targetId)
                .fullname("Budi Santoso")
                .role("BURUH")
                .namaMandor("Andi Mandor")
                .build();

        when(adminService.getUserDetail(targetId))
                .thenReturn(ApiResponse.success("Successfully fetched detail user", expectedResponse));

        mockMvc.perform(get("/admin/users/{userId}", targetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Successfully fetched detail user"))
                .andExpect(jsonPath("$.data.id").value(targetId.toString()))
                .andExpect(jsonPath("$.data.fullname").value("Budi Santoso"))
                .andExpect(jsonPath("$.data.namaMandor").value("Andi Mandor"));
    }

    @Test
    void getUserDetail_ShouldReturnBadRequest_WhenUserNotFound() throws Exception {
        UUID fiktifId = UUID.randomUUID();

        when(adminService.getUserDetail(fiktifId))
                .thenThrow(new NotFoundException("User data not found"));

        mockMvc.perform(get("/admin/users/{userId}", fiktifId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("User data not found"));
    }
}
