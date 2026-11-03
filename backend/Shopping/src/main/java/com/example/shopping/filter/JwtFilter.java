package com.example.shopping.filter;

import com.example.shopping.log.LogUtil;
import com.example.shopping.utils.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 认证过滤器（Order=0，在 CryptoFilter 之前）
 * 从 Cookie 中提取 JWT，验证并解析 userId，存入 request attribute
 * 放行：/shopping/login, /shopping/register, /api/key/*
 */
@Component
@Order(0)
public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();

        // 放行 OPTIONS 预检请求（CORS）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        // 放行不需要认证的路径
        if (uri.equals("/shopping/login") ||
            uri.equals("/shopping/register") ||
            uri.startsWith("/api/key/") ||
            uri.equals("/") ||
            uri.startsWith("/dist/") ||
            uri.startsWith("/assets/") ||
            uri.equals("/favicon.ico") ||
            uri.equals("/index.html")) {
            chain.doFilter(req, res);
            return;
        }

        // 从 Cookie 中提取 JWT
        String token = extractTokenFromCookie(request);

        if (token == null || !jwtUtil.validateToken(token)) {
            LogUtil.warn("认证", null, "JWT验证失败", "未授权访问: " + uri + ", IP: " + request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
            return;
        }

        // 解析 userId 和 name，存入 request attribute
        io.jsonwebtoken.Claims claims = jwtUtil.parseToken(token);
        Integer userId = Integer.parseInt(claims.getSubject());
        String name = claims.get("name", String.class);
        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentUserName", name);

        chain.doFilter(req, res);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if ("jwt".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
