package com.dragonfruit.core.orders;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
    Optional<CustomerOrder> findByIdempotencyKey(String idempotencyKey);
}
