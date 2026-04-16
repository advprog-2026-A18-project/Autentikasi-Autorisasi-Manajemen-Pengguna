package my_sawit.authentication_manajemen_akun.service;

import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.model.Role;
import my_sawit.authentication_manajemen_akun.model.User;
import my_sawit.authentication_manajemen_akun.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.repository.RefreshTokenRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MandorProfileRepository mandorProfileRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockBuruh;
    private User mockBuruh2;
    private User mockSupir;
    private User mockMandor;
    private User mockMandor2;
    private User mockAdmin;
    private MandorProfile mockMandorProfile;

    @BeforeEach
    void setUp() {
        Role buruhRole = Role.builder().name("BURUH").build();
        Role supirRole = Role.builder().name("SUPIR").build();
        Role mandorRole = Role.builder().name("MANDOR").build();
        Role adminRole = Role.builder().name("ADMIN").build();

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

        mockMandor2 = User.builder()
                .id(UUID.randomUUID())
                .username("mandor_b")
                .email("mandorb@sawit.com")
                .fullname("Mandor B")
                .role(mandorRole)
                .build();

        mockAdmin = User.builder()
                .id(UUID.randomUUID())
                .fullname("Admin Utama")
                .username("admin_utama")
                .email("admin@sawit.com")
                .role(adminRole)
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

    // ASSIGNING BURUH-MANDOR

    @Test
    void assignMandor_ShouldAssignSuccessfully() {
        UUID buruhId = mockBuruh.getId();
        UUID mandorId = mockMandor.getId();

        when(userRepository.findById(buruhId)).thenReturn(Optional.of(mockBuruh));
        when(userRepository.findById(mandorId)).thenReturn(Optional.of(mockMandor));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponseDTO result = userService.assignMandor(buruhId, mandorId);

        assertNotNull(result);
        assertEquals(mockMandor, mockBuruh.getMandor());
    }

    @Test
    void assignMandor_ShouldReassignSuccessfully() {
        mockBuruh.setMandor(mockMandor);
        UUID buruhId = mockBuruh.getId();
        UUID mandor2Id = mockMandor2.getId();

        when(userRepository.findById(buruhId)).thenReturn(Optional.of(mockBuruh));
        when(userRepository.findById(mandor2Id)).thenReturn(Optional.of(mockMandor2));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponseDTO result = userService.assignMandor(buruhId, mandor2Id);

        assertNotNull(result);
        assertEquals(mockMandor2, mockBuruh.getMandor());
        assertNotEquals(mockMandor, mockBuruh.getMandor());
    }

    @Test
    void assignMandor_ShouldThrowException_WhenBuruhNotFound() {
        UUID fiktifId = UUID.randomUUID();
        UUID mandorId = mockMandor.getId();

        when(userRepository.findById(fiktifId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.assignMandor(fiktifId, mandorId);
        });

        assertEquals("Data Buruh tidak ditemukan", exception.getMessage());
    }

    @Test
    void assignMandor_ShouldThrowException_WhenMandorNotFound() {
        UUID buruhId = mockBuruh.getId();
        UUID fiktifId = UUID.randomUUID();

        when(userRepository.findById(buruhId)).thenReturn(Optional.of(mockBuruh));
        when(userRepository.findById(fiktifId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.assignMandor(buruhId, fiktifId);
        });

        assertEquals("Data Mandor tidak ditemukan", exception.getMessage());
    }

    @Test
    void assignMandor_ShouldThrowException_WhenTargetIsNotBuruh() {
        UUID supirId = mockSupir.getId();
        UUID mandorId = mockMandor.getId();

        when(userRepository.findById(supirId)).thenReturn(Optional.of(mockSupir));
        when(userRepository.findById(mandorId)).thenReturn(Optional.of(mockMandor));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.assignMandor(supirId, mandorId);
        });

        assertEquals("Pengguna yang ditugaskan harus memiliki role BURUH.", exception.getMessage());
    }

    @Test
    void assignMandor_ShouldThrowException_WhenSupervisorIsNotMandor() {
        UUID buruhId = mockBuruh.getId();
        UUID atasanId = mockSupir.getId();

        when(userRepository.findById(buruhId)).thenReturn(Optional.of(mockBuruh));
        when(userRepository.findById(atasanId)).thenReturn(Optional.of(mockSupir));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.assignMandor(buruhId, atasanId);
        });

        assertEquals("Target atasan harus memiliki role MANDOR.", exception.getMessage());
    }


    @Test
    void unassignMandor_ShouldUnassignSuccessfully() {
        mockBuruh.setMandor(mockMandor);
        UUID buruhId = mockBuruh.getId();

        when(userRepository.findById(buruhId)).thenReturn(Optional.of(mockBuruh));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponseDTO result = userService.unassignMandor(buruhId);

        assertNotNull(result);
        assertNull(mockBuruh.getMandor());
    }

    @Test
    void unassignMandor_ShouldThrowException_WhenTargetIsNotBuruh() {
        mockMandor.setMandor(mockMandor2);
        UUID targetId = mockMandor.getId();

        when(userRepository.findById(targetId)).thenReturn(Optional.of(mockMandor));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.unassignMandor(targetId);
        });

        assertEquals("Hanya role BURUH yang dapat dicopot penugasannya.", exception.getMessage());
    }

    // delete-by-admin

    @Test
    void deleteUser_ShouldDeleteSuccessfully() {
        UUID targetId = mockBuruh.getId();
        String currentAdminEmail = "admin@sawit.com";

        when(userRepository.findById(targetId)).thenReturn(Optional.of(mockBuruh));

        when(userRepository.findByMandor(mockBuruh)).thenReturn(Collections.emptyList());
        when(refreshTokenRepository.findByUser(mockBuruh)).thenReturn(Optional.empty());

        userService.deleteUser(targetId, currentAdminEmail);

        verify(userRepository, times(2)).flush();

        verify(userRepository, times(1)).deleteById(targetId);
    }

    @Test
    void deleteUser_ShouldBuruhReferToMandorWillNull_WhenDeletingMandor() {
        UUID targetId = mockMandor.getId();
        String currentAdminEmail = "admin@sawit.com";

        User buruh1 = User.builder().id(UUID.randomUUID()).fullname("Bawahan 1").mandor(mockMandor).build();
        User buruh2 = User.builder().id(UUID.randomUUID()).fullname("Bawahan 2").mandor(mockMandor).build();
        List<User> kumpulanBuruh = List.of(buruh1, buruh2);

        when(userRepository.findById(targetId)).thenReturn(Optional.of(mockMandor));
        when(userRepository.findByMandor(mockMandor)).thenReturn(kumpulanBuruh);
        when(refreshTokenRepository.findByUser(mockMandor)).thenReturn(Optional.empty());
        when(mandorProfileRepository.findByUser(mockMandor)).thenReturn(Optional.empty());

        userService.deleteUser(targetId, currentAdminEmail);

        assertNull(buruh1.getMandor());
        assertNull(buruh2.getMandor());

        verify(userRepository, times(1)).save(buruh1);
        verify(userRepository, times(1)).save(buruh2);

        verify(userRepository, times(2)).flush();

        verify(userRepository, times(1)).deleteById(targetId);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenDeletingSelf() {
        UUID targetId = mockAdmin.getId();
        String currentAdminEmail = "admin@sawit.com";

        when(userRepository.findById(targetId)).thenReturn(Optional.of(mockAdmin));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(targetId, currentAdminEmail);
        });

        assertEquals("Admin tidak dapat menghapus dirinya sendiri.", exception.getMessage());

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        UUID targetId = UUID.randomUUID();
        String currentAdminEmail = "admin@sawit.com";

        when(userRepository.findById(targetId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(targetId, currentAdminEmail);
        });

        assertEquals("Data pengguna tidak ditemukan", exception.getMessage());
    }


}