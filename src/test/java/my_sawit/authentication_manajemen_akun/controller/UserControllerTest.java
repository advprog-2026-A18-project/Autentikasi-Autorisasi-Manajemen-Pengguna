package my_sawit.authentication_manajemen_akun.controller;

import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = () -> "budi@sawit.com";
    }

    @Test
    void getMyProfile_ShouldReturn200AndProfileData() {
        UserResponseDTO mockResponse = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .email("budi@sawit.com")
                .fullname("Budi Buruh")
                .role("BURUH")
                .build();

        when(userService.getMyProfile("budi@sawit.com")).thenReturn(mockResponse);

        ResponseEntity<ApiResponse<UserResponseDTO>> response = userController.getMyProfile(mockPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals("Berhasil mengambil profil", response.getBody().getMessage());
        assertEquals("budi@sawit.com", response.getBody().getData().getEmail());

        verify(userService, times(1)).getMyProfile("budi@sawit.com");
    }

    @Test
    void getMyProfile_ShouldThrowException_WhenProfileNotFound() {
        when(userService.getMyProfile("budi@sawit.com"))
                .thenThrow(new RuntimeException("Profil tidak ditemukan"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userController.getMyProfile(mockPrincipal);
        });

        assertEquals("Profil tidak ditemukan", exception.getMessage());
    }

    // update-profile

    @Test
    void updateMyProfile_ShouldReturn200AndUpdatedProfileData() {
        UserUpdateRequestDTO requestDTO =
                UserUpdateRequestDTO.builder()
                        .fullname("Budi Santoso")
                        .username("budi_santoso")
                        .build();

        UserResponseDTO mockResponse = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .email("budi@sawit.com")
                .fullname("Budi Santoso")
                .username("budi_santoso")
                .role("BURUH")
                .build();

        when(userService.updateMyProfile("budi@sawit.com", requestDTO)).thenReturn(mockResponse);

        ResponseEntity<ApiResponse<UserResponseDTO>> response = userController.updateMyProfile(mockPrincipal, requestDTO);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals("Berhasil memperbarui profil", response.getBody().getMessage());
        assertEquals("Budi Santoso", response.getBody().getData().getFullname());
        assertEquals("budi_santoso", response.getBody().getData().getUsername());

        verify(userService, times(1)).updateMyProfile("budi@sawit.com", requestDTO);
    }

    @Test
    void updateMyProfile_ShouldThrowException_WhenUsernameTaken() {
        UserUpdateRequestDTO requestDTO =
                UserUpdateRequestDTO.builder()
                        .fullname("Budi")
                        .username("mandor_agus")
                        .build();

        when(userService.updateMyProfile("budi@sawit.com", requestDTO))
                .thenThrow(new IllegalArgumentException("Username sudah digunakan oleh pengguna lain"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userController.updateMyProfile(mockPrincipal, requestDTO);
        });

        assertEquals("Username sudah digunakan oleh pengguna lain", exception.getMessage());
    }
}