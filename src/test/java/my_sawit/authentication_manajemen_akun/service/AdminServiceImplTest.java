package my_sawit.authentication_manajemen_akun.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull; 
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MandorProfileRepository mandorProfileRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockBuruh;
    private User mockBuruh2;
    private User mockSupir;
    private User mockMandor;
    private MandorProfile mockMandorProfile;

    @BeforeEach
    void setUp() {
        Role buruhRole = Role.builder().name("BURUH").build();
        Role supirRole = Role.builder().name("SUPIR").build();
        Role mandorRole = Role.builder().name("MANDOR").build();

        mockBuruh = User.builder()
                .id(UUID.randomUUID())
                .fullname("Bambang S")
                .username("bambangz")
                .email("bambang@sawit.com")
                .role(buruhRole)
                .build();

        mockBuruh2 = User.builder()
                .id(UUID.randomUUID())
                .fullname("Bambang G")
                .username("bambang_g")
                .email("bambangg@sawit.com")
                .role(buruhRole)
                .build();

        mockSupir = User.builder()
                .id(UUID.randomUUID())
                .fullname("Agus S")
                .username("aguz")
                .email("agus@sawit.com")
                .role(supirRole)
                .build();

        mockMandor = User.builder()
                .id(UUID.randomUUID())
                .username("mandor agus")
                .email("mandoragus@sawit.com")
                .fullname("Mandor A")
                .role(mandorRole)
                .build();

        mockMandorProfile = MandorProfile.builder()
                .nomorSertifikasi("MNDR-001")
                .build();
    }

    @Test
    void searchUsers_ByFullname_ShouldReturnMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(List.of(mockBuruh));

        when(userRepository.searchUsers(eq("Bambang S"), isNull(), isNull(), eq(pageable)))
                .thenReturn(mockPage);

        Page<UserResponseDTO> result = userService.searchUsers("Bambang S", null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1L, result.getTotalElements()); 
        assertEquals("Bambang S", result.getContent().getFirst().getFullname());
    }

    @Test
    void searchUsers_ByUsername_ShouldReturnMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(List.of(mockBuruh));

        when(userRepository.searchUsers(eq("bambangz"), isNull(), isNull(), eq(pageable)))
                .thenReturn(mockPage);

        Page<UserResponseDTO> result = userService.searchUsers("bambangz", null, null, 0, 10);

        assertNotNull(result);
        assertEquals("bambangz", result.getContent().getFirst().getUsername());
    }

    @Test
    void searchUsers_ByEmail_ShouldReturnMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(List.of(mockSupir));

        when(userRepository.searchUsers(isNull(), eq("agus@sawit.com"), isNull(), eq(pageable)))
                .thenReturn(mockPage);

        Page<UserResponseDTO> result = userService.searchUsers(null, "agus@sawit.com", null, 0, 10);

        assertNotNull(result);
        assertEquals("agus@sawit.com", result.getContent().getFirst().getEmail());
    }

    @Test
    void searchUsers_WithAllFilters_ShouldReturnMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(List.of(mockSupir));

        when(userRepository.searchUsers(eq("aguz"), eq("agus@sawit.com"), eq("SUPIR"), eq(pageable)))
                .thenReturn(mockPage);

        Page<UserResponseDTO> result = userService.searchUsers("aguz", "agus@sawit.com", "SUPIR", 0, 10);

        assertNotNull(result);
        assertEquals("Agus S", result.getContent().getFirst().getFullname());
        assertEquals("SUPIR", result.getContent().getFirst().getRole());
    }

    @Test
    void searchUsers_ShouldReturnMultipleResults_WhenMultipleMatchesFound() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(List.of(mockBuruh, mockBuruh2));

        when(userRepository.searchUsers(eq("Bambang"), isNull(), isNull(), eq(pageable)))
                .thenReturn(mockPage);

        Page<UserResponseDTO> result = userService.searchUsers("Bambang", null, null, 0, 10);

        assertNotNull(result);
        assertEquals(2L, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        assertEquals("Bambang S", result.getContent().getFirst().getFullname());
        assertEquals("Bambang G", result.getContent().get(1).getFullname());
    }

    @Test
    void searchUsers_WithInvalidRole_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.searchUsers(null, null, "HACKER", 0, 10);
        });

        assertEquals("Role tidak valid: HACKER", exception.getMessage());
    }

    @Test
    void searchUsers_WithMandorRole_ShouldMapNomorSertifikasi() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(List.of(mockMandor));

        when(userRepository.searchUsers(isNull(), isNull(), eq("MANDOR"), eq(pageable)))
                .thenReturn(mockPage);
        when(mandorProfileRepository.findByUser(mockMandor))
                .thenReturn(Optional.of(mockMandorProfile));

        Page<UserResponseDTO> result = userService.searchUsers(null, null, "mandor", 0, 10);

        assertNotNull(result);
        assertEquals("MANDOR", result.getContent().getFirst().getRole());
        assertEquals("MNDR-001", result.getContent().getFirst().getNomorSertifikasi());
    }

    @Test
    void searchUsers_WithMandorRoleButNoProfile_ShouldMapSertifikasiAsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(List.of(mockMandor));

        when(userRepository.searchUsers(isNull(), isNull(), eq("MANDOR"), eq(pageable)))
                .thenReturn(mockPage);
        when(mandorProfileRepository.findByUser(mockMandor))
                .thenReturn(Optional.empty());

        Page<UserResponseDTO> result = userService.searchUsers(null, null, "MANDOR", 0, 10);

        assertNotNull(result);
        assertNull(result.getContent().getFirst().getNomorSertifikasi());
    }

    @Test
    void searchUsers_WithNullRoleUser_ShouldMapRoleAsNull() {
        User mockNoRoleUser = User.builder()
                .id(UUID.randomUUID())
                .fullname("Anonym")
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(List.of(mockNoRoleUser));

        when(userRepository.searchUsers(isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(mockPage);

        Page<UserResponseDTO> result = userService.searchUsers(null, null, null, 0, 10);


        assertNotNull(result);
        assertNull(result.getContent().getFirst().getRole());
    }
}