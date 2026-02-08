import request, { put } from '@/utils/request'

export interface LeaderVO {
  id: number
  userId: number
  name: string
  phone: string
  avatar: string
  idCard: string
  idCardFront: string
  idCardBack: string
  certificate: string
  intro: string
  experience: number
  status: number
  statusDesc: string
  leadCount: number
  totalCommission: number
  createdAt: string
  updatedAt: string
}

export interface LeaderQuery {
  page: number
  pageSize: number
  name?: string
  phone?: string
  status?: number
}

export interface LeadRecord {
  id: number
  orderId: number
  orderNo: string
  routeName: string
  startDate: string
  peopleCount: number
  commission: number
  status: number
  statusDesc: string
  createdAt: string
}

export interface CommissionRecord {
  id: number
  type: number
  typeDesc: string
  amount: number
  orderNo: string
  remark: string
  createdAt: string
}

// 获取领队列表
export function getLeaderList(params: LeaderQuery) {
  return request.get<{ list: LeaderVO[]; total: number }>('/admin/leader/list', { params })
}

// 获取领队详情
export function getLeaderDetail(id: number) {
  return request.get<LeaderVO>(`/admin/leader/${id}`)
}

// 审核领队
export function auditLeader(id: number, action: 'approve' | 'reject', reason?: string) {
  return request.post('/admin/leader/applies/audit', { id, status: action === 'approve' ? 1 : 2, auditRemark: reason })
}

// 更新领队状态（启用/禁用）
export function updateLeaderStatus(id: number, status: number) {
  return put(`/admin/leader/${id}/status?status=${status}`)
}

// 获取带队记录
export function getLeadRecords(id: number, params: { page: number; pageSize: number }) {
  return request.get<{ list: LeadRecord[]; total: number }>(`/admin/leader/${id}/leads`, { params })
}

// 获取佣金记录
export function getCommissionRecords(id: number, params: { page: number; pageSize: number }) {
  return request.get<{ list: CommissionRecord[]; total: number }>(`/admin/leader/${id}/commissions`, { params })
}

// 获取领队配置
export function getLeaderConfig() {
  return request.get<{ price: number; commissionRate: number }>('/admin/leader/config')
}

// 更新领队配置
export function updateLeaderConfig(data: { price: number; commissionRate: number }) {
  return request.post('/admin/leader/config', data)
}
