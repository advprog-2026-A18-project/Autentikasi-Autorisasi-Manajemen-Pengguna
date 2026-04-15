package my_sawit.authentication_manajemen_akun.service;

import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.model.Role;
import my_sawit.authentication_manajemen_akun.model.User;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockBuruh;
    private User mockBuruh2;
    private User mockSupir;

    @BeforeEach
    void setUp() {
        Role buruhRole = Role.builder().name("BURUH").build();
        Role supirRole = Role.builder().name("SUPIR").build();

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
    }

    @Test
    void searchUsers_ByFullname_ShouldReturnMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(List.of(mockBuruh));

        when(userRepository.searchUsers(eq("Bambang S"), any(), any(), eq(pageable)))
                .thenReturn(mockPage);

        Page<UserResponseDTO> result = userService.searchUsers("Bambang S", null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Bambang S", result.getContent().getFirst().getFullname());
    }

    @Test
    void searchUsers_ByUsername_ShouldReturnMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(List.of(mockBuruh));

        when(userRepository.searchUsers(eq("bambangz"), any(), any(), eq(pageable)))
                .thenReturn(mockPage);

        Page<UserResponseDTO> result = userService.searchUsers("bambangz", null, null, 0, 10);

        assertNotNull(result);
        assertEquals("bambangz", result.getContent().getFirst().getUsername());
    }

    @Test
    void searchUsers_ByEmail_ShouldReturnMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(List.of(mockSupir));

        when(userRepository.searchUsers(any(), eq("agus@sawit.com"), any(), eq(pageable)))
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

        when(userRepository.searchUsers(eq("Bambang"), any(), any(), eq(pageable)))
                .thenReturn(mockPage);

        Page<UserResponseDTO> result = userService.searchUsers("Bambang", null, null, 0, 10);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements() );
        assertEquals(2, result.getContent().size());

        assertEquals("Bambang S", result.getContent().getFirst().getFullname());
        assertEquals("Bambang G", result.getContent().get(1).getFullname());
    }

}
