package com.innowise.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequestDto {

        @Valid
        @NotEmpty
        private List<OrderItemRequestDto> items;
    }

