package com.example.shopping.controller;

import com.example.shopping.dto.LoginDTO;
import com.example.shopping.dto.Result;
import com.example.shopping.log.LogUtil;
import com.example.shopping.service.AuthService;
import com.example.shopping.utils.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/shopping")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String password = body.get("password");
        return authService.register(name, password);
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> body,
                           HttpServletResponse response) {
        String name = body.get("name");
        String password = body.get("password");

        Result<?> result = authService.login(name, password);

        if (result.getCode() == 200 && result.getData() instanceof LoginDTO loginDTO) {
            String token = jwtUtil.generateToken(loginDTO.getUserId(), loginDTO.getName());

            ResponseCookie cookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(86400)
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());
            LogUtil.info("认证", loginDTO.getUserId(), "JWT签发", "JWT令牌已生成并写入Cookie: " + loginDTO.getName());
        }

        return result;
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        return Result.ok(null);
    }
}
