package com.innowise.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequestDto {

        @NotNull
        private Long userId;

        @Valid
        @NotEmpty
        private List<OrderItemRequestDto> items;
    }

