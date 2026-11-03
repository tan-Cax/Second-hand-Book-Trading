package com.example.shopping.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItem {
    private Integer id;
    private Integer orderId; // Added relation to Order
    private Integer bookId;
    private Integer quantity;
    private BigDecimal price; // Snapshot price at the time of order
    private BigDecimal amount;
}
