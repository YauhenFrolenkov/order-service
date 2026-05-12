package com.innowise.order.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentEvent {

    private String paymentId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String status;
}
