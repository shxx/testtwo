/**
 * 认证相关 API 封装（design.md 第 1 节 src/api/auth.ts）。
 * 系统不提供任何账号/密码登录接口，唯一登录方式为飞书 SSO（AC-3）。
 */
export interface CurrentUser {
  name: string
  mobile: string
}

/** 校验会话：有效返回当前用户，无效（401）返回 null，网络异常抛错由调用方处理 */
export async function fetchMe(): Promise<CurrentUser | null> {
  const res = await fetch('/api/auth/me', { credentials: 'same-origin' })
  if (res.status === 401) {
    return null
  }
  if (!res.ok) {
    throw new Error(`/api/auth/me 请求失败：HTTP ${res.status}`)
  }
  return (await res.json()) as CurrentUser
}

/** 退出登录：仅清本系统会话（无单点登出） */
export async function logout(): Promise<void> {
  await fetch('/api/auth/logout', { method: 'POST', credentials: 'same-origin' })
}

/** 跳转后端登录入口，由后端 302 至飞书授权页（AC-1） */
export function redirectToSso(redirectPath: string): void {
  window.location.href = `/api/auth/login?redirect=${encodeURIComponent(redirectPath)}`
}
