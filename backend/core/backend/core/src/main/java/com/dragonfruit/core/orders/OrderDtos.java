package com.dragonfruit.core.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {
    private OrderDtos() {}

    public record CreateOrderRequest(
        @NotBlank String customerName,
        @NotEmpty List<@Valid ItemRequest> items,
        String idempotencyKey
    ) {}

    public record ItemRequest(
        @NotNull Long productId,
        @Min(1) int quantity
    ) {}

    public record OrderItemResponse(
        Long productId, String productName, int quantity,
        BigDecimal unitPrice, BigDecimal lineTotal
    ) {}

    public record OrderResponse(
        Long orderId, String customerName, BigDecimal totalAmount,
        String status, Integer processedByServer, Instant createdAt,
        List<OrderItemResponse> items
    ) {}
}
