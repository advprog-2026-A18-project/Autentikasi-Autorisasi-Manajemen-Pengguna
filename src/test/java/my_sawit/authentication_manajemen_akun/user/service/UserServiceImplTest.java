package my_sawit.authentication_manajemen_akun.user.service;

import my_sawit.authentication_manajemen_akun.common.exception.BadRequestException;
import my_sawit.authentication_manajemen_akun.common.exception.NotFoundException;
import my_sawit.authentication_manajemen_akun.common.mapper.UserResponseMapper;
import my_sawit.authentication_manajemen_akun.dto.request.UserUpdateRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.domain.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.domain.model.Role;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private UserServiceImpl userService;

    private User mockBuruh;
    private User mockMandor;
    private MandorProfile mockMandorProfile;

    @BeforeEach
    void setUp() {
        UserResponseMapper userResponseMapper = new UserResponseMapper(mandorProfileRepository);
        userService = new UserServiceImpl(userRepository, userResponseMapper);

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

        ApiResponse<UserResponseDTO> response = userService.getMyProfile(email);
        UserResponseDTO result = response.getData();

        assertEquals(200, response.getStatusCode());
        assertEquals("Successfully fetched profile", response.getMessage());
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

        ApiResponse<UserResponseDTO> response = userService.getMyProfile(email);
        UserResponseDTO result = response.getData();

        assertEquals(200, response.getStatusCode());
        assertEquals("Successfully fetched profile", response.getMessage());
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

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.getMyProfile(email);
        });

        assertEquals("Profile not found", exception.getMessage());
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

        ApiResponse<UserResponseDTO> response = userService.updateMyProfile(email, requestDTO);
        UserResponseDTO result = response.getData();

        assertEquals(200, response.getStatusCode());
        assertEquals("Successfully updated profile", response.getMessage());
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

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.updateMyProfile(email, requestDTO);
        });

        assertEquals("Username is already registered by someone else", exception.getMessage());
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

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.updateMyProfile(email, requestDTO);
        });

        assertEquals("Profile not found", exception.getMessage());
        verify(userRepository, never()).save(any());
    }
}
