package com.dragonfruit.core.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class InventoryId implements Serializable {
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "server_id")
    private Integer serverId;

    protected InventoryId() {}

    public InventoryId(Long productId, Integer serverId) {
        this.productId = productId;
        this.serverId = serverId;
    }

    public Long getProductId() { return productId; }
    public Integer getServerId() { return serverId; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof InventoryId that)) return false;
        return Objects.equals(productId, that.productId)
            && Objects.equals(serverId, that.serverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, serverId);
    }
}
