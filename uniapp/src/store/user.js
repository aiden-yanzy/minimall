import { defineStore } from 'pinia'
import { login as loginApi, getMe as getMeApi } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: uni.getStorageSync('token') || '',
    userInfo: uni.getStorageSync('userInfo') || null
  }),
  actions: {
    async login(account, password) {
      const res = await loginApi({ mobile: account, password })
      this.setToken(res.data.token)
      await this.fetchMe()
    },
    async fetchMe() {
      try {
        const res = await getMeApi()
        this.setUserInfo(res.data)
      } catch (e) {
        /* 未登录忽略 */
      }
    },
    setToken(token) {
      this.token = token
      uni.setStorageSync('token', token)
    },
    setUserInfo(info) {
      this.userInfo = info
      uni.setStorageSync('userInfo', info)
    },
    logout() {
      this.token = ''
      this.userInfo = null
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
    }
  }
})
