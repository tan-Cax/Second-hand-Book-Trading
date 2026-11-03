package com.example.shopping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FrozenAmountDTO {
    private Integer id;
    private Integer orderId;
    private BigDecimal amount;
    private Integer status;
    private String statusText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
