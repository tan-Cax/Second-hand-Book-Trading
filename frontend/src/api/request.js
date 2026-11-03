import { init, isReady, secureGet, securePost, securePut, secureDelete } from '../../utils/index.js'
import { getSessionId } from '../../utils/key.js'

class ApiError extends Error {
  constructor(code, msg) {
    super(msg)
    this.code = code
  }
}

async function ensureInit() {
  if (!isReady()) await init()
}

export async function apiGet(url) {
  await ensureInit()
  const res = await secureGet(url)
  if (res.ok && res.data?.code === 200) return res.data.data
  throw new ApiError(res.data?.code, res.data?.msg || '请求失败')
}

export async function apiPost(url, body) {
  await ensureInit()
  const res = await securePost(url, body)
  if (res.ok && res.data?.code === 200) return res.data.data
  throw new ApiError(res.data?.code, res.data?.msg || '请求失败')
}

export async function apiPut(url, body) {
  await ensureInit()
  const res = await securePut(url, body)
  if (res.ok && res.data?.code === 200) return res.data.data
  throw new ApiError(res.data?.code, res.data?.msg || '请求失败')
}

export async function apiDelete(url) {
  await ensureInit()
  const res = await secureDelete(url)
  if (res.ok && res.data?.code === 200) return res.data.data
  throw new ApiError(res.data?.code, res.data?.msg || '请求失败')
}

export async function apiMultipart(url, formData) {
  const headers = {}
  const sid = getSessionId()
  if (sid) headers['X-Session-Id'] = sid

  const res = await fetch(url, {
    method: 'POST',
    body: formData,
    credentials: 'include',
    headers,
  })
  const json = await res.json()
  if (res.ok && json.code === 200) return json.data
  throw new ApiError(json.code, json.msg || '上传失败')
}

export async function apiMultipartPut(url, formData) {
  const headers = {}
  const sid = getSessionId()
  if (sid) headers['X-Session-Id'] = sid

  const res = await fetch(url, {
    method: 'PUT',
    body: formData,
    credentials: 'include',
    headers,
  })
  const json = await res.json()
  if (res.ok && json.code === 200) return json.data
  throw new ApiError(json.code, json.msg || '上传失败')
}

export { ApiError }
