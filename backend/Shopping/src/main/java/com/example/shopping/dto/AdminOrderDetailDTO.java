package com.example.shopping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class AdminOrderDetailDTO {
    private Integer id;
    private Integer userId;
    private String buyerName;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusText;
    private LocalDateTime orderTime;
    private List<AdminOrderItemDTO> items;
}
