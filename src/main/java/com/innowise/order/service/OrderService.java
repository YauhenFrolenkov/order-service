package com.innowise.order.service;

import com.innowise.order.dto.request.CreateOrderRequestDto;
import com.innowise.order.dto.request.UpdateOrderRequestDto;
import com.innowise.order.dto.response.OrderResponseDto;
import com.innowise.order.entity.Order;
import com.innowise.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(CreateOrderRequestDto dto);
    OrderResponseDto getOrderById(Long id);
    Page<OrderResponseDto> getOrders(LocalDateTime from, LocalDateTime to, List<OrderStatus> statuses, Long userId, Pageable pageable);
    List<OrderResponseDto> getOrdersByUserId(Long userId);
    OrderResponseDto updateOrder(Long id, UpdateOrderRequestDto dto);
    void deleteOrder(Long id);
}
