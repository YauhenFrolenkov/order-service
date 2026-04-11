package com.innowise.order.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;

class CustomAccessDeniedHandlerTest {

    private CustomAccessDeniedHandler handler;
    private MockHttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @Mock
    private AccessDeniedException accessDeniedException;

    @BeforeEach
    void setUp() {
        handler = new CustomAccessDeniedHandler();
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldReturn403_andWriteJsonResponse() throws Exception {
        handler.handle(request, response, accessDeniedException);

        assertEquals(403, response.getStatus());
        assertEquals("application/json", response.getContentType());

        String body = response.getContentAsString();
        assertTrue(body.contains("Forbidden"));
        assertTrue(body.contains("Access denied"));
    }
}
