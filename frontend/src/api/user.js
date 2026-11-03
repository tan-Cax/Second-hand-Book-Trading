import { apiGet, apiPut } from './request.js'

export function getUserInfo() {
  return apiGet('/api/user/info')
}

export function getUserWallet() {
  return apiGet('/api/user/wallet')
}

export function changePassword(data) {
  return apiPut('/api/user/password', data)
}
