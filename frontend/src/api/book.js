import { apiGet, apiDelete, apiMultipart, apiMultipartPut } from './request.js'

export function getBookList() {
  return apiGet('/api/book/list')
}

export function searchBooks(keyword) {
  return apiGet(`/api/book/search?keyword=${encodeURIComponent(keyword)}`)
}

export function advancedSearch(params) {
  const qs = new URLSearchParams()
  if (params.keyword) qs.set('keyword', params.keyword)
  if (params.locate) qs.set('locate', params.locate)
  if (params.minPrice != null) qs.set('minPrice', params.minPrice)
  if (params.maxPrice != null) qs.set('maxPrice', params.maxPrice)
  if (params.sortDirection) qs.set('sortDirection', params.sortDirection)
  return apiGet(`/api/book/search/advanced?${qs.toString()}`)
}

export function getRecommend(count = 10) {
  return apiGet(`/api/book/recommend?count=${count}`)
}

export function getBookDetail(id) {
  return apiGet(`/api/book/${id}`)
}

export function getMyBooks() {
  return apiGet('/api/book/my')
}

export function publishBook(formData) {
  return apiMultipart('/api/book/publish', formData)
}

export function updateBook(id, formData) {
  return apiMultipartPut(`/api/book/${id}`, formData)
}

export function delistBook(id) {
  return apiDelete(`/api/book/${id}`)
}
