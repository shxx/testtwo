import { defineStore } from 'pinia'
import { fetchMe, logout as apiLogout, type CurrentUser } from '../api/auth'

/**
 * 会话状态（design.md 第 1 节 stores/auth.ts）。
 * loadError 区分「无会话（走 SSO）」与「服务不可用（页面展示错误与重试）」。
 */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as CurrentUser | null,
    loaded: false,
    loading: false,
    loadError: false,
  }),
  actions: {
    async loadMe() {
      this.loading = true
      this.loadError = false
      try {
        this.user = await fetchMe()
        this.loaded = true
      } catch {
        this.loadError = true
      } finally {
        this.loading = false
      }
    },
    async logout() {
      await apiLogout()
      this.user = null
      this.loaded = false
    },
  },
})
