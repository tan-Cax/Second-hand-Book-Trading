package com.example.shopping.filter;

import com.example.shopping.service.KeyService;
import com.example.shopping.utils.CryptoUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
@Order(1)
public class CryptoFilter implements Filter {

    private final KeyService keyService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CryptoFilter(KeyService keyService) {
        this.keyService = keyService;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();
        if (uri.startsWith("/api/key/") || (!"POST".equalsIgnoreCase(request.getMethod()) && !"PUT".equalsIgnoreCase(request.getMethod()))) {
            chain.doFilter(req, res);
            return;
        }

        String contentType = request.getContentType();
        if (contentType == null || !contentType.contains("application/json")) {
            chain.doFilter(req, res);
            return;
        }

        String body = readBody(request);
        if (body == null || body.isBlank()) {
            chain.doFilter(req, res);
            return;
        }

        try {
            JsonNode json = objectMapper.readTree(body);

            boolean hasEncryptedFields = false;
            for (String field : CryptoUtil.AES_ENCRYPTED_FIELDS) {
                if (json.has(field)) {
                    hasEncryptedFields = true;
                    break;
                }
            }

            if (!hasEncryptedFields) {
                DecryptedRequestWrapper wrappedRequest = new DecryptedRequestWrapper(request, body);
                chain.doFilter(wrappedRequest, res);
                return;
            }

            String sessionId = request.getHeader("X-Session-Id");
            String aesKey = keyService.getAesKey(sessionId);
            if (aesKey == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"AES key not registered\"}");
                return;
            }

            ObjectNode root = (ObjectNode) json;
            for (String field : CryptoUtil.AES_ENCRYPTED_FIELDS) {
                if (root.has(field) && !root.get(field).isNull()) {
                    String decrypted = CryptoUtil.aesDecrypt(root.get(field).asText(), aesKey);
                    root.put(field, decrypted);
                }
            }

            String modifiedBody = objectMapper.writeValueAsString(root);
            DecryptedRequestWrapper wrappedRequest = new DecryptedRequestWrapper(request, modifiedBody);
            chain.doFilter(wrappedRequest, res);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":400,\"message\":\"Decryption failed\"}");
        }
    }

    private String readBody(HttpServletRequest request) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining());
        } catch (Exception e) {
            return null;
        }
    }
}
