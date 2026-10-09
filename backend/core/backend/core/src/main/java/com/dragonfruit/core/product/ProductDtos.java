package com.dragonfruit.core.product;

import java.math.BigDecimal;
import java.time.Instant;

public final class ProductDtos {
    private ProductDtos() {}

    public record ProductResponse(
        Long productId, String sku, String name, String category,
        String description, BigDecimal price, String imageUrl, boolean active
    ) {
        public static ProductResponse from(Product p) {
            return new ProductResponse(p.getId(), p.getSku(), p.getName(),
                p.getCategory(), p.getDescription(), p.getPrice(),
                p.getImageUrl(), p.isActive());
        }
    }

    public record InventoryResponse(
        Long productId, String sku, String name, BigDecimal price,
        Integer serverId, int quantity, int reservedQuantity,
        int availableQuantity, long version, Instant updatedAt
    ) {}
}
