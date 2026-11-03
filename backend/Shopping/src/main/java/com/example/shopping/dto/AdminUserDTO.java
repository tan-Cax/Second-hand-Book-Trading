package com.example.shopping.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserDTO {
    private Integer id;
    private String name;
    private Integer role;
    private Integer isBanned;
    private Integer payLocked;
    private LocalDateTime createdTime;
}
