package com.dragonfruit.core.inventory;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory, InventoryId> {
    @Query("select i from Inventory i join fetch i.product where i.id.serverId = :serverId order by i.product.id")
    List<Inventory> findForServer(@Param("serverId") Integer serverId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i join fetch i.product where i.id.productId = :productId and i.id.serverId = :serverId")
    Optional<Inventory> lockItem(@Param("productId") Long productId,
                                 @Param("serverId") Integer serverId);
}
