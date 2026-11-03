package com.example.shopping.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletInfoDTO {
    private Integer walletId;
    private BigDecimal balance;
    private Integer status;
}
