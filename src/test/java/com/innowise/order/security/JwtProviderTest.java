package com.innowise.order.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private static final String TEST_SECRET = "test-jwt-secret-key-for-unit-tests-only-12345";
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(TEST_SECRET);
        jwtProvider.init();
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
    void shouldReturnFalse_whenTokenIsNull() {
        assertFalse(jwtProvider.validateToken(null));
    }
}
