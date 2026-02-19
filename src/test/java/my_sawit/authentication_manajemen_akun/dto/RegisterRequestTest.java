package my_sawit.authentication_manajemen_akun.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RegisterRequestTest {

    @Test
    void testGetterAndSetter() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Budi");
        request.setEmail("budi@gmail.com");
        request.setPassword("tes");
        request.setRole("Pekerja");

        assertEquals("Budi", request.getName());
        assertEquals("budi@gmail.com", request.getEmail());
        assertEquals("tes", request.getPassword());
        assertEquals("Pekerja", request.getRole());
    }
}