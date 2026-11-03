package com.example.shopping.service;

import com.example.shopping.dao.UserDao;
import com.example.shopping.dao.WalletDao;
import com.example.shopping.dto.Result;
import com.example.shopping.dto.UserInfoDTO;
import com.example.shopping.dto.WalletInfoDTO;
import com.example.shopping.entity.User;
import com.example.shopping.entity.Wallet;
import com.example.shopping.log.LogUtil;
import com.example.shopping.utils.Passwordutil;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserDao userDao;
    private final WalletDao walletDao;

    public UserService(UserDao userDao, WalletDao walletDao) {
        this.userDao = userDao;
        this.walletDao = walletDao;
    }

    public Result<?> getUserInfo(Integer userId) {
        try {
            User user = userDao.findById(userId);
            if (user == null) {
                return Result.fail(404, "用户不存在");
            }
            UserInfoDTO dto = new UserInfoDTO();
            dto.setUserId(user.getId());
            dto.setName(user.getName());
            dto.setRole(user.getRole());
            dto.setCreatedTime(user.getCreatedTime());

            Wallet wallet = walletDao.findByUserId(userId);
            if (wallet != null) {
                dto.setWalletId(wallet.getId());
            }
            return Result.ok(dto);
        } catch (Exception e) {
            return Result.fail(500, "查询失败");
        }
    }

    public Result<?> changePassword(Integer userId, String oldPassword, String newPassword) {
        try {
            User user = userDao.findById(userId);
            if (user == null) {
                return Result.fail(404, "用户不存在");
            }

            if (!Passwordutil.matches(oldPassword, user.getPassword())) {
                LogUtil.warn("用户", userId, "修改密码", "原密码错误");
                return Result.fail(400, "原密码错误");
            }

            userDao.updatePassword(userId, Passwordutil.encode(newPassword));
            LogUtil.info("用户", userId, "修改密码", "登录密码修改成功");
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(500, "修改失败");
        }
    }

    public Result<?> getWalletInfo(Integer userId) {
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
}
