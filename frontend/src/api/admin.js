import { apiGet, apiPost, apiDelete } from './request.js'

export function getAdminUsers(keyword = '', page = 1, size = 20) {
  const qs = new URLSearchParams({ page, size })
  if (keyword) qs.set('keyword', keyword)
  return apiGet(`/api/admin/users?${qs.toString()}`)
}

export function banUser(userId) {
  return apiPost(`/api/admin/users/${userId}/ban`)
}

export function unbanUser(userId) {
  return apiPost(`/api/admin/users/${userId}/unban`)
}

export function lockPay(userId) {
  return apiPost(`/api/admin/users/${userId}/lock-pay`)
}

export function unlockPay(userId) {
  return apiPost(`/api/admin/users/${userId}/unlock-pay`)
}

export function getAdminOrders(status = '', page = 1, size = 20) {
  const qs = new URLSearchParams({ page, size })
  if (status != null && status !== '') qs.set('status', status)
  return apiGet(`/api/admin/orders?${qs.toString()}`)
}

export function getAdminOrderDetail(orderId) {
  return apiGet(`/api/admin/orders/${orderId}`)
}

export function getAdminTransactions(walletId = '', type = '', page = 1, size = 20) {
  const qs = new URLSearchParams({ page, size })
  if (walletId != null && walletId) qs.set('walletId', walletId)
  if (type != null && type !== '') qs.set('type', type)
  return apiGet(`/api/admin/transactions?${qs.toString()}`)
}

export function getAdminStatistics() {
  return apiGet('/api/admin/statistics')
}

export function getDeletedBooks() {
  return apiGet('/api/admin/books/deleted')
}

export function permanentDeleteBook(bookId) {
  return apiDelete(`/api/admin/books/${bookId}`)
}
