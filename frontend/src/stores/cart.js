import { defineStore } from 'pinia'
import { getCartList, addToCart, removeCartItem, updateCartItem, clearCart } from '@/api/cart'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    count: 0,
  }),

  getters: {
    totalAmount: (state) => {
      return state.items.reduce((sum, item) => {
        return sum + item.price * item.quantity
      }, 0)
    },
  },

  actions: {
    async fetch() {
      try {
        const data = await getCartList()
        this.items = data.items || []
        this.count = data.count || 0
      } catch {
        this.items = []
        this.count = 0
      }
    },

    async add(bookId, quantity = 1) {
      await addToCart({ bookId, quantity })
      await this.fetch()
    },

    async remove(bookId) {
      await removeCartItem(bookId)
      await this.fetch()
    },

    async update(bookId, quantity) {
      await updateCartItem({ bookId, quantity })
      await this.fetch()
    },

    async clear() {
      await clearCart()
      await this.fetch()
    },
  },
})
