import { apiGet, apiPost, apiPut, apiDelete } from './request.js'

export function getCartList() {
  return apiGet('/api/cart/list')
}

export function addToCart(data) {
  return apiPost('/api/cart/add', data)
}

export function updateCartItem(data) {
  return apiPut('/api/cart/update', data)
}

export function removeCartItem(bookId) {
  return apiDelete(`/api/cart/remove/${bookId}`)
}

export function clearCart() {
  return apiDelete('/api/cart/clear')
}
