package com.example.shopping.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminOrderItemDTO {
    private Integer id;
    private Integer bookId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal amount;
    private String title;
    private Integer sellerId;
    private String sellerName;
}
