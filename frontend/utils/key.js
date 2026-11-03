/**
 * 前端密钥管理模块
 * 负责与后端协商 AES 密钥
 */
import {
    generateAesKey,
    exportAesKeyBase64,
    rsaEncrypt,
    setAesKey,
    getAesKey
} from './crypto.js';

let sessionId = null;

/**
 * 初始化密钥交换
 * 1. 获取后端 RSA 公钥
 * 2. 生成本地 AES-256 密钥
 * 3. 用 RSA 公钥加密 AES 密钥并注册到后端
 */
export async function initKeys() {
    // 1. 获取 RSA 公钥
    const keyRes = await fetch('/api/key/public');
    const keyData = await keyRes.json();
    if (keyData.code !== 200) throw new Error('获取公钥失败: ' + keyData.msg);
    const publicKey = keyData.data.publicKey;
    sessionId = keyData.data.sessionId;

    // 2. 生成 AES 密钥
    const aesKeyObj = await generateAesKey();
    const aesKeyBase64 = await exportAesKeyBase64(aesKeyObj);

    // 3. 用 RSA 加密 AES 密钥
    const encryptedAesKey = await rsaEncrypt(aesKeyBase64, publicKey);

    // 4. 注册到后端
    const regRes = await fetch(`/api/key/register?sessionId=${sessionId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ encryptedKey: encryptedAesKey })
    });
    const regData = await regRes.json();
    if (regData.code !== 200) throw new Error('注册 AES 密钥失败: ' + regData.msg);

    // 5. 本地保存 AES 密钥
    setAesKey(aesKeyObj);
}

export function getSessionId() {
    return sessionId;
}

export function isKeysReady() {
    return getAesKey() !== null && sessionId !== null;
}
