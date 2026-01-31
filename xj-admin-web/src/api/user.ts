import { get, put, post } from '@/utils/request'
import type {
  ID,
  PageResult,
  UserQueryRequest,
  UserListVO,
} from '@/types'

// 用户列表
export function getUserList(params: UserQueryRequest): Promise<PageResult<UserListVO>> {
  return get('/admin/user/list', params)
}

// 用户详情
export function getUserDetail(id: ID): Promise<UserListVO> {
  return get(`/admin/user/${id}`)
}

// 更新用户状态
export function updateUserStatus(id: ID, status: number): Promise<void> {
  return put(`/admin/user/${id}/status?status=${status}`)
}

// 调整用户积分
export function adjustUserPoints(data: { userId: ID; points: number; reason: string }): Promise<void> {
  return post('/admin/user/points/adjust', data)
}
