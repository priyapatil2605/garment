package com.dragonfruit.core.api;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final JdbcTemplate jdbc;
    public ProductController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public List<Map<String, Object>> list() {
        return jdbc.queryForList("SELECT product_id AS productId, sku, name, category, description, price, image_url AS imageUrl, active FROM products WHERE active = TRUE ORDER BY product_id");
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable long id) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT product_id AS productId, sku, name, category, description, price, image_url AS imageUrl, active FROM products WHERE product_id = ? AND active = TRUE", id);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        return rows.get(0);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody ProductRequest req) {
        if (blank(req.sku()) || blank(req.name()) || blank(req.category()) || req.price() == null || req.price().signum() < 0)
            throw new IllegalArgumentException("sku, name, category and a non-negative price are required");
        Long id = jdbc.queryForObject("INSERT INTO products(sku,name,category,description,price,image_url) VALUES(?,?,?,?,?,?)",
            Long.class, req.sku().trim(), req.name().trim(), req.category().trim(), req.description(), req.price(), req.imageUrl());
        List<Map<String, Object>> result = jdbc.queryForList("SELECT product_id AS productId, sku, name, category, description, price, image_url AS imageUrl, active FROM products WHERE product_id=?", id);
        return result.get(0);
    }

    private boolean blank(String s) { return s == null || s.isBlank(); }
    public record ProductRequest(String sku, String name, String category, String description,
                                 java.math.BigDecimal price, String imageUrl) {}
}
