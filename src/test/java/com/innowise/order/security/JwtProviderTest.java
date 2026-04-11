package com.innowise.order.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private static final String TEST_SECRET = "test-jwt-secret-key-for-unit-tests-only-12345";
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(TEST_SECRET);
        jwtProvider.init();
    }

    private String generateToken(String userId, String role) {
        Key key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .signWith(key)
                .compact();
    }

    @Test
    void shouldReturnTrue_whenTokenIsValid() {
        String token = generateToken("1", "ADMIN");

        assertTrue(jwtProvider.validateToken(token));
    }

    @Test
    void shouldReturnFalse_whenTokenIsInvalid() {
        assertFalse(jwtProvider.validateToken("invalid.token"));
    }

    @Test
    void shouldReturnFalse_whenTokenIsEmpty() {
        assertFalse(jwtProvider.validateToken(""));
    }

    @Test
    void shouldExtractUserIdFromToken() {
        String token = generateToken("123", "ADMIN");

        Long userId = jwtProvider.getUserIdFromToken(token);

        assertEquals(123L, userId);
    }

    @Test
    void shouldExtractRoleFromToken() {
        String token = generateToken("123", "ADMIN");

        String role = jwtProvider.getRoleFromToken(token);

        assertEquals("ADMIN", role);
    }

    @Test
    void shouldReturnFalse_whenTokenIsNull() {
        assertFalse(jwtProvider.validateToken(null));
    }

    @Test
    void shouldReturnFalse_whenTokenIsMalformed() {
        assertFalse(jwtProvider.validateToken("abc.def"));
    }
}
