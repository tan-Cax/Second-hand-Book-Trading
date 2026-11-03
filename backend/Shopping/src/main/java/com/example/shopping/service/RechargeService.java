package com.example.shopping.service;

import com.example.shopping.dao.TransactionLogDao;
import com.example.shopping.dao.UserDao;
import com.example.shopping.dao.WalletDao;
import com.example.shopping.dto.RechargeDTO;
import com.example.shopping.dto.Result;
import com.example.shopping.entity.TransactionLog;
import com.example.shopping.entity.User;
import com.example.shopping.entity.Wallet;
import com.example.shopping.log.LogUtil;
import com.example.shopping.utils.DButil;
import com.example.shopping.utils.PepperUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;

@Service
public class RechargeService {

    public static final int MAX_PAY_FAIL_COUNT = 5;
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private final UserDao userDao;
    private final WalletDao walletDao;
    private final TransactionLogDao transactionLogDao;

    public RechargeService(UserDao userDao, WalletDao walletDao, TransactionLogDao transactionLogDao) {
        this.userDao = userDao;
        this.walletDao = walletDao;
        this.transactionLogDao = transactionLogDao;
    }

    public Result<?> verifyPayPassword(Integer userId, String rawPayPassword) {
        Connection conn = null;
        try {
            Wallet wallet = walletDao.findByUserId(userId);
            if (wallet == null) {
                return Result.fail(400, "钱包不存在");
            }

            if (wallet.getStatus() != null && wallet.getStatus() == 0) {
                LogUtil.warn("钱包", userId, "支付密码验证", "支付账户已被封禁");
                return Result.fail(403, "支付账户已被封禁，无法进行此操作");
            }

            if (wallet.getPayLocked() != null && wallet.getPayLocked() == 1) {
                return Result.fail(403, "支付账户已锁定，请联系管理员解锁");
            }

            if (wallet.getPayPassword() == null || wallet.getPayPassword().isEmpty()) {
                return Result.fail(400, "请先设置支付密码");
            }

            String pepperedHash = PepperUtil.getPepperedHash(rawPayPassword);
            if (!encoder.matches(pepperedHash, wallet.getPayPassword())) {
                conn = DButil.getConnection();
                conn.setAutoCommit(false);

                int currentCount = walletDao.incrementPayFailCountAndCheck(wallet.getId(), MAX_PAY_FAIL_COUNT, conn);

                conn.commit();

                if (currentCount >= MAX_PAY_FAIL_COUNT) {
                    LogUtil.error("钱包", userId, "支付密码锁定", "支付密码连续错误" + MAX_PAY_FAIL_COUNT + "次，账户已锁定, walletId=" + wallet.getId());
                    return Result.fail(403, "支付密码连续错误5次，账户已锁定，请联系管理员解锁");
                } else {
                    int remaining = MAX_PAY_FAIL_COUNT - currentCount;
                    LogUtil.warn("钱包", userId, "支付密码验证失败", "密码错误, 剩余" + remaining + "次机会, walletId=" + wallet.getId());
                    return Result.fail(400, "支付密码错误，剩余" + remaining + "次机会");
                }
            }

            conn = DButil.getConnection();
            conn.setAutoCommit(false);
            walletDao.resetPayFailCount(wallet.getId(), conn);
            conn.commit();
            return Result.ok();
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) { }
            }
            return Result.fail(500, "密码验证失败: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception ex) { }
            }
        }
    }

    public Result<?> recharge(Integer userId, BigDecimal amount, String rawPayPassword) {
        Connection conn = null;

        try {
            User user = userDao.findById(userId);
            if (user == null) {
                return Result.fail(400, "用户不存在");
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.fail(400, "充值金额必须大于0");
            }

            Result<?> verifyResult = verifyPayPassword(userId, rawPayPassword);
            if (verifyResult.getCode() != 200) {
                return verifyResult;
            }

            Wallet wallet = walletDao.findByUserId(userId);

            conn = DButil.getConnection();
            conn.setAutoCommit(false);

            BigDecimal currentBalance = walletDao.getBalanceForUpdate(wallet.getId(), conn);
            walletDao.addBalance(wallet.getId(), amount, conn);

            TransactionLog log = new TransactionLog();
            log.setWalletId(wallet.getId());
            log.setAmount(amount);
            log.setType(1);
            log.setDescription("用户充值 " + amount + " 元");
            transactionLogDao.insert(log, conn);

            conn.commit();

            LogUtil.info("钱包", userId, "充值", "充值成功, 金额=" + amount + ", walletId=" + wallet.getId());
            RechargeDTO dto = new RechargeDTO();
            dto.setAmount(amount);
            dto.setBalance(currentBalance.add(amount));
            return Result.ok(dto);

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) { }
            }
            return Result.fail(500, "充值失败: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception ex) { }
            }
        }
    }

    public Result<?> setPayPassword(Integer userId, String rawPayPassword) {
        try {
            User user = userDao.findById(userId);
            if (user == null) {
                return Result.fail(400, "用户不存在");
            }

            Wallet wallet = walletDao.findByUserId(userId);
            if (wallet == null) {
                return Result.fail(400, "钱包不存在");
            }

            if (wallet.getStatus() != null && wallet.getStatus() == 0) {
                LogUtil.warn("钱包", userId, "设置支付密码", "支付账户已被封禁");
                return Result.fail(403, "支付账户已被封禁，无法进行此操作");
            }

            if (wallet.getPayLocked() != null && wallet.getPayLocked() == 1) {
                return Result.fail(403, "支付账户已锁定，请联系管理员解锁");
            }

            if (wallet.getPayPassword() != null && !wallet.getPayPassword().isEmpty()) {
                return Result.fail(400, "支付密码已设置，请通过修改密码功能操作");
            }

            String pepperedHash = PepperUtil.getPepperedHash(rawPayPassword);
            String encodedPassword = encoder.encode(pepperedHash);

            walletDao.updatePayPassword(wallet.getId(), encodedPassword);

            LogUtil.info("钱包", userId, "设置支付密码", "支付密码设置成功, walletId=" + wallet.getId());
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(500, "设置失败: " + e.getMessage());
        }
    }
}
