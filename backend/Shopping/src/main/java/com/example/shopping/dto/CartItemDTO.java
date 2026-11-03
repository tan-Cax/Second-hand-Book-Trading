package com.example.shopping.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Integer id;
    private Integer bookId;
    private String title;
    private BigDecimal price;
    private String locate;
    private String imageUrl;
    private Integer quantity;
}
