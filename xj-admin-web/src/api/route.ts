import { get, post, put, del } from '@/utils/request'
import type {
  ID,
  PageResult,
  RouteQueryRequest,
  RouteListVO,
  RouteDetailVO,
  RouteCreateRequest,
  RouteUpdateRequest,
  RouteAuditRequest,
  PackageVO,
  PackageRequest,
  PriceCalendarItem,
  PriceCalendarSaveRequest,
} from '@/types'

// 线路列表
export function getRouteList(params: RouteQueryRequest): Promise<PageResult<RouteListVO>> {
  return get('/admin/route/list', params)
}

// 线路详情
export function getRouteDetail(id: ID): Promise<RouteDetailVO> {
  return get(`/admin/route/${id}`)
}

// 创建线路
export function createRoute(data: RouteCreateRequest): Promise<void> {
  return post('/admin/route', data)
}

// 更新线路
export function updateRoute(data: RouteUpdateRequest): Promise<void> {
  return put('/admin/route', data)
}

// 删除线路
export function deleteRoute(id: ID): Promise<void> {
  return del(`/admin/route/${id}`)
}

// 更新线路状态（上架/下架）
export function updateRouteStatus(id: ID, status: number): Promise<void> {
  return put(`/admin/route/${id}/status?status=${status}`)
}

// 线路审核
export function auditRoute(data: RouteAuditRequest): Promise<void> {
  return post('/admin/route/audit', data)
}

// 获取线路套餐列表
export function getRoutePackages(productId: ID): Promise<PackageVO[]> {
  return get(`/admin/route/${productId}/packages`)
}

// 创建套餐
export function createPackage(data: PackageRequest): Promise<void> {
  return post('/admin/route/package', data)
}

// 更新套餐
export function updatePackage(data: PackageRequest): Promise<void> {
  return put('/admin/route/package', data)
}

// 删除套餐
export function deletePackage(id: ID): Promise<void> {
  return del(`/admin/route/package/${id}`)
}

// 获取价格日历
export function getPriceCalendar(skuId: ID, startDate: string, endDate: string): Promise<PriceCalendarItem[]> {
  return get(`/admin/route/package/${skuId}/calendar`, { startDate, endDate })
}

// 设置价格日历
export function savePriceCalendar(data: PriceCalendarSaveRequest): Promise<void> {
  return post('/admin/route/package/prices', data)
}
