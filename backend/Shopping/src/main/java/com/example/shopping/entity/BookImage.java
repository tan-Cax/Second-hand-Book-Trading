package com.example.shopping.entity;

import lombok.Data;

@Data
public class BookImage {
    private Integer id;
    private Integer bookId;
    private String imageUrl;
    private Integer sortOrder;
}
