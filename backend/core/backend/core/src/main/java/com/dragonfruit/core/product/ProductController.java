package com.dragonfruit.core.product;

import com.dragonfruit.core.product.ProductDtos.InventoryResponse;
import com.dragonfruit.core.product.ProductDtos.ProductResponse;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/products")
    public List<ProductResponse> products() {
        return service.products();
    }

    @GetMapping("/products/{id}")
    public ProductResponse product(@PathVariable Long id) {
        return service.product(id);
    }

    @GetMapping("/inventory")
    public List<InventoryResponse> inventory(
        @RequestParam(required = false) Integer serverId
    ) {
        return service.inventory(serverId);
    }
}
