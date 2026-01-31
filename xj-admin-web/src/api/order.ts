import { get, post } from '@/utils/request'
import type {
  PageResult,
  OrderQueryRequest,
  OrderListVO,
  OrderDetailVO,
  OrderConfirmRequest,
  OrderRemarkRequest,
  OrderStatsVO,
} from '@/types'

// 订单列表
export function getOrderList(params: OrderQueryRequest): Promise<PageResult<OrderListVO>> {
  return get('/admin/order/list', params)
}

// 订单详情
export function getOrderDetail(orderNo: string): Promise<OrderDetailVO> {
  return get(`/admin/order/${orderNo}`)
}

// 确认/拒绝订单
export function confirmOrder(data: OrderConfirmRequest): Promise<void> {
  return post('/admin/order/confirm', data)
}

// 添加订单备注
export function addOrderRemark(data: OrderRemarkRequest): Promise<void> {
  return post('/admin/order/remark', data)
}

// 订单统计
export function getOrderStats(): Promise<OrderStatsVO> {
  return get('/admin/order/stats')
}
