package com.example.shopping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailDTO {
    private Integer id;
    private Integer userId;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusText;
    private LocalDateTime orderTime;
    private List<OrderItemDetailDTO> items;
}
