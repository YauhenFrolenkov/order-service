package com.innowise.order.service.impl;

import com.innowise.order.client.UserServiceClient;
import com.innowise.order.dto.request.CreateOrderRequestDto;
import com.innowise.order.dto.request.UpdateOrderRequestDto;
import com.innowise.order.dto.response.OrderResponseDto;
import com.innowise.order.dto.response.UserResponseDto;
import com.innowise.order.entity.Item;
import com.innowise.order.entity.Order;
import com.innowise.order.entity.OrderItem;
import com.innowise.order.entity.OrderStatus;
import com.innowise.order.exception.ItemNotFoundException;
import com.innowise.order.exception.OrderNotFoundException;
import com.innowise.order.exception.UserNotAuthenticatedException;
import com.innowise.order.mapper.OrderMapper;
import com.innowise.order.repository.ItemRepository;
import com.innowise.order.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private Item item;
    private OrderResponseDto orderResponseDto;
    private UserResponseDto userResponse;
    private CreateOrderRequestDto createDto;
    private UpdateOrderRequestDto updateDto;

    @BeforeEach
    void setUp() {
        Authentication auth = new UsernamePasswordAuthenticationToken(10L, null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        item = Item.builder()
                .id(1L)
                .name("Item 1")
                .price(new BigDecimal("100.00"))
                .build();

        order = Order.builder()
                .id(1L)
                .userId(10L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("200.00"))
                .build();

        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setQuantity(2);
        orderItem.setOrder(order);

        order.setItems(new ArrayList<>(List.of(orderItem)));

        orderResponseDto = OrderResponseDto.builder()
                .id(1L)
                .userId(10L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("200.00"))
                .build();

        userResponse = UserResponseDto.builder()
                .id(10L)
                .name("Test")
                .surname("User")
                .email("test@example.com")
                .build();

        createDto = new CreateOrderRequestDto();
        updateDto = new UpdateOrderRequestDto();
    }

    @Test
    void testCreateOrder_Success() {
        when(orderMapper.toEntity(any(CreateOrderRequestDto.class))).thenReturn(order);
        when(userServiceClient.getUserById(10L)).thenReturn(userResponse);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderResponseDto);

        OrderResponseDto result = orderService.createOrder(createDto);

        assertNotNull(result);
        assertEquals(10L, result.getUserId());
    }

    @Test
    void testUpdateOrder_Success() {
        when(orderMapper.toEntity(any(UpdateOrderRequestDto.class))).thenReturn(order);
        lenient().when(orderRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(order));
        lenient().when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        lenient().when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(orderMapper.toDto(any(Order.class))).thenReturn(orderResponseDto);
        lenient().when(userServiceClient.getUserById(anyLong())).thenReturn(userResponse);

        OrderResponseDto result = orderService.updateOrder(1L, updateDto);

        assertNotNull(result);
        assertEquals(OrderStatus.CREATED, result.getStatus());
    }

    @Test
    void testCreateOrder_ItemNotFound() {
        when(orderMapper.toEntity(any(CreateOrderRequestDto.class))).thenReturn(order);
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class,
                () -> orderService.createOrder(createDto));
    }

    @Test
    void testGetOrderById_Success() {
        when(orderRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);
        when(userServiceClient.getUserById(10L)).thenReturn(userResponse);

        OrderResponseDto result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(10L, result.getUserId());
    }

    @Test
    void testGetOrderById_NotFound() {
        when(orderRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrderById(1L));
    }

    @Test
    void testGetOrders() {
        Page<Order> orderPage = new PageImpl<>(List.of(order));
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(orderPage);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderResponseDto);
        when(userServiceClient.getUserById(10L)).thenReturn(userResponse);

        Page<OrderResponseDto> result = orderService.getOrders(null, null, null, null, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetOrdersByUserId() {
        when(orderRepository.findByUserIdAndDeletedFalse(10L)).thenReturn(List.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);
        when(userServiceClient.getUserById(10L)).thenReturn(userResponse);

        List<OrderResponseDto> result = orderService.getOrdersByUserId(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testDeleteOrder_Success() {
        when(orderRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrder(1L);

        assertTrue(order.getDeleted());
        verify(orderRepository).save(order);
    }

    @Test
    void testCreateOrder_UserNotAuthenticated() {
        SecurityContextHolder.clearContext();

        when(orderMapper.toEntity(any(CreateOrderRequestDto.class))).thenReturn(order);
        assertThrows(UserNotAuthenticatedException.class,
                () -> orderService.createOrder(createDto));
    }

    @Test
    void testCreateOrder_WithEmptyItems() {
        Order emptyOrder = Order.builder()
                .id(1L)
                .userId(10L)
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.ZERO)
                .items(null)
                .build();

        when(orderMapper.toEntity(any(CreateOrderRequestDto.class))).thenReturn(emptyOrder);
        when(userServiceClient.getUserById(10L)).thenReturn(userResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderResponseDto);

        OrderResponseDto result = orderService.createOrder(createDto);

        assertNotNull(result);

    }

    @Test
    void testUpdateOrder_NotFound() {
        when(orderRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.updateOrder(1L, updateDto));
    }

    @Test
    void testUpdateOrder_ItemNotFound() {
        when(orderRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(order));

        Order updatedOrder = new Order();

        OrderItem newItem = new OrderItem();
        newItem.setItem(Item.builder().id(1L).build());
        newItem.setQuantity(2);

        updatedOrder.setItems(List.of(newItem));

        when(orderMapper.toEntity(any(UpdateOrderRequestDto.class)))
                .thenReturn(updatedOrder);

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class,
                () -> orderService.updateOrder(1L, updateDto));
    }

    @Test
    void testUpdateOrder_ChangeStatus() {
        OrderResponseDto processingResponse = OrderResponseDto.builder()
                .id(1L)
                .userId(10L)
                .status(OrderStatus.PROCESSING)
                .totalPrice(new BigDecimal("200.00"))
                .build();

        UpdateOrderRequestDto statusOnlyDto = new UpdateOrderRequestDto();
        statusOnlyDto.setStatus(OrderStatus.PROCESSING);

        when(orderMapper.toEntity(any(UpdateOrderRequestDto.class))).thenReturn(order);
        when(orderRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(order));


        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toDto(any(Order.class))).thenReturn(processingResponse);
        when(userServiceClient.getUserById(anyLong())).thenReturn(userResponse);

        OrderResponseDto result = orderService.updateOrder(1L, statusOnlyDto);

        assertNotNull(result);
        assertEquals(OrderStatus.PROCESSING, result.getStatus());
    }

    @Test
    void testMarkAsPaid_Success() {
        order.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(order));

        orderService.markAsPaid(1L);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void testMarkAsFailed_Success() {
        order.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(order));

        orderService.markAsFailed(1L);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void testMarkAsPaid_AlreadyCompleted() {
        order.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(order));

        orderService.markAsPaid(1L);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void testMarkAsFailed_AlreadyCancelled() {
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(order));

        orderService.markAsFailed(1L);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}