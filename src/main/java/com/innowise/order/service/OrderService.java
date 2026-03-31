package com.innowise.order.service;

import com.innowise.order.dto.response.OrderResponseDto;
import com.innowise.order.entity.Order;
import com.innowise.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(Order order);
    OrderResponseDto getOrderById(Long id, String email);
    Page<OrderResponseDto> getOrders(LocalDateTime from, LocalDateTime to, List<OrderStatus> statuses, Long userId, String email, Pageable pageable);
    List<OrderResponseDto> getOrdersByUserId(Long userId, String email);
    OrderResponseDto updateOrder(Long id, Order updatedOrder);
    void deleteOrder(Long id);
}
