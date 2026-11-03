package com.example.shopping.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Book {
    private Integer id;
    private Integer sellerId;
    private String title;
    private String publisher;
    private String edition;
    private String author;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private String locate;
    private Integer isDeleted;    // 逻辑删除 0:正常, 1:已删除
    private LocalDateTime createdTime;
}
