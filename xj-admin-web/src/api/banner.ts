import { get, post, put, del } from '@/utils/request'
import type {
  ID,
  PageResult,
  PageQuery,
  BannerVO,
  BannerCreateRequest,
  BannerUpdateRequest,
} from '@/types'

// Banner列表（分页）
export function getBannerList(params: PageQuery): Promise<PageResult<BannerVO>> {
  return get('/admin/banner/list', params)
}

// Banner列表（全部）
export function getBannerListAll(): Promise<BannerVO[]> {
  return get('/admin/banner/listAll')
}

// Banner详情
export function getBannerDetail(id: ID): Promise<BannerVO> {
  return get(`/admin/banner/${id}`)
}

// 创建Banner
export function createBanner(data: BannerCreateRequest): Promise<void> {
  return post('/admin/banner', data)
}

// 更新Banner
export function updateBanner(data: BannerUpdateRequest): Promise<void> {
  return put('/admin/banner', data)
}

// 更新Banner状态
export function updateBannerStatus(id: ID, status: number): Promise<void> {
  return put(`/admin/banner/${id}/status?status=${status}`)
}

// 删除Banner
export function deleteBanner(id: ID): Promise<void> {
  return del(`/admin/banner/${id}`)
}

// 调整排序
export function updateBannerSort(data: { id: ID; sortOrder: number }): Promise<void> {
  return put('/admin/banner/sort', data)
}
