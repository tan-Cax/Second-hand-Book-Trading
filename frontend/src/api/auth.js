import { apiPost } from './request.js'

export function registerApi(data) {
  return apiPost('/shopping/register', data)
}

export function loginApi(data) {
  return apiPost('/shopping/login', data)
}

export function logoutApi() {
  return apiPost('/shopping/logout')
}
