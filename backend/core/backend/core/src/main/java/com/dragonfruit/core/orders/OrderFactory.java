package com.dragonfruit.core.orders;

import java.math.BigDecimal;
import java.time.Instant;

final class OrderFactory {
    private OrderFactory() {}

    static CustomerOrder create(String customerName, String key, Integer serverId) {
        return CustomerOrderAccess.create(customerName, key, serverId);
    }

    static void setTotal(CustomerOrder order, BigDecimal total) {
        CustomerOrderAccess.setTotal(order, total);
    }
}
