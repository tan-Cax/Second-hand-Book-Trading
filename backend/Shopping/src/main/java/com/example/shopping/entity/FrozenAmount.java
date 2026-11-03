package com.example.shopping.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FrozenAmount {
    private Integer id;
    private Integer orderId;
    private Integer payerWalletId;
    private Integer receiverWalletId;
    private BigDecimal amount;
    private Integer status; // 1:冻结中, 2:已解冻入账, 3:已退款
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
