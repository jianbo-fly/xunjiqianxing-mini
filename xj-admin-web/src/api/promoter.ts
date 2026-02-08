import request, { put } from '@/utils/request'

export interface PromoterVO {
  id: number
  userId: number
  name: string
  phone: string
  avatar: string
  status: number
  statusDesc: string
  totalOrders: number
  totalAmount: number
  totalPoints: number
  availablePoints: number
  bindCount: number
  createdAt: string
  updatedAt: string
}

export interface PromoterQuery {
  page: number
  pageSize: number
  name?: string
  phone?: string
  status?: number
}

export interface ScanRecord {
  id: number
  userId: number
  userName: string
  userPhone: string
  userAvatar: string
  bindTime: string
  orderCount: number
  totalAmount: number
}

export interface PointRecord {
  id: number
  type: number
  typeDesc: string
  points: number
  orderNo: string
  remark: string
  createdAt: string
}

// 获取推广员列表
export function getPromoterList(params: PromoterQuery) {
  return request.get<{ list: PromoterVO[]; total: number }>('/admin/promoter/list', { params })
}

// 获取推广员详情
export function getPromoterDetail(id: number) {
  return request.get<PromoterVO>(`/admin/promoter/${id}`)
}

// 审核推广员
export function auditPromoter(id: number, action: 'approve' | 'reject', reason?: string) {
  return request.post('/admin/promoter/applies/audit', { id, status: action === 'approve' ? 1 : 2, reason })
}

// 更新推广员状态（启用/禁用）
export function updatePromoterStatus(id: number, status: number) {
  return put(`/admin/promoter/${id}/status?status=${status}`)
}

// 获取扫码绑定记录
export function getScanRecords(id: number, params: { page: number; pageSize: number }) {
  return request.get<{ list: ScanRecord[]; total: number }>(`/admin/promoter/${id}/bindLogs`, { params })
}

// 获取积分记录
export function getPointRecords(id: number, params: { page: number; pageSize: number }) {
  return request.get<{ list: PointRecord[]; total: number }>(`/admin/promoter/${id}/commissions`, { params })
}
