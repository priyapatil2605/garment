package com.dragonfruit.core.orders;

import java.math.BigDecimal;
import java.time.Instant;

final class CustomerOrderAccess {
    private CustomerOrderAccess() {}

    static CustomerOrder create(String customerName, String key, Integer serverId) {
        CustomerOrder order = new CustomerOrder();
        order.customerName = customerName;
        order.idempotencyKey = (key == null || key.isBlank()) ? null : key.trim();
        order.processedByServer = serverId;
        order.status = "CONFIRMED";
        order.totalAmount = BigDecimal.ZERO;
        order.createdAt = Instant.now();
        return order;
    }

    static void setTotal(CustomerOrder order, BigDecimal total) {
        order.totalAmount = total;
    }
}
