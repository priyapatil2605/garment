package com.dragonfruit.core.orders;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(name = "customer_name", nullable = false, length = 150)
    String customerName;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    BigDecimal totalAmount;

    @Column(nullable = false, length = 30)
    String status;

    @Column(name = "processed_by_server")
    Integer processedByServer;

    @Column(name = "idempotency_key", unique = true, length = 100)
    String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    protected CustomerOrder() {}

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public Integer getProcessedByServer() { return processedByServer; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt() { return createdAt; }
}
