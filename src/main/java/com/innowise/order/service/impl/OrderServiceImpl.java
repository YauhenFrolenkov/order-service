package com.innowise.order.service.impl;

import com.innowise.order.client.UserServiceClient;
import com.innowise.order.dto.response.OrderResponseDto;
import com.innowise.order.dto.response.UserResponseDto;
import com.innowise.order.entity.Item;
import com.innowise.order.entity.Order;
import com.innowise.order.entity.OrderStatus;
import com.innowise.order.exception.ItemNotFoundException;
import com.innowise.order.exception.OrderNotFoundException;
import com.innowise.order.exception.UserNotAuthenticatedException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private static final String UNKNOWN_EMAIL = "unknown@example.com";
    private static final String UNKNOWN_USER = "Unknown User";

    @Override
    @Transactional
    public OrderResponseDto createOrder(Order order) {
        order.setId(null);
        order.setStatus(OrderStatus.CREATED);
        order.setDeleted(false);

        Long userId = getCurrentUserId();
        order.setUserId(userId);

        if (order.getItems() != null) {
            order.getItems().forEach(orderItem -> {

                Item itemFromDb = itemRepository.findById(orderItem.getItem().getId())
                        .orElseThrow(() ->
                                new ItemNotFoundException("Item not found with id: " + orderItem.getItem().getId())
                        );

                orderItem.setItem(itemFromDb);
                orderItem.setOrder(order);
            });
        }

        order.setTotalPrice(calculateTotalPrice(order));
        Order saved = orderRepository.save(order);

        return enrichWithUser(saved);
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "getOrderByIdFallback")
    public OrderResponseDto getOrderById(Long id) {
        Order order = getEntityById(id);
        return enrichWithUser(order);
    }

    @SuppressWarnings("unused")
    public OrderResponseDto getOrderByIdFallback(Long id, Throwable ex) {

        Order order = getEntityById(id);
        OrderResponseDto dto = orderMapper.toDto(order);
        dto.setUserEmail(UNKNOWN_EMAIL);
        dto.setUserName(UNKNOWN_USER);
        return dto;
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "getOrdersFallback")
    public Page<OrderResponseDto> getOrders(LocalDateTime from, LocalDateTime to, List<OrderStatus> statuses, Long userIdFilter, Pageable pageable) {

        Specification<Order> spec = OrderSpecification.notDeleted()
                .and(OrderSpecification.hasUserId(userIdFilter))
                .and(OrderSpecification.hasStatuses(statuses))
                .and(OrderSpecification.createdAfter(from))
                .and(OrderSpecification.createdBefore(to));

        Page<Order> orders = orderRepository.findAll(spec, pageable);

        return orders.map(this::enrichWithUser);
    }

    @SuppressWarnings("unused")
    public Page<OrderResponseDto> getOrdersFallback(LocalDateTime from, LocalDateTime to, List<OrderStatus> statuses, Long userIdFilter, Pageable pageable, Throwable ex) {

        Page<Order> orders = orderRepository.findAll(pageable);

        return orders.map(order -> {
            OrderResponseDto dto = orderMapper.toDto(order);

            dto.setUserEmail(UNKNOWN_EMAIL);
            dto.setUserName(UNKNOWN_USER);

            return dto;
        });
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "getOrdersByUserFallback")
    public List<OrderResponseDto> getOrdersByUserId(Long userIdFilter) {
        List<Order> orders = orderRepository.findByUserIdAndDeletedFalse(userIdFilter);
        UserResponseDto user = userServiceClient.getUserById(userIdFilter);

        return orders.stream()
                .map(order -> {
                    OrderResponseDto dto = orderMapper.toDto(order);

                    dto.setUserEmail(user.getEmail());
                    dto.setUserName(user.getName() + " " + user.getSurname());

                    return dto;
                })
                .toList();
    }

    @SuppressWarnings("unused")
    public List<OrderResponseDto> getOrdersByUserFallback(Long userIdFilter, Throwable ex) {

        List<Order> orders = orderRepository.findByUserIdAndDeletedFalse(userIdFilter);

        return orders.stream()
                .map(order -> {
                    OrderResponseDto dto = orderMapper.toDto(order);

                    dto.setUserEmail(UNKNOWN_EMAIL);
                    dto.setUserName(UNKNOWN_USER);
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
                                new ItemNotFoundException("Item not found with id: " + orderItem.getItem().getId())
                        );

                orderItem.setItem(itemFromDb);
                orderItem.setOrder(existing);

                existing.getItems().add(orderItem);
            });

            existing.setTotalPrice(calculateTotalPrice(existing));
        }

        Order saved = orderRepository.save(existing);

        return enrichWithUser(saved);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = getEntityById(id);
        order.setDeleted(true);

        orderRepository.save(order);
    }

    private OrderResponseDto enrichWithUser(Order order) {
        OrderResponseDto dto = orderMapper.toDto(order);
        UserResponseDto user = userServiceClient.getUserById(order.getUserId());

        dto.setUserEmail(user.getEmail());
        dto.setUserName(user.getName() + " " + user.getSurname());
        return dto;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Long)) {
            throw new UserNotAuthenticatedException("User is not authenticated");
        }

        return (Long) authentication.getPrincipal();
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
