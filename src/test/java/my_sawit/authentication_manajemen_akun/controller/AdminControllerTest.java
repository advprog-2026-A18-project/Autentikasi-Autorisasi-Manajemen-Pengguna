package my_sawit.authentication_manajemen_akun.controller;
import my_sawit.authentication_manajemen_akun.dto.request.UserSearchRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.service.UserService;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

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
        when(userService.searchUsers(isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(mockPage);

        UserSearchRequestDTO requestDTO = new UserSearchRequestDTO();

        ResponseEntity<ApiResponse<Page<UserResponseDTO>>> response =
                userController.searchUsers(requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals("Berhasil mengambil daftar pengguna", response.getBody().getMessage());

        Page<UserResponseDTO> responseData = response.getBody().getData();
        assertEquals(1, responseData.getTotalElements());
        assertEquals("Budi Santoso", responseData.getContent().get(0).getFullname());
    }

    @Test
    void searchUsers_WithParams_ShouldReturnFilteredData() {

        Page<UserResponseDTO> mockPage = new PageImpl<>(List.of(mockUserMandor));
        when(userService.searchUsers(eq("Andi"), eq("andi@sawit.com"), eq("MANDOR"), eq(1), eq(5)))
                .thenReturn(mockPage);


        UserSearchRequestDTO requestDTO = UserSearchRequestDTO.builder()
                .name("Andi")
                .email("andi@sawit.com")
                .role("MANDOR")
                .page(1)
                .size(5)
                .build();


        ResponseEntity<ApiResponse<Page<UserResponseDTO>>> response =
                userController.searchUsers(requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Page<UserResponseDTO> responseData = response.getBody().getData();
        assertEquals("Andi Mandor", responseData.getContent().get(0).getFullname());
        assertEquals("MANDOR", responseData.getContent().get(0).getRole());
    }

    @Test
    void searchUsers_WithInvalidRole_ShouldThrowException() {

        when(userService.searchUsers(isNull(), isNull(), eq("HACKER"), eq(0), eq(10)))
                .thenThrow(new IllegalArgumentException("Role tidak valid: HACKER"));


        UserSearchRequestDTO requestDTO = UserSearchRequestDTO.builder()
                .role("HACKER")
                .build();


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userController.searchUsers(requestDTO);
        });

        assertEquals("Role tidak valid: HACKER", exception.getMessage());
    }
}