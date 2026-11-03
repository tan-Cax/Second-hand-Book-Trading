package com.example.shopping.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {

    private static final Logger bizLogger = LoggerFactory.getLogger("SHOPPING_LOG");

    public static void info(String module, Integer userId, String action, String detail) {
        bizLogger.info("[{}] [userId={}] [action={}] {}", module, userId != null ? userId : "-", action, detail);
    }

    public static void warn(String module, Integer userId, String action, String detail) {
        bizLogger.warn("[{}] [userId={}] [action={}] {}", module, userId != null ? userId : "-", action, detail);
    }

    public static void error(String module, Integer userId, String action, String detail, Throwable t) {
        bizLogger.error("[{}] [userId={}] [action={}] {}", module, userId != null ? userId : "-", action, detail, t);
    }

    public static void error(String module, Integer userId, String action, String detail) {
        bizLogger.error("[{}] [userId={}] [action={}] {}", module, userId != null ? userId : "-", action, detail);
    }
}
