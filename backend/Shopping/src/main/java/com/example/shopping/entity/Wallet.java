package com.example.shopping.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Wallet {
    private Integer id;
    private Integer userId;
    private String payPassword;
    private BigDecimal balance;
    private Integer status; // 1:正常, 0:冻结
    private Integer payFailCount;
    private Integer payLocked;
    private LocalDateTime createdTime;
}
