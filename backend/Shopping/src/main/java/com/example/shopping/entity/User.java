package com.example.shopping.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Integer id;
    private String name;
    private String password;
    private Integer role; //0-普通用户，1-管理员，管理员不允许注册，只有初始账户
    private Integer isBanned; //0-正常，1-已封禁
    private LocalDateTime createdTime;
}
