package com.example.shopping.controller;

import com.example.shopping.dto.Result;
import com.example.shopping.log.LogUtil;
import com.example.shopping.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/info")
    public Result<?> getUserInfo(HttpServletRequest request) {
        Integer currentUserId = (Integer) request.getAttribute("currentUserId");
        return userService.getUserInfo(currentUserId);
    }

    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, Object> body,
                                    HttpServletRequest request) {
        Integer currentUserId = (Integer) request.getAttribute("currentUserId");
        String oldPassword = (String) body.get("oldPassword");
        String newPassword = (String) body.get("newPassword");
        Result<?> result = userService.changePassword(currentUserId, oldPassword, newPassword);
        if (result.getCode() == 200) {
            LogUtil.info("用户", currentUserId, "修改密码请求", "登录密码修改成功");
        }
        return result;
    }

    @GetMapping("/wallet")
    public Result<?> getWalletInfo(HttpServletRequest request) {
        Integer currentUserId = (Integer) request.getAttribute("currentUserId");
        return userService.getWalletInfo(currentUserId);
    }
}
