import { get, post, put, del } from '@/utils/request'
import type {
  ID,
  PageResult,
  PageQuery,
  SupplierVO,
  SupplierCreateRequest,
  SupplierUpdateRequest,
} from '@/types'

// 供应商列表
export function getSupplierList(params: PageQuery): Promise<PageResult<SupplierVO>> {
  return get('/admin/supplier/list', params)
}

// 供应商详情
export function getSupplierDetail(id: ID): Promise<SupplierVO> {
  return get(`/admin/supplier/${id}`)
}

// 创建供应商
export function createSupplier(data: SupplierCreateRequest): Promise<void> {
  return post('/admin/supplier', data)
}

// 更新供应商
export function updateSupplier(data: SupplierUpdateRequest): Promise<void> {
  return put('/admin/supplier', data)
}

// 更新供应商状态
export function updateSupplierStatus(id: ID, status: number): Promise<void> {
  return put(`/admin/supplier/${id}/status?status=${status}`)
}

// 重置供应商密码
export function resetSupplierPassword(id: ID): Promise<void> {
  return post(`/admin/supplier/${id}/resetPassword`)
}

// 删除供应商
export function deleteSupplier(id: ID): Promise<void> {
  return del(`/admin/supplier/${id}`)
}
