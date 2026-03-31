package com.innowise.order.mapper;

import com.innowise.order.dto.request.OrderItemRequestDto;
import com.innowise.order.dto.response.OrderItemResponseDto;
import com.innowise.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "item", source = "itemId")
    OrderItem toEntity(OrderItemRequestDto dto);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "price", source = "item.price")
    OrderItemResponseDto toDto(OrderItem item);

    List<OrderItemResponseDto> toDtoList(List<OrderItem> items);
}
