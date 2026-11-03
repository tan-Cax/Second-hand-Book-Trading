package com.example.shopping.controller;

import com.example.shopping.dto.Result;
import com.example.shopping.service.RechargeService;
import com.example.shopping.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final RechargeService rechargeService;
    private final WalletService walletService;

    public WalletController(RechargeService rechargeService, WalletService walletService) {
        this.rechargeService = rechargeService;
        this.walletService = walletService;
    }

    @PostMapping("/create")
    public Result<?> createWallet(@RequestBody Map<String, Object> body,
                                  HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        String payPassword = (String) body.get("payPassword");
        if (payPassword == null || payPassword.trim().isEmpty()) {
            return Result.fail(400, "支付密码不能为空");
        }
        return walletService.createWallet(userId, payPassword);
    }

    @GetMapping("/info")
    public Result<?> getWallet(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return walletService.getWallet(userId);
    }

    @PostMapping("/recharge")
    public Result<?> recharge(@RequestBody Map<String, Object> body,
                              HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        Object amountObj = body.get("amount");
        if (amountObj == null) {
            return Result.fail(400, "充值金额不能为空");
        }
        BigDecimal amount = new BigDecimal(amountObj.toString());
        String payPassword = (String) body.get("payPassword");
        if (payPassword == null || payPassword.trim().isEmpty()) {
            return Result.fail(400, "支付密码不能为空");
        }
        return rechargeService.recharge(userId, amount, payPassword);
    }

    @PutMapping("/pay-password")
    public Result<?> updatePayPassword(@RequestBody Map<String, Object> body,
                                       HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        String oldPassword = (String) body.get("oldPassword");
        String newPassword = (String) body.get("newPassword");
        return walletService.updatePayPassword(userId, oldPassword, newPassword);
    }

    @PutMapping("/pay-password/set")
    public Result<?> setPayPassword(@RequestBody Map<String, Object> body,
                                    HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        String payPassword = (String) body.get("payPassword");
        if (payPassword == null || payPassword.trim().isEmpty()) {
            return Result.fail(400, "支付密码不能为空");
        }
        return rechargeService.setPayPassword(userId, payPassword);
    }

    @GetMapping("/frozen/total")
    public Result<?> getFrozenTotal(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return walletService.getFrozenTotal(userId);
    }

    @GetMapping("/frozen/payer")
    public Result<?> getFrozenAsPayer(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return walletService.getFrozenAsPayer(userId, page, size);
    }

    @GetMapping("/frozen/receiver")
    public Result<?> getFrozenAsReceiver(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return walletService.getFrozenAsReceiver(userId, page, size);
    }

    @GetMapping("/transaction-log")
    public Result<?> getTransactionLog(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return walletService.getTransactionLog(userId, page, size);
    }
}
