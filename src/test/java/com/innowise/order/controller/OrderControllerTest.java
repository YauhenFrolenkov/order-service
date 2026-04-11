package com.innowise.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.order.client.UserServiceClient;
import com.innowise.order.dto.request.CreateOrderRequestDto;
import com.innowise.order.dto.request.OrderItemRequestDto;
import com.innowise.order.dto.request.UpdateOrderRequestDto;
import com.innowise.order.dto.response.OrderResponseDto;
import com.innowise.order.entity.OrderStatus;
import com.innowise.order.exception.ItemNotFoundException;
import com.innowise.order.exception.OrderNotFoundException;
import com.innowise.order.mapper.OrderMapper;
import com.innowise.order.security.JwtProvider;
import com.innowise.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private OrderMapper orderMapper;

    @MockitoBean
    private UserServiceClient userServiceClient;

    private OrderResponseDto responseDto;

    @BeforeEach
    void setUp() {
        responseDto = new OrderResponseDto();
        responseDto.setId(1L);
        responseDto.setUserId(10L);
        responseDto.setStatus(OrderStatus.CREATED);
        responseDto.setTotalPrice(BigDecimal.valueOf(100));
    }

    @Test
    void testGetOrderById_Success() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetOrderById_NotFound() throws Exception {
        when(orderService.getOrderById(1L)).thenThrow(new OrderNotFoundException("Order not found"));

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateOrder_Success() throws Exception {
        CreateOrderRequestDto request = new CreateOrderRequestDto();

        OrderItemRequestDto item = new OrderItemRequestDto();
        item.setItemId(1L);
        item.setQuantity(2);

        request.setItems(List.of(item));

        when(orderService.createOrder(any())).thenReturn(responseDto);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetOrders() throws Exception {
        Page<OrderResponseDto> page = new PageImpl<>(List.of(responseDto));
        when(orderService.getOrders(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void testGetOrdersByUserId() throws Exception {
        when(orderService.getOrdersByUserId(10L)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/orders/user/10/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void testUpdateOrder() throws Exception {
        UpdateOrderRequestDto request = new UpdateOrderRequestDto();
        request.setStatus(OrderStatus.PROCESSING);

        when(orderService.updateOrder(eq(1L), any())).thenReturn(responseDto);

        mockMvc.perform(put("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testDeleteOrder() throws Exception {
        doNothing().when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testCreateOrder_BadRequest() throws Exception {
        CreateOrderRequestDto request = new CreateOrderRequestDto();

        OrderItemRequestDto item = new OrderItemRequestDto();
        item.setItemId(1L);
        item.setQuantity(2);
        request.setItems(List.of(item));

        when(orderService.createOrder(any())).thenThrow(new ItemNotFoundException("Item not found"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateOrder_NotFound() throws Exception {
        UpdateOrderRequestDto request = new UpdateOrderRequestDto();
        request.setStatus(OrderStatus.PROCESSING);

        when(orderService.updateOrder(eq(1L), any())).thenThrow(new OrderNotFoundException("Not found"));

        mockMvc.perform(put("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateOrder_ValidationError() throws Exception {
        CreateOrderRequestDto request = new CreateOrderRequestDto();

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
