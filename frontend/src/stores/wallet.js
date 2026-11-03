import { defineStore } from 'pinia'
import { getWalletInfo } from '@/api/wallet'

export const useWalletStore = defineStore('wallet', {
  state: () => ({
    walletId: null,
    balance: 0,
    status: null,
    loaded: false,
  }),

  actions: {
    async fetch() {
      try {
        const data = await getWalletInfo()
        this.walletId = data.walletId
        this.balance = data.balance
        this.status = data.status
        this.loaded = true
      } catch {
        this.walletId = null
        this.balance = 0
        this.status = null
        this.loaded = false
      }
    },
  },
})
