package com.dragonfruit.core.orders;

import com.dragonfruit.core.inventory.Inventory;
import java.lang.reflect.Field;
import java.time.Instant;

final class InventoryMutator {
    private InventoryMutator() {}

    static void reserve(Inventory inventory, int quantity) {
        try {
            Field reserved = Inventory.class.getDeclaredField("reservedQuantity");
            Field version = Inventory.class.getDeclaredField("version");
            Field updated = Inventory.class.getDeclaredField("updatedAt");
            reserved.setAccessible(true);
            version.setAccessible(true);
            updated.setAccessible(true);
            reserved.setInt(inventory, inventory.getReservedQuantity() + quantity);
            version.setLong(inventory, inventory.getVersion() + 1);
            updated.set(inventory, Instant.now());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to update inventory", e);
        }
    }
}
