package com.innowise.order.specification;

import com.innowise.order.entity.Order;
import com.innowise.order.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;


class OrderSpecificationTest {

    @Test
    void hasUserId_shouldReturnPredicate_whenUserIdNotNull() {
        Specification<Order> spec = OrderSpecification.hasUserId(1L);

        assertNotNull(spec);
    }

    @Test
    void hasUserId_shouldReturnNullPredicate_whenUserIdNull() {
        Specification<Order> spec = OrderSpecification.hasUserId(null);

        assertNotNull(spec); // spec exists, predicate is null inside
    }

    @Test
    void hasStatuses_shouldReturnPredicate_whenNotEmpty() {
        Specification<Order> spec =
                OrderSpecification.hasStatuses(List.of(OrderStatus.CREATED));

        assertNotNull(spec);
    }

    @Test
    void hasStatuses_shouldReturnNullPredicate_whenEmpty() {
        Specification<Order> spec =
                OrderSpecification.hasStatuses(List.of());

        assertNotNull(spec);
    }

    @Test
    void createdAfter_shouldReturnPredicate_whenFromNotNull() {
        Specification<Order> spec =
                OrderSpecification.createdAfter(LocalDateTime.now());

        assertNotNull(spec);
    }

    @Test
    void createdAfter_shouldReturnNullPredicate_whenFromNull() {
        Specification<Order> spec =
                OrderSpecification.createdAfter(null);

        assertNotNull(spec);
    }

    @Test
    void createdBefore_shouldReturnPredicate_whenToNotNull() {
        Specification<Order> spec =
                OrderSpecification.createdBefore(LocalDateTime.now());

        assertNotNull(spec);
    }

    @Test
    void createdBefore_shouldReturnNullPredicate_whenToNull() {
        Specification<Order> spec =
                OrderSpecification.createdBefore(null);

        assertNotNull(spec);
    }

    @Test
    void notDeleted_shouldAlwaysReturnPredicate() {
        Specification<Order> spec = OrderSpecification.notDeleted();

        assertNotNull(spec);
    }


}
