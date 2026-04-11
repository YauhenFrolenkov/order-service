package com.innowise.order.dto.request;

import com.innowise.order.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateOrderRequestDto {

    @NotNull
    private OrderStatus status;

    @Valid
    private List<OrderItemRequestDto> items;
}
