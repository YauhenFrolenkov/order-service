package com.innowise.order.dto.response;

import com.innowise.order.entity.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDto {

    private Long id;
    private Long userId;

    private String userEmail;
    private String userName;

    private OrderStatus status;
    private BigDecimal totalPrice;

    private List<OrderItemResponseDto> items;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
