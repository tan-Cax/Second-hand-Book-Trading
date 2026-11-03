package com.example.shopping.service;

import com.example.shopping.dao.UserDao;
import com.example.shopping.dto.LoginDTO;
import com.example.shopping.dto.RegisterDTO;
import com.example.shopping.dto.Result;
import com.example.shopping.entity.User;
import com.example.shopping.log.LogUtil;
import com.example.shopping.utils.Passwordutil;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Result<?> register(String name, String password) {
        try {
            User existing = userDao.findByName(name);
            if (existing != null) {
                LogUtil.warn("认证", null, "用户注册", "用户名已存在: " + name);
                return Result.fail(400, "用户名已存在");
            }

            User user = new User();
            user.setName(name);
            user.setPassword(Passwordutil.encode(password));
            user.setRole(0);
            userDao.insert(user);

            LogUtil.info("认证", user.getId(), "用户注册", "用户注册成功: " + name);
            RegisterDTO dto = new RegisterDTO();
            dto.setUserId(user.getId());
            return Result.ok(dto);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return Result.fail(400, "用户名已存在");
            }
            return Result.fail(500, "注册失败: " + e.getMessage());
        }
    }

    public Result<?> login(String name, String password) {
        try {
            User user = userDao.findByName(name);
            if (user == null) {
                LogUtil.warn("认证", null, "用户登录", "登录失败，用户不存在: " + name);
                return Result.fail(400, "用户名或密码错误");
            }

            if (!Passwordutil.matches(password, user.getPassword())) {
                LogUtil.warn("认证", user.getId(), "用户登录", "登录失败，密码错误: " + name);
                return Result.fail(400, "用户名或密码错误");
            }

            if (user.getIsBanned() != null && user.getIsBanned() == 1) {
                LogUtil.warn("认证", user.getId(), "用户登录", "登录失败，账号已被封禁: " + name);
                return Result.fail(403, "账号已被封禁，请联系管理员");
            }

            LogUtil.info("认证", user.getId(), "用户登录", "登录成功: " + name);
            LoginDTO dto = new LoginDTO();
            dto.setUserId(user.getId());
            dto.setName(user.getName());
            return Result.ok(dto);
        } catch (Exception e) {
            return Result.fail(500, "登录失败: " + e.getMessage());
        }
    }
}
