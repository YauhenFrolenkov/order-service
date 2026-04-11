package com.innowise.order.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleUserNotAuthenticatedException() {
        UserNotAuthenticatedException ex = new UserNotAuthenticatedException("User not authenticated");

        ResponseEntity<Map<String, Object>> response = handler.handleUserNotAuthenticated(ex);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("User not authenticated", response.getBody().get("message"));
        assertEquals("Unauthorized", response.getBody().get("error"));
    }

    @Test
    void shouldHandleGenericException() {
        Exception ex = new Exception("Something went wrong");

        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Something went wrong", response.getBody().get("message"));
        assertEquals("Internal Server Error", response.getBody().get("error"));
    }

    @Test
    void shouldHandleOrderNotFoundException() {
        OrderNotFoundException ex = new OrderNotFoundException("Order not found");

        ResponseEntity<Map<String, Object>> response = handler.handleOrderNotFound(ex);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Order not found", response.getBody().get("message"));
    }

    @Test
    void shouldHandleItemNotFoundException() {
        ItemNotFoundException ex = new ItemNotFoundException("Item not found");

        ResponseEntity<Map<String, Object>> response = handler.handleItemNotFound(ex);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Item not found", response.getBody().get("message"));
    }

    @Test
    void shouldHandleAccessDeniedException() {
        var ex = new org.springframework.security.access.AccessDeniedException("Denied");

        ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(ex);

        assertEquals(403, response.getStatusCode().value());
        assertEquals("Access Denied", response.getBody().get("message"));
        assertEquals("Forbidden", response.getBody().get("error"));
    }
}

