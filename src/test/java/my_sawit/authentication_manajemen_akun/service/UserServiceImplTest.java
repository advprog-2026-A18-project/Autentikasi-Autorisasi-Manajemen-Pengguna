package my_sawit.authentication_manajemen_akun.service;

import my_sawit.authentication_manajemen_akun.dto.request.UserUpdateRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.model.Role;
import my_sawit.authentication_manajemen_akun.model.User;
import my_sawit.authentication_manajemen_akun.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MandorProfileRepository mandorProfileRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockBuruh;
    private User mockMandor;
    private MandorProfile mockMandorProfile;

    @BeforeEach
    void setUp() {
        Role buruhRole = Role.builder().name("BURUH").build();
        Role mandorRole = Role.builder().name("MANDOR").build();

        mockMandor = User.builder()
                .id(UUID.randomUUID())
                .email("mandor@sawit.com")
                .fullname("Andi Mandor")
                .username("andi_mandor")
                .role(mandorRole)
                .build();

        mockBuruh = User.builder()
                .id(UUID.randomUUID())
                .email("buruh@sawit.com")
                .username("budi_buruh")
                .fullname("Budi Buruh")
                .role(buruhRole)
                .mandor(mockMandor)
                .build();

        mockMandorProfile = MandorProfile.builder()
                .nomorSertifikasi("MNDR-999")
                .user(mockMandor)
                .build();
    }

    @Test
    void getMyProfile_Buruh_ShouldReturnProfileSuccessfully() {
        String email = "buruh@sawit.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockBuruh));

        UserResponseDTO result = userService.getMyProfile(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("Budi Buruh", result.getFullname());
        assertEquals("BURUH", result.getRole());
        assertEquals("Andi Mandor", result.getNamaMandor());
        assertNull(result.getNomorSertifikasi());

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void getMyProfile_Mandor_ShouldReturnProfileSuccessfully() {
        String email = "mandor@sawit.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockMandor));
        when(mandorProfileRepository.findByUser(mockMandor)).thenReturn(Optional.of(mockMandorProfile));

        UserResponseDTO result = userService.getMyProfile(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("MANDOR", result.getRole());
        assertEquals("MNDR-999", result.getNomorSertifikasi());
        assertNull(result.getNamaMandor());
    }

    @Test
    void getMyProfile_ShouldThrowException_WhenEmailNotFound() {
        String email = "ghost@sawit.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getMyProfile(email);
        });

        assertEquals("Profil tidak ditemukan", exception.getMessage());
    }

    // update-profile

    @Test
    void updateMyProfile_ShouldUpdateSuccessfully() {
        String email = "buruh@sawit.com";
        UserUpdateRequestDTO requestDTO =
                UserUpdateRequestDTO.builder()
                        .fullname("Budi Santoso")
                        .username("budi_santoso")
                        .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockBuruh));
        when(userRepository.existsByUsername("budi_santoso")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponseDTO result = userService.updateMyProfile(email, requestDTO);

        assertNotNull(result);
        assertEquals("Budi Santoso", result.getFullname());
        assertEquals("budi_santoso", result.getUsername());


        verify(userRepository, times(1)).save(mockBuruh);
    }

    @Test
    void updateMyProfile_ShouldThrowException_WhenUsernameAlreadyTaken() {
        String email = "buruh@sawit.com";
        UserUpdateRequestDTO requestDTO =
                UserUpdateRequestDTO.builder()
                        .fullname("Budi Santoso")
                        .username("mandor_agus")
                        .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockBuruh));
        when(userRepository.existsByUsername("mandor_agus")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateMyProfile(email, requestDTO);
        });

        assertEquals("Username sudah digunakan oleh pengguna lain", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateMyProfile_ShouldThrowException_WhenEmailNotFound() {
        String email = "ghost@sawit.com";
        UserUpdateRequestDTO requestDTO =
                UserUpdateRequestDTO.builder()
                        .fullname("Hantu")
                        .username("hantu123")
                        .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateMyProfile(email, requestDTO);
        });

        assertEquals("Profil tidak ditemukan", exception.getMessage());
        verify(userRepository, never()).save(any());
    }
}