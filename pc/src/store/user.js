import { defineStore } from 'pinia'
import { login as loginApi, getMe } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({ token: localStorage.getItem('pc_token') || '', userInfo: null }),
  actions: {
    async login(mobile, password) {
      const res = await loginApi({ mobile, password })
      this.token = res.data.token
      localStorage.setItem('pc_token', this.token)
      const me = await getMe()
      this.userInfo = me.data
    },
    logout() { this.token = ''; this.userInfo = null; localStorage.removeItem('pc_token') }
  }
})
