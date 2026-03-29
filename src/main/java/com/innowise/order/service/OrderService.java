package com.innowise.order.service;

import com.innowise.order.entity.Order;
import com.innowise.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    Order createOrder(Order order);
    Order getOrderById(Long id);
    Page<Order> getOrders(LocalDateTime from, LocalDateTime to, List<OrderStatus> statuses, Long userId, Pageable pageable);
    List<Order> getOrdersByUserId(Long userId);
    Order updateOrder(Long id, Order updatedOrder);
    void deleteOrder(Long id);
}
