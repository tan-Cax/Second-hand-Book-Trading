package com.example.shopping.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Scanner;

public class PepperGenerator {

    private static final int PEPPER_LENGTH = 64;

    public static void main(String[] args) {
        String defaultPath = "key/pepper.key";
        String filePath = defaultPath;

        System.out.println("=== Pepper 密钥生成器 ===");
        System.out.println("默认路径: " + defaultPath);

        Scanner scanner = new Scanner(System.in);
        System.out.print("是否使用默认路径？(y/n): ");
        String choice = scanner.nextLine().trim();

        if (!"y".equalsIgnoreCase(choice)) {
            System.out.print("请输入 Pepper 文件保存路径: ");
            filePath = scanner.nextLine().trim();
        }

        try {
            byte[] pepper = new byte[PEPPER_LENGTH];
            new SecureRandom().nextBytes(pepper);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(pepper);
            }

            System.out.println("Pepper 密钥已生成: " + filePath);
            System.out.println("密钥长度: " + PEPPER_LENGTH + " 字节");
            System.out.println("请妥善保管此文件，不要泄露或丢失！");
        } catch (IOException e) {
            System.err.println("生成 Pepper 失败: " + e.getMessage());
        }
    }
}
