package com.innowise.order.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequestDto {

    @NotNull
    private Long itemId;

    @NotNull
    @Min(1)
    @Max(1000)
    private Integer quantity;
}
