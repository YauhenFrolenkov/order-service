package com.innowise.order.security;

import com.innowise.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {

    private final OrderRepository orderRepository;

    public boolean isOrderOwner(Long orderId, Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();

        return orderRepository.findById(orderId)
                .map(order -> order.getUserId().equals(currentUserId))
                .orElse(false);
    }
}