package com.example.shopping.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Integer id;
    private Integer userId;
    private Integer walletId;
    private LocalDateTime orderTime;
    private BigDecimal totalAmount;
    private Integer status; // 0:未支付, 1:已支付待发货, 2:已发货待收货, 3:已完成, 4:已取消
}
