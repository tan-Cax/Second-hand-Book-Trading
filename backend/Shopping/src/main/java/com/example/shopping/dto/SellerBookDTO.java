package com.example.shopping.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SellerBookDTO {
    private Integer id;
    private String title;
    private BigDecimal price;
    private String locate;
    private String imageUrl;
    private Integer quantity;
}
