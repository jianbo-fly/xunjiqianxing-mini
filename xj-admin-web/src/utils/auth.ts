import type { UserInfo } from '@/types'

const TOKEN_KEY = 'token'
const USER_KEY = 'userInfo'

// Token
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

// UserInfo
export function getUserInfo(): UserInfo | null {
  const data = localStorage.getItem(USER_KEY)
  return data ? JSON.parse(data) : null
}

export function setUserInfo(userInfo: UserInfo): void {
  localStorage.setItem(USER_KEY, JSON.stringify(userInfo))
}

export function removeUserInfo(): void {
  localStorage.removeItem(USER_KEY)
}

// 清除所有登录信息
export function clearAuth(): void {
  removeToken()
  removeUserInfo()
}

// 是否已登录
export function isLoggedIn(): boolean {
  return !!getToken()
}
