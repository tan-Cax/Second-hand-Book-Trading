/**
 * 前端请求拦截器
 * 自动对敏感字段 AES 加密/解密
 */
import { aesEncrypt, aesDecrypt } from './crypto.js';
import { getSessionId } from './key.js';

const SENSITIVE_FIELDS = ['payPassword', 'balance', 'amount', 'password', 'oldPassword', 'newPassword'];

export async function encryptRequestBody(body) {
    if (!body) return body;
    const data = typeof body === 'string' ? JSON.parse(body) : body;

    const hasSensitive = Object.keys(data).some(key => SENSITIVE_FIELDS.includes(key));
    if (!hasSensitive) return data;

    const result = {};
    for (const [key, value] of Object.entries(data)) {
        if (value == null) {
            result[key] = value;
        } else if (SENSITIVE_FIELDS.includes(key)) {
            const strValue = typeof value === 'string' ? value : String(value);
            result[key] = await aesEncrypt(strValue);
        } else {
            result[key] = value;
        }
    }
    return result;
}

export async function decryptResponseBody(response) {
    if (!response) return response;
    let data = response;
    if (typeof response === 'string') {
        try { data = JSON.parse(response); } catch { return response; }
    }
    if (!data || typeof data !== 'object' || Array.isArray(data)) return data;

    // 递归解密嵌套在 Result.data 内的敏感字段
    const inner = data.data;
    if (inner && typeof inner === 'object' && !Array.isArray(inner)) {
        const innerHasSensitive = Object.keys(inner).some(key => SENSITIVE_FIELDS.includes(key));
        if (innerHasSensitive) {
            for (const key of Object.keys(inner)) {
                if (SENSITIVE_FIELDS.includes(key) && typeof inner[key] === 'string') {
                    try { inner[key] = await aesDecrypt(inner[key]); }
                    catch { /* 解密失败保持原值 */ }
                }
            }
            return data;
        }
    }

    // 顶层（兼容非 Result 包装的响应）
    const hasSensitive = Object.keys(data).some(key => SENSITIVE_FIELDS.includes(key));
    if (!hasSensitive) return data;

    for (const key of Object.keys(data)) {
        if (SENSITIVE_FIELDS.includes(key) && typeof data[key] === 'string') {
            try { data[key] = await aesDecrypt(data[key]); }
            catch { /* 解密失败保持原值 */ }
        }
    }
    return data;
}

export async function secureFetch(url, options = {}) {
    const { body, ...rest } = options;

    let encryptedBody = null;
    if (body) {
        const encrypted = await encryptRequestBody(body);
        encryptedBody = JSON.stringify(encrypted);
    }

    const headers = {
        'Content-Type': 'application/json',
        ...(getSessionId() ? { 'X-Session-Id': getSessionId() } : {}),
        ...rest.headers
    };

    const response = await fetch(url, {
        ...rest,
        body: encryptedBody,
        headers
    });

    const text = await response.text();
    if (text) {
        const decrypted = await decryptResponseBody(text);
        return { ok: response.ok, status: response.status, data: decrypted };
    }

    return { ok: response.ok, status: response.status, data: null };
}

export async function securePut(url, body) {
    return secureFetch(url, { method: 'PUT', body });
}

export async function secureDelete(url, body) {
    return secureFetch(url, { method: 'DELETE', body });
}
