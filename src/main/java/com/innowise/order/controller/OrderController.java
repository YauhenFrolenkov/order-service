package com.innowise.order.controller;


import com.innowise.order.dto.request.CreateOrderRequestDto;
import com.innowise.order.dto.request.UpdateOrderRequestDto;
import com.innowise.order.dto.response.OrderResponseDto;
import com.innowise.order.entity.Order;
import com.innowise.order.entity.OrderStatus;
import com.innowise.order.mapper.OrderMapper;
import com.innowise.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @RequestBody @Valid CreateOrderRequestDto dto
    ) {
        Order order = orderMapper.toEntity(dto);

        OrderResponseDto response = orderService.createOrder(order);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id, @RequestParam String email) {
        return ResponseEntity.ok(orderService.getOrderById(id, email)); // временно
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> getOrders(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,

            @RequestParam(required = false) List<OrderStatus> statuses,
            @RequestParam(required = false) Long userId,

            @RequestParam String email, // временно
            Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getOrders(from, to, statuses, userId, email, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByUserId(@PathVariable Long userId, @RequestParam String email) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId, email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDto> updateOrder(
            @PathVariable Long id,
            @RequestBody @Valid UpdateOrderRequestDto dto
    ) {
        Order order = orderMapper.toEntity(dto);

        return ResponseEntity.ok(orderService.updateOrder(id, order));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
