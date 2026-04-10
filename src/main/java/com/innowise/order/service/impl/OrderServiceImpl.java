package com.innowise.order.service.impl;

import com.innowise.order.client.UserServiceClient;
import com.innowise.order.dto.request.CreateOrderRequestDto;
import com.innowise.order.dto.request.UpdateOrderRequestDto;
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

    @Override
    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequestDto dto) {

        Order order = orderMapper.toEntity(dto);
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
    public OrderResponseDto getOrderById(Long id) {
        Order order = getEntityById(id);
        return enrichWithUser(order);
    }

    @Override
    public Page<OrderResponseDto> getOrders(LocalDateTime from, LocalDateTime to, List<OrderStatus> statuses, Long userIdFilter, Pageable pageable) {

        Specification<Order> spec = OrderSpecification.notDeleted()
                .and(OrderSpecification.hasUserId(userIdFilter))
                .and(OrderSpecification.hasStatuses(statuses))
                .and(OrderSpecification.createdAfter(from))
                .and(OrderSpecification.createdBefore(to));

        Page<Order> orders = orderRepository.findAll(spec, pageable);

        return orders.map(this::enrichWithUser);
    }

    @Override
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

    @Override
    @Transactional
    public OrderResponseDto updateOrder(Long id, UpdateOrderRequestDto dto) {
        Order existing = getEntityById(id);

        Order updatedOrder = orderMapper.toEntity(dto);

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
