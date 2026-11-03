/**
 * 前端加密模块
 * 提供 AES-GCM、RSA-OAEP 加解密能力
 */

const crypto = window.crypto || window.msCrypto;
const subtle = crypto.subtle;

// ======================== Base64 工具 ========================

function base64Encode(arrayBuffer) {
    const bytes = new Uint8Array(arrayBuffer);
    let binary = '';
    for (let i = 0; i < bytes.length; i++) {
        binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary);
}

function base64Decode(base64Str) {
    const binary = atob(base64Str);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) {
        bytes[i] = binary.charCodeAt(i);
    }
    return bytes.buffer;
}

// ======================== AES-GCM ========================

let aesKey = null;

export async function generateAesKey() {
    return await subtle.generateKey(
        { name: 'AES-GCM', length: 256 },
        true,
        ['encrypt', 'decrypt']
    );
}

export async function exportAesKeyBase64(key) {
    const raw = await subtle.exportKey('raw', key);
    return base64Encode(raw);
}

export async function importAesKeyBase64(base64Key) {
    const raw = base64Decode(base64Key);
    return await subtle.importKey(
        'raw',
        raw,
        { name: 'AES-GCM', length: 256 },
        false,
        ['encrypt', 'decrypt']
    );
}

export async function aesEncrypt(plaintext) {
    if (!aesKey) throw new Error('AES key not initialized');
    const iv = crypto.getRandomValues(new Uint8Array(12));
    const encoded = new TextEncoder().encode(plaintext);
    const cipherBuffer = await subtle.encrypt(
        { name: 'AES-GCM', iv },
        aesKey,
        encoded
    );
    const combined = new Uint8Array(iv.length + cipherBuffer.byteLength);
    combined.set(iv, 0);
    combined.set(new Uint8Array(cipherBuffer), iv.length);
    return base64Encode(combined.buffer);
}

export async function aesDecrypt(ciphertext) {
    if (!aesKey) throw new Error('AES key not initialized');
    const combined = new Uint8Array(base64Decode(ciphertext));
    const iv = combined.slice(0, 12);
    const cipherBytes = combined.slice(12);
    const plainBuffer = await subtle.decrypt(
        { name: 'AES-GCM', iv },
        aesKey,
        cipherBytes
    );
    return new TextDecoder().decode(plainBuffer);
}

export function setAesKey(key) {
    aesKey = key;
}

export function getAesKey() {
    return aesKey;
}

// ======================== RSA-OAEP（密钥交换） ========================

export async function rsaEncrypt(plaintext, publicKeyBase64) {
    const keyData = base64Decode(publicKeyBase64);
    const publicKey = await subtle.importKey(
        'spki',
        keyData,
        { name: 'RSA-OAEP', hash: 'SHA-256' },
        false,
        ['encrypt']
    );
    const encoded = new TextEncoder().encode(plaintext);
    const encrypted = await subtle.encrypt(
        { name: 'RSA-OAEP' },
        publicKey,
        encoded
    );
    return base64Encode(encrypted);
}

export async function rsaDecrypt(ciphertext) {
    throw new Error('Frontend does not hold RSA private key');
}
