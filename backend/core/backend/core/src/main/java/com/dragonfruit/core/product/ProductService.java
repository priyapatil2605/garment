package com.dragonfruit.core.product;

import com.dragonfruit.core.inventory.Inventory;
import com.dragonfruit.core.inventory.InventoryRepository;
import com.dragonfruit.core.product.ProductDtos.InventoryResponse;
import com.dragonfruit.core.product.ProductDtos.ProductResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository products;
    private final InventoryRepository inventory;

    public ProductService(ProductRepository products, InventoryRepository inventory) {
        this.products = products;
        this.inventory = inventory;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> products() {
        return products.findByActiveTrueOrderByIdAsc().stream()
            .map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse product(Long id) {
        Product p = products.findById(id)
            .filter(Product::isActive)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        return ProductResponse.from(p);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> inventory(Integer serverId) {
        int node = serverId == null ? 1 : serverId;
        if (node < 1 || node > 3) throw new IllegalArgumentException("serverId must be 1, 2 or 3");
        return inventory.findForServer(node).stream().map(this::toInventoryResponse).toList();
    }

    private InventoryResponse toInventoryResponse(Inventory i) {
        Product p = i.getProduct();
        return new InventoryResponse(p.getId(), p.getSku(), p.getName(), p.getPrice(),
            i.getId().getServerId(), i.getQuantity(), i.getReservedQuantity(),
            i.getAvailableQuantity(), i.getVersion(), null);
    }
}
