<script setup lang="ts">
// 登录被拒提示页（design.md 第 6 节）：静态页面，无任何表单或替代登录入口（AC-3）
</script>

<template>
  <div class="page">
    <div class="card">
      <p class="icon">🚫</p>
      <p class="title">无法进入系统</p>
      <p class="message">飞书账号与台账账号不匹配，无法进入系统，请联系管理员。</p>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: #f5f6fa;
}
.card {
  background: #fff;
  padding: 48px 64px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.icon {
  font-size: 40px;
}
.title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 12px 0 8px;
}
.message {
  color: #888;
}
</style>
