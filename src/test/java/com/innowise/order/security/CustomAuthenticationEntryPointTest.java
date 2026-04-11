package com.innowise.order.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;

class CustomAuthenticationEntryPointTest {

    private CustomAuthenticationEntryPoint entryPoint;
    private MockHttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @Mock
    private AuthenticationException authException;

    @BeforeEach
    void setUp() {
        entryPoint = new CustomAuthenticationEntryPoint();
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldReturn401_andWriteJsonResponse() throws Exception {
        entryPoint.commence(request, response, authException);

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());

        String body = response.getContentAsString();
        assertTrue(body.contains("Unauthorized"));
        assertTrue(body.contains("Authentication required"));
    }
}
