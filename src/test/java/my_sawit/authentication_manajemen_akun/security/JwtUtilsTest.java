package my_sawit.authentication_manajemen_akun.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    private final String dummySecret = "IniAdalahKunciRahasiaSuperPanjangUntukTesting123!!!";
    private final int dummyExpirationMs = 3600000; // 1 Jam expiry

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();

        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", dummySecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", dummyExpirationMs);
    }

    @Test
    void testGenerateToken_ShouldReturnValidJwtString() {
        String email = "petani@mysawit.com";
        String role = "BURUH";

        String token = jwtUtils.generateToken(email, role);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        String[] tokenParts = token.split("\\.");
        assertEquals(3, tokenParts.length);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(dummySecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(email, claims.getSubject());
        assertEquals(role, claims.get("role"));
    }
}