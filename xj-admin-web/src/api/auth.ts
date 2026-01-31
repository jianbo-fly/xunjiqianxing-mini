import { post, get } from '@/utils/request'
import type { LoginRequest, LoginResponse, UserInfo } from '@/types'

// 登录
export function login(data: LoginRequest): Promise<LoginResponse> {
  return post('/admin/auth/login', data)
}

// 退出登录
export function logout(): Promise<void> {
  return post('/admin/auth/logout')
}

// 获取当前用户信息
export function getUserInfo(): Promise<UserInfo> {
  return get('/admin/auth/userinfo')
}

// 修改密码
export function changePassword(data: { oldPassword: string; newPassword: string }): Promise<void> {
  return post('/admin/auth/changePassword', data)
}
