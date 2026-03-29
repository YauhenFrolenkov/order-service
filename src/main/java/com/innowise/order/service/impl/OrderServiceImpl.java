package com.innowise.order.service.impl;

import com.innowise.order.entity.Order;
import com.innowise.order.entity.OrderStatus;
import com.innowise.order.repository.OrderRepository;
import com.innowise.order.service.OrderService;
import com.innowise.order.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl  implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Order createOrder(Order order) {
        order.setId(null);
        order.setStatus(OrderStatus.CREATED);
        order.setDeleted(false);

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            order.getItems().forEach(item -> item.setOrder(order));
        }

        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException("Order not found with id: " + id));
    }

    @Override
    public Page<Order> getOrders(LocalDateTime from,
                                 LocalDateTime to,
                                 List<OrderStatus> statuses,
                                 Long userId,
                                 Pageable pageable) {

        Specification<Order> spec = OrderSpecification.notDeleted()
                .and(OrderSpecification.hasUserId(userId))
                .and(OrderSpecification.hasStatuses(statuses))
                .and(OrderSpecification.createdAfter(from))
                .and(OrderSpecification.createdBefore(to));

        return orderRepository.findAll(spec, pageable);
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdAndDeletedFalse(userId);
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, Order updatedOrder) {
        Order existing = getOrderById(id);

        existing.setStatus(updatedOrder.getStatus());
        existing.setTotalPrice(updatedOrder.getTotalPrice());

        return orderRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        order.setDeleted(true);

        orderRepository.save(order);
    }
}
