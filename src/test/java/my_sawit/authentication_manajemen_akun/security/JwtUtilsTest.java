package my_sawit.authentication_manajemen_akun.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
        String id = UUID.randomUUID().toString();

        String token = jwtUtils.generateToken(email, role, id );

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
        assertEquals(id, claims.get("id"));
    }

    @Test
    void testGetEmailFromToken_ShouldReturnCorrectEmail() {

        String email = "mandor_andi@sawit.com";
        String token = jwtUtils.generateToken(email, "MANDOR", UUID.randomUUID().toString());

        String extractedEmail = jwtUtils.getEmailFromToken(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    void testGetRoleFromToken_ShouldReturnCorrectRole() {
        String role = "MANDOR";
        String token = jwtUtils.generateToken("mandor_andi@sawit.com", role, UUID.randomUUID().toString());

        String extractedRole = jwtUtils.getRoleFromToken(token);

        assertEquals(role, extractedRole);
    }

    @Test
    void testGetIdFromToken_ShouldReturnCorrectId() {
        String id = UUID.randomUUID().toString();
        String token = jwtUtils.generateToken("mandor_andi@sawit.com", "MANDOR", id);

        String extractedId = jwtUtils.getIdFromToken(token);

        assertEquals(id, extractedId);
    }


    @Test
    void validateToken_WhenTokenIsValid_ShouldReturnTrue() {

        String validToken = jwtUtils.generateToken("mandor_andi@sawit.com", "MANDOR", UUID.randomUUID().toString());

        boolean isValid = jwtUtils.validateToken(validToken);

        assertTrue(isValid);
    }

    @Test
    void validateToken_WhenTokenIsMalformed_ShouldReturnFalse() {
        String malformedToken = "ini.bukan.token.jwt.yang.benar";

        boolean isValid = jwtUtils.validateToken(malformedToken);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WhenTokenIsExpired_ShouldReturnFalse() {
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -1000);
        String expiredToken = jwtUtils.generateToken("mandor_andi@sawit.com", "MANDOR", UUID.randomUUID().toString());

        boolean isValid = jwtUtils.validateToken(expiredToken);

        assertFalse(isValid);

        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", dummyExpirationMs);
    }
}