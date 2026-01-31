import { get, post, put, del } from '@/utils/request'
import type {
  ID,
  CategoryVO,
  CategoryCreateRequest,
  CategoryUpdateRequest,
} from '@/types'

// 分类列表
export function getCategoryList(bizType?: string): Promise<CategoryVO[]> {
  return get('/admin/category/list', bizType ? { bizType } : undefined)
}

// 分类详情
export function getCategoryDetail(id: ID): Promise<CategoryVO> {
  return get(`/admin/category/${id}`)
}

// 创建分类
export function createCategory(data: CategoryCreateRequest): Promise<void> {
  return post('/admin/category', data)
}

// 更新分类
export function updateCategory(data: CategoryUpdateRequest): Promise<void> {
  return put('/admin/category', data)
}

// 更新分类状态
export function updateCategoryStatus(id: ID, status: number): Promise<void> {
  return put(`/admin/category/${id}/status?status=${status}`)
}

// 删除分类
export function deleteCategory(id: ID): Promise<void> {
  return del(`/admin/category/${id}`)
}

// 调整排序
export function updateCategorySort(data: { id: ID; sortOrder: number }): Promise<void> {
  return put('/admin/category/sort', data)
}
