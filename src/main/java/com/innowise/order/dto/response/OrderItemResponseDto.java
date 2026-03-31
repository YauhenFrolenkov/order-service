package com.innowise.order.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponseDto {

    private Long itemId;
    private String itemName;
    private BigDecimal price;
    private Integer quantity;

}
