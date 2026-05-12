package com.innowise.order.kafka.consumer;

import com.innowise.order.kafka.dto.CreatePaymentEvent;
import com.innowise.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderService orderService;

    @KafkaListener(topics = "create-payment")
    public void handlePaymentEvent(CreatePaymentEvent event) {

        log.info("Received event: {}", event);

        if ("SUCCESS".equals(event.getStatus())) {
            orderService.markAsPaid(event.getOrderId());
        } else {
            orderService.markAsFailed(event.getOrderId());
        }
    }
}