package com.example.shopping.service;

import com.example.shopping.dao.FrozenAmountDao;
import com.example.shopping.dao.TransactionLogDao;
import com.example.shopping.dao.WalletDao;
import com.example.shopping.dto.*;
import com.example.shopping.entity.FrozenAmount;
import com.example.shopping.entity.TransactionLog;
import com.example.shopping.entity.Wallet;
import com.example.shopping.log.LogUtil;
import com.example.shopping.utils.PepperUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class WalletService {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private final WalletDao walletDao;
    private final FrozenAmountDao frozenAmountDao;
    private final TransactionLogDao transactionLogDao;

    public WalletService(WalletDao walletDao, FrozenAmountDao frozenAmountDao,
                         TransactionLogDao transactionLogDao) {
        this.walletDao = walletDao;
        this.frozenAmountDao = frozenAmountDao;
        this.transactionLogDao = transactionLogDao;
    }

    // 开通钱包
    public Result<?> createWallet(Integer userId, String payPassword) {
        try {
            Wallet existing = walletDao.findByUserId(userId);
            if (existing != null) {
                LogUtil.warn("钱包", userId, "开通钱包", "用户已开通钱包");
                return Result.fail(400, "用户已开通钱包");
            }

            Wallet wallet = new Wallet();
            wallet.setUserId(userId);
            String pepperedHash = PepperUtil.getPepperedHash(payPassword);
            wallet.setPayPassword(encoder.encode(pepperedHash));
            walletDao.insert(wallet);

            LogUtil.info("钱包", userId, "开通钱包", "钱包开通成功, walletId=" + wallet.getId());
            WalletInfoDTO dto = new WalletInfoDTO();
            dto.setWalletId(wallet.getId());
            dto.setBalance(BigDecimal.ZERO);
            dto.setStatus(1);
            return Result.ok(dto);
        } catch (Exception e) {
            return Result.fail(500, "开通失败: " + e.getMessage());
        }
    }

    // 查看钱包信息
    public Result<?> getWallet(Integer userId) {
        try {
            Wallet wallet = walletDao.findByUserId(userId);
            if (wallet == null) {
                return Result.fail(404, "未开通钱包");
            }
            WalletInfoDTO dto = new WalletInfoDTO();
            dto.setWalletId(wallet.getId());
            dto.setBalance(wallet.getBalance());
            dto.setStatus(wallet.getStatus());
            return Result.ok(dto);
        } catch (Exception e) {
            return Result.fail(500, "查询失败");
        }
    }

    // 修改支付密码
    public Result<?> updatePayPassword(Integer userId, String oldPassword, String newPassword) {
        try {
            Wallet wallet = walletDao.findByUserId(userId);
            if (wallet == null) {
                return Result.fail(404, "未开通钱包");
            }

            if (wallet.getStatus() != null && wallet.getStatus() == 0) {
                LogUtil.warn("钱包", userId, "修改支付密码", "支付账户已被封禁");
                return Result.fail(403, "支付账户已被封禁，无法进行此操作");
            }

            if (wallet.getPayLocked() != null && wallet.getPayLocked() == 1) {
                return Result.fail(403, "支付账户已锁定，请联系管理员解锁");
            }

            String storedPwd = walletDao.getPayPassword(wallet.getId());
            String pepperedOld = PepperUtil.getPepperedHash(oldPassword);
            if (!encoder.matches(pepperedOld, storedPwd)) {
                LogUtil.warn("钱包", userId, "修改支付密码失败", "原密码错误");
                return Result.fail(400, "原支付密码错误");
            }

            String pepperedNew = PepperUtil.getPepperedHash(newPassword);
            walletDao.updatePayPassword(wallet.getId(), encoder.encode(pepperedNew));
            LogUtil.info("钱包", userId, "修改支付密码", "支付密码修改成功, walletId=" + wallet.getId());
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(500, "修改失败");
        }
    }

    // 查看冻结总额
    public Result<?> getFrozenTotal(Integer userId) {
        try {
            Wallet wallet = walletDao.findByUserId(userId);
            if (wallet == null) {
                return Result.fail(404, "未开通钱包");
            }
            BigDecimal frozenTotal = frozenAmountDao.getFrozenTotalByWallet(wallet.getId());
            return Result.ok(frozenTotal);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    // 查看冻结明细（作为买家）
    public Result<?> getFrozenAsPayer(Integer userId, int page, int size) {
        try {
            Wallet wallet = walletDao.findByUserId(userId);
            if (wallet == null) {
                return Result.fail(404, "未开通钱包");
            }
            List<FrozenAmount> list = frozenAmountDao.findByPayerWallet(wallet.getId(), page, size);
            int total = frozenAmountDao.countByPayerWallet(wallet.getId());
            return Result.ok(PageResult.of(convertFrozenList(list), total, page, size));
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    // 查看冻结明细（作为卖家）
    public Result<?> getFrozenAsReceiver(Integer userId, int page, int size) {
        try {
            Wallet wallet = walletDao.findByUserId(userId);
            if (wallet == null) {
                return Result.fail(404, "未开通钱包");
            }
            List<FrozenAmount> list = frozenAmountDao.findByReceiverWallet(wallet.getId(), page, size);
            int total = frozenAmountDao.countByReceiverWallet(wallet.getId());
            return Result.ok(PageResult.of(convertFrozenList(list), total, page, size));
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    // 查看交易记录
    public Result<?> getTransactionLog(Integer userId, int page, int size) {
        try {
            Wallet wallet = walletDao.findByUserId(userId);
            if (wallet == null) {
                return Result.fail(404, "未开通钱包");
            }
            List<TransactionLog> logs = transactionLogDao.findByWalletId(wallet.getId(), page, size);
            List<TransactionLogDTO> logList = new ArrayList<>();
            for (TransactionLog log : logs) {
                TransactionLogDTO dto = new TransactionLogDTO();
                dto.setId(log.getId());
                dto.setAmount(log.getAmount());
                dto.setType(log.getType());
                dto.setTypeText(getTransactionTypeText(log.getType()));
                dto.setOrderId(log.getOrderId());
                dto.setDescription(log.getDescription());
                dto.setCreatedTime(log.getCreatedTime());
                logList.add(dto);
            }
            int total = transactionLogDao.countByWalletId(wallet.getId());
            return Result.ok(PageResult.of(logList, total, page, size));
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    private List<FrozenAmountDTO> convertFrozenList(List<FrozenAmount> list) {
        List<FrozenAmountDTO> result = new ArrayList<>();
        for (FrozenAmount fa : list) {
            FrozenAmountDTO dto = new FrozenAmountDTO();
            dto.setId(fa.getId());
            dto.setOrderId(fa.getOrderId());
            dto.setAmount(fa.getAmount());
            dto.setStatus(fa.getStatus());
            dto.setStatusText(getFrozenStatusText(fa.getStatus()));
            dto.setCreatedAt(fa.getCreatedAt());
            dto.setUpdatedAt(fa.getUpdatedAt());
            result.add(dto);
        }
        return result;
    }

    private String getFrozenStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "冻结中";
            case 2 -> "已入账";
            case 3 -> "已退款";
            default -> "未知";
        };
    }

    private String getTransactionTypeText(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case 1 -> "充值";
            case 2 -> "购书支出";
            case 3 -> "售书收入";
            case 4 -> "退款";
            default -> "未知";
        };
    }
}
