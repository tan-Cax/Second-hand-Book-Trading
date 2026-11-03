package com.example.shopping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class AdminStatisticsDTO {
    private Integer totalUsers;
    private Integer totalOrders;
    private Map<String, Integer> ordersByStatus;
    private BigDecimal totalRecharge;
    private BigDecimal totalSales;
    private BigDecimal totalRefund;
    private Integer totalWallets;
}
