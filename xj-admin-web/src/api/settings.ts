import request from '@/utils/request'

export interface SystemSettings {
  // 基础配置
  customerServicePhone: string
  wecomServiceUrl: string
  paymentTimeout: number
  childAgeLimit: number

  // 积分规则
  orderPointsRate: number
  promoterPointsRate: number

  // 退款规则
  refundRules: RefundRule[]

  // 价格配置
  memberPrice: number
  leaderPrice: number
}

export interface RefundRule {
  daysBeforeStart: number
  refundRate: number
}

// 获取系统配置
export function getSystemSettings() {
  return request.get<SystemSettings>('/admin/settings')
}

// 更新系统配置
export function updateSystemSettings(data: Partial<SystemSettings>) {
  return request.post('/admin/settings', data)
}

// 获取基础配置
export function getBasicSettings() {
  return request.get<{
    customerServicePhone: string
    wecomServiceUrl: string
    paymentTimeout: number
    childAgeLimit: number
  }>('/admin/settings/basic')
}

// 更新基础配置
export function updateBasicSettings(data: {
  customerServicePhone: string
  wecomServiceUrl: string
  paymentTimeout: number
  childAgeLimit: number
}) {
  return request.post('/admin/settings/basic', data)
}

// 获取积分规则
export function getPointsSettings() {
  return request.get<{
    orderPointsRate: number
    promoterPointsRate: number
  }>('/admin/settings/points')
}

// 更新积分规则
export function updatePointsSettings(data: {
  orderPointsRate: number
  promoterPointsRate: number
}) {
  return request.post('/admin/settings/points', data)
}

// 获取退款规则
export function getRefundSettings() {
  return request.get<{ rules: RefundRule[] }>('/admin/settings/refund')
}

// 更新退款规则
export function updateRefundSettings(data: { rules: RefundRule[] }) {
  return request.post('/admin/settings/refund', data)
}

// 获取价格配置
export function getPriceSettings() {
  return request.get<{
    memberPrice: number
    leaderPrice: number
  }>('/admin/settings/price')
}

// 更新价格配置
export function updatePriceSettings(data: {
  memberPrice: number
  leaderPrice: number
}) {
  return request.post('/admin/settings/price', data)
}
