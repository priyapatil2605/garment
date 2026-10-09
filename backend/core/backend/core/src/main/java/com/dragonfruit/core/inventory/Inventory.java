package com.dragonfruit.core.inventory;

import com.dragonfruit.core.product.Product;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "inventory")
public class Inventory {
    @EmbeddedId
    private InventoryId id;

    @MapsId("productId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Column(nullable = false)
    private long version;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Inventory() {}

    public InventoryId getId() { return id; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public int getReservedQuantity() { return reservedQuantity; }
    public long getVersion() { return version; }
    public int getAvailableQuantity() { return quantity - reservedQuantity; }
}
