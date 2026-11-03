package com.example.shopping.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionLog {
    private Integer id;
    private Integer walletId;
    private BigDecimal amount;
    private Integer type; // 1:充值, 2:购书支出, 3:售书收入, 4:退款
    private Integer orderId;
    private String description;
    private LocalDateTime createdTime;
}
