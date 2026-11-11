import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { redirectToSso } from '../api/auth'
import LedgerHome from '../views/LedgerHome.vue'
import LoginDenied from '../views/LoginDenied.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/ledger' },
    {
      path: '/ledger',
      name: 'ledger-home',
      component: LedgerHome,
      meta: { requiresAuth: true },
    },
    // 登录被拒提示页：唯一不要求会话的路由（design.md 第 3/6 节）
    {
      path: '/login-denied',
      name: 'login-denied',
      component: LoginDenied,
    },
    { path: '/:pathMatch(.*)*', redirect: '/ledger' },
  ],
})

/**
 * 全局前置守卫（design.md 第 3 节 AC-1 前端层）：
 * 除 /login-denied 外全部要求会话；无效则跳转后端登录入口，
 * 由后端 302 至飞书授权页。系统不设任何登录表单页面。
 */
router.beforeEach(async (to) => {
  if (to.path === '/login-denied') {
    return true
  }
  const auth = useAuthStore()
  if (!auth.loaded && !auth.loadError && !auth.loading) {
    await auth.loadMe()
  }
  if (auth.loadError) {
    // 服务不可用：放行进入页面，由页面展示错误与重试，避免重定向死循环
    return true
  }
  if (auth.user) {
    return true
  }
  // 无会话 → 重定向飞书 SSO（携带原始路径，认证成功后回跳）
  redirectToSso(to.fullPath)
  return false
})

export default router
