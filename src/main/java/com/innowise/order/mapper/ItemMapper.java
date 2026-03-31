package com.innowise.order.mapper;

import com.innowise.order.entity.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    default Item fromId(Long id) {
        if (id == null) return null;

        Item item = new Item();
        item.setId(id);
        return item;
    }
}
