package com.example.shopping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminOrderDTO {
    private Integer id;
    private Integer userId;
    private String buyerName;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusText;
    private LocalDateTime orderTime;
}
