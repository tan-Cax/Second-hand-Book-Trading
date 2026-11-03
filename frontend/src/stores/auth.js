import { defineStore } from 'pinia'
import { getUserInfo } from '@/api/user'
import { loginApi, registerApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    isLoggedIn: false,
    isAdmin: false,
  }),

  actions: {
    async fetchUserInfo() {
      try {
        this.user = await getUserInfo()
        this.isLoggedIn = true
        this.isAdmin = this.user?.role === 1
      } catch (e) {
        this.user = null
        this.isLoggedIn = false
        this.isAdmin = false
        throw e
      }
    },

    async login(data) {
      await loginApi(data)
      await this.fetchUserInfo()
    },

    async register(data) {
      await registerApi(data)
    },

    logout() {
      this.user = null
      this.isLoggedIn = false
      this.isAdmin = false
    },
  },
})
