package com.example.shopping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserInfoDTO {
    private Integer userId;
    private String name;
    private Integer role;
    private LocalDateTime createdTime;
    private Integer walletId;
}
