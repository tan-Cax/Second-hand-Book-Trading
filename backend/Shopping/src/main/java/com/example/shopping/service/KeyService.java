package com.example.shopping.service;

import com.example.shopping.utils.CryptoUtil;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KeyService {

    private static final long AES_KEY_MAX_AGE_MS = 3600_000; // 1 hour

    private final KeyPair rsaKeyPair;
    private final ConcurrentHashMap<String, AesKeyEntry> aesKeyStore = new ConcurrentHashMap<>();

    private static class AesKeyEntry {
        String key;
        long createTime;

        AesKeyEntry(String key) {
            this.key = key;
            this.createTime = System.currentTimeMillis();
        }
    }

    public KeyService() throws Exception {
        this.rsaKeyPair = CryptoUtil.generateRsaKeyPair();
    }

    public String getPublicKeyBase64() {
        return CryptoUtil.getPublicKeyBase64(rsaKeyPair.getPublic());
    }

    public PrivateKey getPrivateKey() {
        return rsaKeyPair.getPrivate();
    }

    public String getAesKey(String sessionId) {
        cleanExpiredKeysIfNeeded();
        AesKeyEntry entry = aesKeyStore.get(sessionId);
        return entry != null ? entry.key : null;
    }

    public void registerAesKey(String sessionId, String encryptedAesKey) throws Exception {
        String aesKey = CryptoUtil.rsaDecrypt(encryptedAesKey, rsaKeyPair.getPrivate());
        aesKeyStore.putIfAbsent(sessionId, new AesKeyEntry(aesKey));
    }

    public boolean hasAesKey(String sessionId) {
        cleanExpiredKeysIfNeeded();
        return aesKeyStore.containsKey(sessionId);
    }

    public void removeAesKey(String sessionId) {
        aesKeyStore.remove(sessionId);
    }

    private void cleanExpiredKeysIfNeeded() {
        long now = System.currentTimeMillis();
        aesKeyStore.entrySet().removeIf(e -> now - e.getValue().createTime > AES_KEY_MAX_AGE_MS);
    }
}
