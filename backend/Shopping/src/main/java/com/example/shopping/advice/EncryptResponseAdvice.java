package com.example.shopping.advice;

import com.example.shopping.dto.Result;
import com.example.shopping.service.KeyService;
import com.example.shopping.utils.CryptoUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;

@RestControllerAdvice
public class EncryptResponseAdvice implements ResponseBodyAdvice<Object> {

    private final KeyService keyService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public EncryptResponseAdvice(KeyService keyService) {
        this.keyService = keyService;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null) return body;
        if (body instanceof String) return body;

        String sessionId = null;
        if (request instanceof ServletServerHttpRequest servletRequest) {
            sessionId = servletRequest.getServletRequest().getHeader("X-Session-Id");
        }

        if (sessionId == null || !keyService.hasAesKey(sessionId)) {
            return body;
        }

        String aesKey = keyService.getAesKey(sessionId);

        if (body instanceof Map) {
            encryptFields((Map<String, Object>) body, aesKey);
        } else if (body instanceof Result) {
            Object data = ((Result<?>) body).getData();
            if (data instanceof Map) {
                encryptFields((Map<String, Object>) data, aesKey);
            } else if (data != null && !(data instanceof Iterable) && !data.getClass().isArray()) {
                Map<String, Object> dataMap = objectMapper.convertValue(data, Map.class);
                encryptFields(dataMap, aesKey);
                try {
                    java.lang.reflect.Field dataField = Result.class.getDeclaredField("data");
                    dataField.setAccessible(true);
                    dataField.set(body, dataMap);
                } catch (Exception e) {
                    com.example.shopping.log.LogUtil.error("加密", null, "响应加密失败", "字段: " + e.getMessage());
                }
            }
        }

        return body;
    }

    private void encryptFields(Map<String, Object> map, String aesKey) {
        for (String field : CryptoUtil.AES_ENCRYPTED_FIELDS) {
            if (map.containsKey(field) && map.get(field) != null) {
                try {
                    map.put(field, CryptoUtil.aesEncrypt(String.valueOf(map.get(field)), aesKey));
                } catch (Exception e) {
                    com.example.shopping.log.LogUtil.error("加密", null, "响应加密失败", "字段: " + field + ", 错误: " + e.getMessage());
                }
            }
        }
    }
}
