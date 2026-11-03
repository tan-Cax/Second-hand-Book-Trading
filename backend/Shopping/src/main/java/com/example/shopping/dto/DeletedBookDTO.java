package com.example.shopping.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeletedBookDTO {
    private Integer id;
    private String title;
    private Integer sellerId;
    private BigDecimal price;
    private Integer quantity;
    private String locate;
    private Boolean hasUnfinishedOrders;
    private Boolean canDelete;
}
