package com.innowise.order.service.impl;

import com.innowise.order.client.UserServiceClient;
import com.innowise.order.dto.response.OrderResponseDto;
import com.innowise.order.dto.response.UserResponseDto;
import com.innowise.order.entity.Item;
import com.innowise.order.entity.Order;
import com.innowise.order.entity.OrderStatus;
import com.innowise.order.exception.OrderNotFoundException;
import com.innowise.order.mapper.OrderMapper;
import com.innowise.order.repository.ItemRepository;
import com.innowise.order.repository.OrderRepository;
import com.innowise.order.service.OrderService;
import com.innowise.order.specification.OrderSpecification;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl  implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public OrderResponseDto createOrder(Order order) {
        order.setId(null);
        order.setStatus(OrderStatus.CREATED);
        order.setDeleted(false);

        if (order.getItems() != null) {
            order.getItems().forEach(orderItem -> {

                Item itemFromDb = itemRepository.findById(orderItem.getItem().getId())
                        .orElseThrow(() ->
                                new RuntimeException("Item not found with id: " + orderItem.getItem().getId())
                        );

                orderItem.setItem(itemFromDb);
                orderItem.setOrder(order);
            });
        }

        order.setTotalPrice(calculateTotalPrice(order));

        Order saved = orderRepository.save(order);

        return orderMapper.toDto(saved);
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "getOrderByIdFallback")
    public OrderResponseDto getOrderById(Long id, String email) {

        Order order = getEntityById(id);
        OrderResponseDto dto = orderMapper.toDto(order);

        UserResponseDto user = userServiceClient.getUserByEmail(email);

        dto.setUserEmail(user.getEmail());
        dto.setUserName(user.getName() + " " + user.getSurname());

        return dto;
    }

    public OrderResponseDto getOrderByIdFallback(Long id, String email, Throwable ex) {

        Order order = getEntityById(id);
        OrderResponseDto dto = orderMapper.toDto(order);

        dto.setUserEmail(email);
        dto.setUserName("Unknown User");

        return dto;
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "getOrdersFallback")
    public Page<OrderResponseDto> getOrders(LocalDateTime from,
                                 LocalDateTime to,
                                 List<OrderStatus> statuses,
                                 Long userId,
                                 String email,
                                 Pageable pageable) {

        Specification<Order> spec = OrderSpecification.notDeleted()
                .and(OrderSpecification.hasUserId(userId))
                .and(OrderSpecification.hasStatuses(statuses))
                .and(OrderSpecification.createdAfter(from))
                .and(OrderSpecification.createdBefore(to));

        Page<Order> orders = orderRepository.findAll(spec, pageable);

        return orders.map(order -> {
            OrderResponseDto dto = orderMapper.toDto(order);

            UserResponseDto user = userServiceClient.getUserByEmail(email);

            dto.setUserEmail(user.getEmail());
            dto.setUserName(user.getName() + " " + user.getSurname());

            return dto;
        });
    }

    public Page<OrderResponseDto> getOrdersFallback(
            LocalDateTime from,
            LocalDateTime to,
            List<OrderStatus> statuses,
            Long userId,
            String email,
            Pageable pageable,
            Throwable ex
    ) {

        Page<Order> orders = orderRepository.findAll(pageable);

        return orders.map(order -> {
            OrderResponseDto dto = orderMapper.toDto(order);

            dto.setUserEmail(email);
            dto.setUserName("Unknown User");

            return dto;
        });
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "getOrdersByUserFallback")
    public List<OrderResponseDto> getOrdersByUserId(Long userId, String email) {
        List<Order> orders = orderRepository.findByUserIdAndDeletedFalse(userId);

        return orders.stream()
                .map(order -> {
                    OrderResponseDto dto = orderMapper.toDto(order);

                    UserResponseDto user = userServiceClient.getUserByEmail(email);

                    dto.setUserEmail(user.getEmail());
                    dto.setUserName(user.getName() + " " + user.getSurname());

                    return dto;
                })
                .toList();
    }

    public List<OrderResponseDto> getOrdersByUserFallback(Long userId, String email, Throwable ex) {

        List<Order> orders = orderRepository.findByUserIdAndDeletedFalse(userId);

        return orders.stream()
                .map(order -> {
                    OrderResponseDto dto = orderMapper.toDto(order);

                    dto.setUserEmail(email);
                    dto.setUserName("Unknown User");

                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrder(Long id, Order updatedOrder) {
        Order existing = getEntityById(id);

        existing.setStatus(updatedOrder.getStatus());

        if (updatedOrder.getItems() != null) {
            existing.getItems().clear();

            updatedOrder.getItems().forEach(orderItem -> {

                Item itemFromDb = itemRepository.findById(orderItem.getItem().getId())
                        .orElseThrow(() ->
                                new RuntimeException("Item not found with id: " + orderItem.getItem().getId())
                        );

                orderItem.setItem(itemFromDb);
                orderItem.setOrder(existing);

                existing.getItems().add(orderItem);
            });

            existing.setTotalPrice(calculateTotalPrice(existing));
        }

        Order saved = orderRepository.save(existing);

        return orderMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = getEntityById(id);
        order.setDeleted(true);

        orderRepository.save(order);
    }

    private Order getEntityById(Long id) {
        return orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found with id: " + id));
    }

    private BigDecimal calculateTotalPrice(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return order.getItems().stream()
                .map(item -> item.getItem().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
