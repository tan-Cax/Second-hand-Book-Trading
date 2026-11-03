import { apiGet, apiPost, apiPut } from './request.js'

export function createWallet(data) {
  return apiPost('/api/wallet/create', data)
}

export function getWalletInfo() {
  return apiGet('/api/wallet/info')
}

export function recharge(data) {
  return apiPost('/api/wallet/recharge', data)
}

export function changePayPassword(data) {
  return apiPut('/api/wallet/pay-password', data)
}

export function setPayPassword(data) {
  return apiPut('/api/wallet/pay-password/set', data)
}

export function getFrozenTotal() {
  return apiGet('/api/wallet/frozen/total')
}

export function getFrozenPayer(page = 1, size = 10) {
  return apiGet(`/api/wallet/frozen/payer?page=${page}&size=${size}`)
}

export function getFrozenReceiver(page = 1, size = 10) {
  return apiGet(`/api/wallet/frozen/receiver?page=${page}&size=${size}`)
}

export function getTransactionLog(page = 1, size = 20) {
  return apiGet(`/api/wallet/transaction-log?page=${page}&size=${size}`)
}
