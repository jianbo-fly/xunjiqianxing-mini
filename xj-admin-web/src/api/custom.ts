import request from '@/utils/request'

export interface CustomDemandVO {
  id: number
  userId: number
  phone: string
  destination: string
  startDate: string
  days: number
  adultCount: number
  childCount: number
  budget: string
  requirements: string
  status: number
  statusDesc: string
  followRecords: FollowRecord[]
  createdAt: string
  updatedAt: string
}

export interface FollowRecord {
  id: number
  content: string
  createdAt: string
  operatorName: string
}

export interface CustomDemandQuery {
  page: number
  pageSize: number
  phone?: string
  destination?: string
  status?: number
  startDateBegin?: string
  startDateEnd?: string
}

// 获取定制需求列表
export function getCustomDemandList(params: CustomDemandQuery) {
  return request.get<{ list: CustomDemandVO[]; total: number }>('/admin/custom/list', { params })
}

// 获取定制需求详情
export function getCustomDemandDetail(id: number) {
  return request.get<CustomDemandVO>(`/admin/custom/${id}`)
}

// 更新定制需求状态
export function updateCustomDemandStatus(id: number, status: number, remark?: string) {
  return request.post(`/admin/custom/${id}/status`, { status, remark })
}

// 添加跟进记录
export function addFollowRecord(id: number, content: string) {
  return request.post(`/admin/custom/${id}/follow`, { content })
}
