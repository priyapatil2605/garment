package com.dragonfruit.core.api;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final JdbcTemplate jdbc;
    public InventoryController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public List<Map<String, Object>> list(@RequestParam(required = false) Integer serverId) {
        if (serverId != null && (serverId < 1 || serverId > 3))
            throw new IllegalArgumentException("serverId must be 1, 2, or 3");
        String sql = "SELECT p.product_id AS productId, p.sku, p.name AS productName, p.price, i.server_id AS serverId, i.quantity, i.reserved_quantity AS reservedQuantity, (i.quantity-i.reserved_quantity) AS available, i.version, i.updated_at AS updatedAt FROM inventory i JOIN products p ON p.product_id=i.product_id " +
            (serverId == null ? "" : "WHERE i.server_id=? ") + "ORDER BY p.product_id, i.server_id";
        return serverId == null ? jdbc.queryForList(sql) : jdbc.queryForList(sql, serverId);
    }

    @PutMapping("/{productId}")
    public Map<String, Object> update(@PathVariable long productId,
            @RequestParam int serverId, @RequestBody StockUpdate req) {
        if (serverId < 1 || serverId > 3 || req.quantity() < 0)
            throw new IllegalArgumentException("serverId must be 1-3 and quantity must be non-negative");
        int n = jdbc.update("UPDATE inventory SET quantity=?, version=version+1, updated_at=CURRENT_TIMESTAMP WHERE product_id=? AND server_id=?",
            req.quantity(), productId, serverId);
        if (n == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory row not found");
        return jdbc.queryForMap("SELECT product_id AS productId, server_id AS serverId, quantity, reserved_quantity AS reservedQuantity, version FROM inventory WHERE product_id=? AND server_id=?", productId, serverId);
    }
    public record StockUpdate(int quantity) {}
}
