package com.example.shopping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionLogDTO {
    private Integer id;
    private BigDecimal amount;
    private Integer type;
    private String typeText;
    private Integer orderId;
    private String description;
    private LocalDateTime createdTime;
}
