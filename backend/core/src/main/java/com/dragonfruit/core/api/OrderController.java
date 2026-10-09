package com.dragonfruit.core.api;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final JdbcTemplate jdbc;
    @Value("${app.server-id:1}") private int serverId;
    public OrderController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public List<Map<String, Object>> list() {
        return jdbc.queryForList("SELECT order_id AS orderId, customer_name AS customerName, total_amount AS totalAmount, status, processed_by_server AS processedByServer, idempotency_key AS idempotencyKey, created_at AS createdAt FROM orders ORDER BY created_at DESC");
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable long id) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT order_id AS orderId, customer_name AS customerName, total_amount AS totalAmount, status, processed_by_server AS processedByServer, idempotency_key AS idempotencyKey, created_at AS createdAt FROM orders WHERE order_id=?", id);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        Map<String, Object> order = new LinkedHashMap<>(rows.get(0));
        order.put("items", jdbc.queryForList("SELECT product_id AS productId, product_name AS productName, quantity, unit_price AS unitPrice, line_total AS lineTotal FROM order_items WHERE order_id=? ORDER BY order_item_id", id));
        return order;
    }

    @PostMapping
    @Transactional
    public Map<String, Object> create(@RequestBody CreateOrderRequest req) {
        if (req.customerName() == null || req.customerName().isBlank() || req.items() == null || req.items().isEmpty())
            throw new IllegalArgumentException("customerName and at least one item are required");
        if (req.idempotencyKey() != null && !req.idempotencyKey().isBlank()) {
            List<Long> existing = jdbc.query("SELECT order_id FROM orders WHERE idempotency_key=?",
                (rs, n) -> rs.getLong(1), req.idempotencyKey().trim());
            if (!existing.isEmpty()) return get(existing.get(0));
        }
        BigDecimal total = BigDecimal.ZERO;
        List<ResolvedItem> resolved = new ArrayList<>();
        for (ItemRequest item : req.items()) {
            if (item.productId() == null || item.quantity() < 1)
                throw new IllegalArgumentException("Each item needs productId and quantity >= 1");
            List<ResolvedItem> rows = jdbc.query("SELECT product_id, name, price FROM products WHERE product_id=? AND active=TRUE",
                (rs, n) -> new ResolvedItem(rs.getLong("product_id"), rs.getString("name"), rs.getBigDecimal("price"), item.quantity()), item.productId());
            if (rows.isEmpty()) throw new IllegalArgumentException("Active product not found: " + item.productId());
            ResolvedItem r = rows.get(0);
            int changed = jdbc.update("UPDATE inventory SET quantity=quantity-?, version=version+1, updated_at=CURRENT_TIMESTAMP WHERE product_id=? AND server_id=? AND quantity-reserved_quantity>=?",
                item.quantity(), item.productId(), serverId, item.quantity());
            if (changed != 1) throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock on server " + serverId + " for " + r.name());
            total = total.add(r.price().multiply(BigDecimal.valueOf(item.quantity())));
            resolved.add(r);
        }
        jdbc.update("INSERT INTO orders(customer_name,total_amount,status,processed_by_server,idempotency_key) VALUES(?,?,'CONFIRMED',?,?)",
            req.customerName().trim(), total, serverId,
            req.idempotencyKey() == null || req.idempotencyKey().isBlank() ? null : req.idempotencyKey().trim());
        Long orderId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        for (ResolvedItem r : resolved) {
            BigDecimal line = r.price().multiply(BigDecimal.valueOf(r.quantity()));
            jdbc.update("INSERT INTO order_items(order_id,product_id,product_name,quantity,unit_price,line_total) VALUES(?,?,?,?,?,?)",
                orderId, r.productId(), r.name(), r.quantity(), r.price(), line);
        }
        return get(orderId);
    }

    public record CreateOrderRequest(String customerName, List<ItemRequest> items, String idempotencyKey) {}
    public record ItemRequest(Long productId, int quantity) {}
    private record ResolvedItem(Long productId, String name, BigDecimal price, int quantity) {}
}
