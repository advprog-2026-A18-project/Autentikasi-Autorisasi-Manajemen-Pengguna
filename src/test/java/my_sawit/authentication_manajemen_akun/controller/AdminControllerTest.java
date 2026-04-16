package my_sawit.authentication_manajemen_akun.controller;

import my_sawit.authentication_manajemen_akun.dto.request.UserSearchRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.PagingResponseDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

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
    void searchUsers_WithoutParams_ShouldReturnSuccess() {
        Page<UserResponseDTO> mockPage = new PageImpl<>(List.of(mockUserBuruh));
        when(adminService.searchUsers(isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(mockPage);

        UserSearchRequestDTO requestDTO = new UserSearchRequestDTO();

        ResponseEntity<ApiResponse<PagingResponseDTO<UserResponseDTO>>> response =
                adminController.searchUsers(requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals("Berhasil mengambil daftar pengguna", response.getBody().getMessage());

        PagingResponseDTO<UserResponseDTO> responseData = response.getBody().getData();
        assertEquals(1L, responseData.getTotalElements());
        assertEquals("Budi Santoso", responseData.getContent().getFirst().getFullname());
    }

    @Test
    void searchUsers_WithParams_ShouldReturnFilteredData() {
        Page<UserResponseDTO> mockPage = new PageImpl<>(List.of(mockUserMandor));
        when(adminService.searchUsers(eq("Andi"), eq("andi@sawit.com"), eq("MANDOR"), eq(1), eq(5)))
                .thenReturn(mockPage);

        UserSearchRequestDTO requestDTO = UserSearchRequestDTO.builder()
                .name("Andi")
                .email("andi@sawit.com")
                .role("MANDOR")
                .page(1)
                .size(5)
                .build();

        ResponseEntity<ApiResponse<PagingResponseDTO<UserResponseDTO>>> response =
                adminController.searchUsers(requestDTO);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        PagingResponseDTO<UserResponseDTO> responseData = response.getBody().getData();
        assertEquals("Andi Mandor", responseData.getContent().getFirst().getFullname());
        assertEquals("MANDOR", responseData.getContent().getFirst().getRole());
    }

    @Test
    void searchUsers_EmptyData_ShouldReturnNoUsersFetchedMessage() {
        Page<UserResponseDTO> emptyPage = new PageImpl<>(Collections.emptyList());
        when(adminService.searchUsers(eq("Fiktif"), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(emptyPage);

        UserSearchRequestDTO requestDTO = UserSearchRequestDTO.builder()
                .name("Fiktif")
                .build();

        ResponseEntity<ApiResponse<PagingResponseDTO<UserResponseDTO>>> response =
                adminController.searchUsers(requestDTO);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals("No users fetched", response.getBody().getMessage());

        PagingResponseDTO<UserResponseDTO> responseData = response.getBody().getData();
        assertEquals(0L, responseData.getTotalElements());
        assertTrue(responseData.getContent().isEmpty());
    }

    @Test
    void searchUsers_WithInvalidRole_ShouldThrowException() {
        when(adminService.searchUsers(isNull(), isNull(), eq("HACKER"), eq(0), eq(10)))
                .thenThrow(new IllegalArgumentException("Role tidak valid: HACKER"));

        UserSearchRequestDTO requestDTO = UserSearchRequestDTO.builder()
                .role("HACKER")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adminController.searchUsers(requestDTO);
        });

        assertEquals("Role tidak valid: HACKER", exception.getMessage());
    }

    // ASSIGNING BURUH

    @Test
    void assignMandor_ShouldReturnSuccessResponse() {
        UUID buruhId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();

        UserResponseDTO mockAssignedBuruh = UserResponseDTO.builder()
                .fullname("Budi Santoso")
                .role("BURUH")
                .namaMandor("Andi Mandor")
                .build();

        when(adminService.assignMandor(buruhId, mandorId)).thenReturn(mockAssignedBuruh);

        ResponseEntity<ApiResponse<UserResponseDTO>> response =
                adminController.assignMandor(buruhId, mandorId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals("Berhasil menugaskan mandor", response.getBody().getMessage());
        assertEquals("Andi Mandor", response.getBody().getData().getNamaMandor());
    }

    @Test
    void unassignMandor_ShouldReturnSuccessResponse() {
        UUID buruhId = UUID.randomUUID();

        UserResponseDTO mockUnassignedBuruh = UserResponseDTO.builder()
                .fullname("Budi Santoso")
                .role("BURUH")
                .namaMandor(null)
                .build();

        when(adminService.unassignMandor(buruhId)).thenReturn(mockUnassignedBuruh);

        ResponseEntity<ApiResponse<UserResponseDTO>> response =
                adminController.unassignMandor(buruhId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals("Berhasil mencopot penugasan mandor", response.getBody().getMessage());
        assertNull(response.getBody().getData().getNamaMandor());
    }

    @Test
    void assignMandor_ShouldThrowException_WhenServiceThrowsError() {
        UUID supirId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();

        when(adminService.assignMandor(supirId, mandorId))
                .thenThrow(new IllegalArgumentException("Pengguna yang ditugaskan harus memiliki role BURUH."));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adminController.assignMandor(supirId, mandorId);
        });

        assertEquals("Pengguna yang ditugaskan harus memiliki role BURUH.", exception.getMessage());
    }

    @Test
    void unassignMandor_ShouldThrowException_WhenIdNotFound() {
        UUID fiktifId = UUID.randomUUID();

        when(adminService.unassignMandor(fiktifId))
                .thenThrow(new RuntimeException("Data Buruh tidak ditemukan"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminController.unassignMandor(fiktifId);
        });

        assertEquals("Data Buruh tidak ditemukan", exception.getMessage());
    }

    // delete-by-admin

    @Test
    void deleteUser_ShouldReturnSuccessResponse() {
        UUID targetId = UUID.randomUUID();
        Principal mockPrincipal = () -> "admin@sawit.com";

        org.mockito.Mockito.doNothing().when(adminService).deleteUser(targetId, "admin@sawit.com");

        ResponseEntity<ApiResponse<Object>> response = adminController.deleteUser(targetId, mockPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals("Berhasil menghapus pengguna", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void deleteUser_ShouldThrowException_WhenTryingToDeleteSelf() {
        UUID targetId = UUID.randomUUID();
        Principal mockPrincipal = () -> "admin@sawit.com";

        org.mockito.Mockito.doThrow(new IllegalArgumentException("Admin tidak dapat menghapus dirinya sendiri."))
                .when(adminService).deleteUser(targetId, "admin@sawit.com");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adminController.deleteUser(targetId, mockPrincipal);
        });

        assertEquals("Admin tidak dapat menghapus dirinya sendiri.", exception.getMessage());
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        UUID fiktifId = UUID.randomUUID();
        Principal mockPrincipal = () -> "admin@sawit.com";

        org.mockito.Mockito.doThrow(new RuntimeException("Data pengguna tidak ditemukan"))
                .when(adminService).deleteUser(fiktifId, "admin@sawit.com");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminController.deleteUser(fiktifId, mockPrincipal);
        });

        assertEquals("Data pengguna tidak ditemukan", exception.getMessage());
    }

    // admin-fetch-detail-profile-user


    @Test
    void getUserDetail_ShouldReturnSuccessResponse() {
        UUID targetId = UUID.randomUUID();
        UserResponseDTO expectedResponse = UserResponseDTO.builder()
                .id(targetId)
                .fullname("Budi Santoso")
                .role("BURUH")
                .namaMandor("Andi Mandor")
                .build();


        when(adminService.getUserDetail(targetId)).thenReturn(expectedResponse);


        ResponseEntity<ApiResponse<UserResponseDTO>> response = adminController.getUserDetail(targetId);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals("Berhasil mengambil detail pengguna", response.getBody().getMessage());
        assertEquals("Budi Santoso", response.getBody().getData().getFullname());
        assertEquals("Andi Mandor", response.getBody().getData().getNamaMandor());


        verify(adminService, times(1)).getUserDetail(targetId);
    }

    @Test
    void getUserDetail_ShouldThrowException_WhenUserNotFound() {
        UUID fiktifId = UUID.randomUUID();
        when(adminService.getUserDetail(fiktifId))
                .thenThrow(new RuntimeException("Data pengguna tidak ditemukan"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminController.getUserDetail(fiktifId);
        });

        assertEquals("Data pengguna tidak ditemukan", exception.getMessage());
    }

}