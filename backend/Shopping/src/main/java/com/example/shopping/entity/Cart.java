package com.example.shopping.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Cart {
    private Integer id;
    private Integer userId;
    private Integer bookId;
    private Integer quantity; // Added quantity to buy
    private LocalDateTime addedTime; // Changed from orderTime for accuracy
}
