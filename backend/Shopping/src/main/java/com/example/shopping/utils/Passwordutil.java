package com.example.shopping.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//数据库存储数据加密
public class Passwordutil {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

}
