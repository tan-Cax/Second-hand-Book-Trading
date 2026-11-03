import { apiGet, apiPost } from './request.js'

export function createOrder(bookIds) {
  return apiPost('/api/order/create', { bookIds })
}

export function payOrder(orderId, data) {
  return apiPost(`/api/order/${orderId}/pay`, data)
}

export function shipOrder(orderId) {
  return apiPost(`/api/order/${orderId}/ship`)
}

export function confirmOrder(orderId) {
  return apiPost(`/api/order/${orderId}/confirm`)
}

export function cancelOrder(orderId) {
  return apiPost(`/api/order/${orderId}/cancel`)
}

export function getMyOrders(page = 1, size = 10, status) {
  let url = `/api/order/my?page=${page}&size=${size}`
  if (status != null && status !== '') url += `&status=${status}`
  return apiGet(url)
}

export function getSellerOrders(page = 1, size = 10) {
  return apiGet(`/api/order/seller?page=${page}&size=${size}`)
}

export function getOrderDetail(orderId) {
  return apiGet(`/api/order/${orderId}`)
}
