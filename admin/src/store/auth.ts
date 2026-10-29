import { defineStore } from 'pinia'
import { loginApi, meApi, logoutApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    admin: null as any,
    permissions: [] as string[]
  }),
  actions: {
    async login(account: string, password: string) {
      const res: any = await loginApi({ account, password })
      this.token = res.data.token
      localStorage.setItem('admin_token', this.token)
    },
    async fetchMe() {
      const res: any = await meApi()
      this.admin = res.data
      this.permissions = res.data?.permissions || []
      return this.admin
    },
    /** 权限判定：'*' 表示超管放行 */
    can(perm?: string) {
      if (!perm) return true
      if (this.permissions.includes('*')) return true
      return this.permissions.includes(perm)
    },
    async logout() {
      try {
        await logoutApi()
      } finally {
        this.token = ''
        this.admin = null
        localStorage.removeItem('admin_token')
      }
    }
  }
})
