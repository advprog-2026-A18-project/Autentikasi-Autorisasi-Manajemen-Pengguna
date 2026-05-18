package my_sawit.authentication_manajemen_akun.mandor.service;

import my_sawit.authentication_manajemen_akun.dto.request.BawahanSearchRequestDTO; // <-- Tambahkan import ini
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.domain.model.Role;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MandorServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MandorServiceImpl mandorService;

    private User mockMandor;
    private User mockBuruh;

    @BeforeEach
    void setUp() {
        Role mandorRole = Role.builder().name("MANDOR").build();
        Role buruhRole = Role.builder().name("BURUH").build();

        mockMandor = User.builder()
                .id(UUID.randomUUID())
                .email("mandor@sawit.com")
                .username("andi mandor")
                .fullname("Andi Mandor")
                .role(mandorRole)
                .build();

        mockBuruh = User.builder()
                .id(UUID.randomUUID())
                .email("buruh@sawit.com")
                .username("budi buruh")
                .fullname("Budi Buruh")
                .role(buruhRole)
                .mandor(mockMandor)
                .build();
    }

    @Test
    void getMyBawahan_ShouldReturnList_WhenNoSearchFilter() {
        User bawahan = User.builder().id(UUID.randomUUID()).fullname("Agus Buruh").role(mockBuruh.getRole()).build();

        when(userRepository.findByEmail(mockMandor.getEmail())).thenReturn(Optional.of(mockMandor));
        when(userRepository.findByMandor(mockMandor)).thenReturn(List.of(mockBuruh, bawahan));

        // Menggunakan DTO kosong (tanpa nama pencarian)
        BawahanSearchRequestDTO requestDTO = new BawahanSearchRequestDTO();
        ApiResponse<List<UserResponseDTO>> response =
                mandorService.getMyBawahan(mockMandor.getEmail(), requestDTO);

        List<UserResponseDTO> result = response.getData();

        assertEquals(200, response.getStatusCode());
        assertEquals("Successfully fetched bawahan", response.getMessage());
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Budi Buruh", result.get(0).getFullname());
        assertEquals("Agus Buruh", result.get(1).getFullname());
        verify(userRepository, times(1)).findByMandor(mockMandor);
    }

    @Test
    void getMyBawahan_ShouldReturnFilteredList_WhenSearchNameProvided() {
        String searchName = "Budi";
        // Menggunakan DTO dengan nama pencarian
        BawahanSearchRequestDTO requestDTO = BawahanSearchRequestDTO.builder().name(searchName).build();

        when(userRepository.findByEmail(mockMandor.getEmail())).thenReturn(Optional.of(mockMandor));
        when(userRepository.findByMandorAndFullnameContainingIgnoreCase(mockMandor, searchName))
                .thenReturn(List.of(mockBuruh));

        ApiResponse<List<UserResponseDTO>> response =
                mandorService.getMyBawahan(mockMandor.getEmail(), requestDTO);

        List<UserResponseDTO> result = response.getData();

        assertEquals(200, response.getStatusCode());
        assertEquals("Successfully fetched bawahan", response.getMessage());
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Budi Buruh", result.get(0).getFullname());
        verify(userRepository, times(1)).findByMandorAndFullnameContainingIgnoreCase(mockMandor, searchName);
    }

}
