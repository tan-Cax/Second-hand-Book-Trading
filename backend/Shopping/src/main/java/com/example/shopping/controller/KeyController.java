package com.example.shopping.controller;

import com.example.shopping.dto.KeyExchangeDTO;
import com.example.shopping.dto.Result;
import com.example.shopping.service.KeyService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/key")
public class KeyController {

    private final KeyService keyService;

    public KeyController(KeyService keyService) {
        this.keyService = keyService;
    }

    @GetMapping("/public")
    public Result<?> getPublicKey() {
        KeyExchangeDTO dto = new KeyExchangeDTO();
        dto.setPublicKey(keyService.getPublicKeyBase64());
        dto.setSessionId(UUID.randomUUID().toString());
        return Result.ok(dto);
    }

    @PostMapping("/register")
    public Result<?> registerAesKey(
            @RequestParam String sessionId,
            @RequestBody Map<String, String> body) {
        try {
            keyService.registerAesKey(sessionId, body.get("encryptedKey"));
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(400, "Failed to register AES key");
        }
    }
}
