package com.innowise.order.security;

import com.innowise.order.repository.OrderRepository;
import org.springframework.security.core.Authentication;
import com.innowise.order.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSecurityTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private Authentication authentication;

    private OrderSecurity orderSecurity;

    @BeforeEach
    void setUp() {
        orderSecurity = new OrderSecurity(orderRepository);
    }

    private void mockUser(Long userId) {
        when(authentication.getPrincipal()).thenReturn(userId);
    }

    @Test
    void shouldReturnTrue_whenOwner() {
        mockUser(1L);

        Order order = new Order();
        order.setUserId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        boolean result = orderSecurity.isOrderOwner(1L, authentication);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenNotOwner() {
        mockUser(1L);

        Order order = new Order();
        order.setUserId(2L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        boolean result = orderSecurity.isOrderOwner(1L, authentication);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalse_whenOrderNotFound() {
        mockUser(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = orderSecurity.isOrderOwner(1L, authentication);

        assertFalse(result);
    }
}