<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const loggingOut = ref(false)

onMounted(async () => {
  // 路由守卫已保证会话有效；此处兜底刷新用户信息
  if (!auth.loaded && !auth.loadError) {
    await auth.loadMe()
  }
})

async function onLogout() {
  if (loggingOut.value) return
  loggingOut.value = true
  try {
    await auth.logout()
    // 回到根路径，守卫将触发重走 SSO（无单点登出，仅清本地会话）
    await router.replace('/')
  } finally {
    loggingOut.value = false
  }
}

async function retry() {
  await auth.loadMe()
  if (auth.user) {
    router.go(0)
  }
}
</script>

<template>
  <div class="page">
    <header class="topbar">
      <span class="title">门店网络台账</span>
      <span v-if="auth.user" class="user-area">
        <span class="user-name">{{ auth.user.name }}</span>
        <button class="btn" :disabled="loggingOut" @click="onLogout">退出</button>
      </span>
    </header>
    <main class="content">
      <p v-if="auth.loading" class="hint">加载中…</p>
      <div v-else-if="auth.loadError" class="error-box">
        <p>服务不可用，请稍后重试</p>
        <button class="btn" @click="retry">重试</button>
      </div>
      <div v-else-if="auth.user" class="placeholder">
        <p class="placeholder-title">台账功能建设中</p>
        <p class="hint">当前用户：{{ auth.user.name }}（{{ auth.user.mobile }}）</p>
      </div>
    </main>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: #2f54eb;
  color: #fff;
}
.title {
  font-size: 18px;
  font-weight: 600;
}
.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}
.btn {
  padding: 4px 14px;
  cursor: pointer;
  border: 1px solid #fff;
  background: transparent;
  color: #fff;
  border-radius: 4px;
}
.content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.placeholder-title {
  font-size: 20px;
  color: #333;
}
.hint {
  color: #999;
}
.error-box {
  text-align: center;
  color: #d4380d;
}
.error-box .btn {
  border-color: #d4380d;
  color: #d4380d;
}
</style>
