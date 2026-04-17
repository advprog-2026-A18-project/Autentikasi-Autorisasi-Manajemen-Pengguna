package my_sawit.authentication_manajemen_akun.controller;

import my_sawit.authentication_manajemen_akun.dto.request.BawahanSearchRequestDTO; // <-- Tambahkan import ini
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.service.MandorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MandorControllerTest {

    @Mock
    private MandorService mandorService;

    @InjectMocks
    private MandorController mandorController;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = () -> "mandor@sawit.com";
    }

    @Test
    void getMyBawahan_ShouldReturn200AndList() {
        UserResponseDTO bawahanDTO = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .username("budi")
                .fullname("Budi Buruh")
                .role("BURUH")
                .build();

        List<UserResponseDTO> mockList = List.of(bawahanDTO);

        // Buat instance DTO untuk mocking dan request
        BawahanSearchRequestDTO requestDTO = new BawahanSearchRequestDTO();

        when(mandorService.getMyBawahan("mandor@sawit.com", requestDTO)).thenReturn(mockList);

        ResponseEntity<ApiResponse<List<UserResponseDTO>>> response =
                mandorController.getMyBawahan(mockPrincipal, requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals("Berhasil mengambil daftar bawahan", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Budi Buruh", response.getBody().getData().get(0).getFullname());

        verify(mandorService, times(1)).getMyBawahan("mandor@sawit.com", requestDTO);
    }

    @Test
    void getMyBawahan_ShouldThrowException_WhenNotMandor() {
        BawahanSearchRequestDTO requestDTO = new BawahanSearchRequestDTO();

        when(mandorService.getMyBawahan("mandor@sawit.com", requestDTO))
                .thenThrow(new RuntimeException("Akses ditolak: Hanya MANDOR yang dapat melihat daftar bawahan"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mandorController.getMyBawahan(mockPrincipal, requestDTO);
        });

        assertEquals("Akses ditolak: Hanya MANDOR yang dapat melihat daftar bawahan", exception.getMessage());
    }
}