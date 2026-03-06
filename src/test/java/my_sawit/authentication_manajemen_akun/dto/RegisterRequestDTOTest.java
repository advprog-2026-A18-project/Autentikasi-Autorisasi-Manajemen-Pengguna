package my_sawit.authentication_manajemen_akun.dto;

import my_sawit.authentication_manajemen_akun.dto.request.RegisterRequestDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RegisterRequestDTOTest {

    @Test
    void testGetterAndSetter() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("Budi");
        request.setFullname("Budi S");
        request.setEmail("budi@gmail.com");
        request.setPassword("tes");
        request.setRole("BURUH");

        assertEquals("Budi", request.getUsername());
        assertEquals("Budi S", request.getFullname());
        assertEquals("budi@gmail.com", request.getEmail());
        assertEquals("tes", request.getPassword());
        assertEquals("BURUH", request.getRole());
    }
}