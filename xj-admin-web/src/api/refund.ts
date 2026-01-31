import { get, post } from '@/utils/request'
import type {
  ID,
  PageResult,
  RefundQueryRequest,
  RefundListVO,
  RefundAuditRequest,
} from '@/types'

// 退款列表
export function getRefundList(params: RefundQueryRequest): Promise<PageResult<RefundListVO>> {
  return get('/admin/refund/list', params)
}

// 退款详情
export function getRefundDetail(id: ID): Promise<RefundListVO> {
  return get(`/admin/refund/${id}`)
}

// 审核退款
export function auditRefund(data: RefundAuditRequest): Promise<void> {
  return post('/admin/refund/audit', data)
}

// 待处理退款数量
export function getRefundPendingCount(): Promise<number> {
  return get('/admin/refund/pendingCount')
}
