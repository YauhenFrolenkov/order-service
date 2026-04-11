package com.innowise.order.dto.response;

import com.innowise.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
