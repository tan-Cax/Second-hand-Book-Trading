package com.example.shopping.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class PepperUtil {

    private static byte[] pepper;

    @Value("${pepper.file.path}")
    private String pepperFilePath;

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(pepperFilePath);
            pepper = Files.readAllBytes(path);
            if (pepper.length != 64) {
                throw new RuntimeException("Pepper 文件长度必须为64字节，当前: " + pepper.length);
            }
        } catch (IOException e) {
            throw new RuntimeException("无法读取 Pepper 文件: " + pepperFilePath, e);
        }
    }

    public static byte[] getPepper() {
        if (pepper == null) {
            throw new RuntimeException("Pepper 尚未初始化");
        }
        return pepper;
    }

    public static String getPepperString() {
        return new String(getPepper(), StandardCharsets.ISO_8859_1);
    }

    public static String getPepperedHash(String password) {
        try {
            byte[] pepper = getPepper();
            byte[] pwdBytes = password.getBytes(StandardCharsets.UTF_8);
            byte[] combined = new byte[pepper.length + pwdBytes.length];
            System.arraycopy(pepper, 0, combined, 0, pepper.length);
            System.arraycopy(pwdBytes, 0, combined, pepper.length, pwdBytes.length);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
