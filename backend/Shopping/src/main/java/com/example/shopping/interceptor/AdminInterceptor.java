package com.example.shopping.interceptor;

import com.example.shopping.dao.UserDao;
import com.example.shopping.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(1)
public class AdminInterceptor implements HandlerInterceptor {

    private final UserDao userDao;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdminInterceptor(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object userIdObj = request.getAttribute("currentUserId");
        if (userIdObj == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录\"}");
            return false;
        }

        Integer userId = (Integer) userIdObj;
        try {
            User user = userDao.findById(userId);
            if (user == null || user.getRole() == null || user.getRole() != 1) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                Map<String, Object> result = new HashMap<>();
                result.put("code", 403);
                result.put("msg", "需要管理员权限");
                response.getWriter().write(objectMapper.writeValueAsString(result));
                return false;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"msg\":\"权限验证失败\"}");
            return false;
        }
        return true;
    }
}
