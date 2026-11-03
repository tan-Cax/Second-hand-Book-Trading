package com.example.shopping.scheduler;

import com.example.shopping.log.LogUtil;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class IdleTimeoutMonitor {

    private final AtomicLong lastAccessTime = new AtomicLong(System.currentTimeMillis());

    @Value("${app.idle-timeout-seconds:300}")
    private int timeoutSeconds;

    private final ApplicationContext ctx;

    public IdleTimeoutMonitor(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    public void updateAccessTime() {
        lastAccessTime.set(System.currentTimeMillis());
    }

    @Scheduled(fixedRate = 10000)
    public void checkIdleTimeout() {
        long elapsed = (System.currentTimeMillis() - lastAccessTime.get()) / 1000;
        if (elapsed >= timeoutSeconds) {
            LogUtil.info("系统", null, "空闲超时", "超过 " + timeoutSeconds + " 秒无操作，自动关闭");
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                SpringApplication.exit(ctx, () -> 0);
                System.exit(0);
            }).start();
        }
    }

    @PreDestroy
    public void onDestroy() {
        LogUtil.info("系统", null, "空闲超时", "应用程序关闭");
    }
}
