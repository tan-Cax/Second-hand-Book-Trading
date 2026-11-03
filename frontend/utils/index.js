/**
 * 加密模块统一入口
 *
 * 使用示例：
 *   import { init, secureGet, securePost } from './utils/index.js';
 *
 *   // 页面加载时初始化密钥
 *   await init();
 *
 *   // 发起加密请求
 *   const res = await securePost('/api/book/list', { keyword: 'Java' });
 *   console.log(res.data);
 */
import { initKeys, isKeysReady } from './key.js';
import { secureFetch, securePut, secureDelete } from './request.js';

let initialized = false;

export async function init() {
    if (initialized) return;
    await initKeys();
    initialized = true;
}

export function isReady() {
    return isKeysReady();
}

export async function secureGet(url) {
    return secureFetch(url, { method: 'GET' });
}

export async function securePost(url, body) {
    return secureFetch(url, { method: 'POST', body });
}

export { securePut, secureDelete };
