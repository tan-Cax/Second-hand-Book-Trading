package com.example.shopping.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RechargeDTO {
    private BigDecimal amount;
    private BigDecimal balance;
}
