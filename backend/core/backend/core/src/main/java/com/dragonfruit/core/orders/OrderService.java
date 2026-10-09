package com.dragonfruit.core.orders;

import com.dragonfruit.core.inventory.Inventory;
import com.dragonfruit.core.inventory.InventoryRepository;
import com.dragonfruit.core.product.Product;
import com.dragonfruit.core.product.ProductRepository;
import com.dragonfruit.core.orders.OrderDtos.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final ProductRepository products;
    private final InventoryRepository inventory;
    private final CustomerOrderRepository orders;
    private final OrderItemRepository items;

    @Value("${server.port:8080}")
    private int serverPort;

    public OrderService(ProductRepository products, InventoryRepository inventory,
                        CustomerOrderRepository orders, OrderItemRepository items) {
        this.products = products;
        this.inventory = inventory;
        this.orders = orders;
        this.items = items;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            var previous = orders.findByIdempotencyKey(request.idempotencyKey());
            if (previous.isPresent()) return toResponse(previous.get());
        }

        CustomerOrder order = new CustomerOrder();
        // Entity construction is handled through the package-private factory below.
        order = OrderFactory.create(request.customerName().trim(),
            request.idempotencyKey(), serverIdFromPort(serverPort));

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> createdItems = new ArrayList<>();

        for (ItemRequest item : request.items()) {
            Product product = products.findById(item.productId())
                .filter(Product::isActive)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Active product not found: " + item.productId()));

            Inventory stock = inventory.lockItem(product.getId(),
                serverIdFromPort(serverPort))
                .orElseThrow(() -> new IllegalStateException(
                    "No inventory row for product " + product.getId()
                    + " on server " + serverIdFromPort(serverPort)));

            if (stock.getAvailableQuantity() < item.quantity()) {
                throw new IllegalStateException("Insufficient stock for " + product.getName());
            }

            // Stock mutation is delegated to the entity helper.
            InventoryMutator.reserve(stock, item.quantity());
            OrderItem orderItem = new OrderItem(order, product, item.quantity());
            createdItems.add(orderItem);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.quantity())));
        }

        OrderFactory.setTotal(order, total);
        CustomerOrder saved = orders.save(order);
        items.saveAll(createdItems);
        return toResponse(saved, createdItems);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        CustomerOrder order = orders.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        return toResponse(order);
    }

    private OrderResponse toResponse(CustomerOrder order) {
        return toResponse(order, items.findByOrder_IdOrderByIdAsc(order.getId()));
    }

    private OrderResponse toResponse(CustomerOrder order, List<OrderItem> orderItems) {
        List<OrderItemResponse> responseItems = orderItems.stream()
            .map(i -> new OrderItemResponse(i.getProduct().getId(),
                i.getProductName(), i.getQuantity(), i.getUnitPrice(), i.getLineTotal()))
            .toList();
        return new OrderResponse(order.getId(), order.getCustomerName(),
            order.getTotalAmount(), order.getStatus(), order.getProcessedByServer(),
            order.getCreatedAt(), responseItems);
    }

    private int serverIdFromPort(int port) {
        return switch (port) {
            case 8081 -> 2;
            case 8082 -> 3;
            default -> 1;
        };
    }
}
