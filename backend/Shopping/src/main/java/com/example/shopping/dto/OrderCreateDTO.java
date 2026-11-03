package com.example.shopping.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderCreateDTO {
    private Integer orderId;
    private BigDecimal totalAmount;
}
