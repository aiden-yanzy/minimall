import { defineStore } from 'pinia'
import { getCartList } from '@/api/cart'

export const useCartStore = defineStore('cart', {
  state: () => ({ count: 0 }),
  actions: {
    async fetchCount() {
      try {
        const res = await getCartList()
        this.count = res.data.reduce((s, i) => s + i.num, 0)
      } catch (e) { this.count = 0 }
    }
  }
})
