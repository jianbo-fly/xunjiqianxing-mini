// ==================== 通用类型 ====================

// ID 类型（支持大数字符串）
export type ID = string | number

// API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页响应类型
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
  totalPages?: number
}

// 分页请求参数
export interface PageQuery {
  page?: number
  pageSize?: number
}

// ==================== 认证 ====================

// 用户信息
export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar?: string
  role: 'admin' | 'supplier'
  supplierId?: number
  supplierName?: string
}

// 登录请求
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应（后端返回扁平结构）
export interface LoginResponse {
  token: string
  userId: number
  username: string
  nickname: string
  avatar?: string
  roleType: 'admin' | 'supplier'
  supplierId?: number
  supplierName?: string
}

// 菜单项
export interface MenuItem {
  path: string
  title: string
  icon?: string
  children?: MenuItem[]
  roles?: string[]
}

// ==================== 线路管理 ====================

// 线路查询参数
export interface RouteQueryRequest extends PageQuery {
  keyword?: string
  category?: string
  departureCity?: string
  supplierId?: ID
  status?: number
  auditStatus?: number
}

// 行程活动
export interface ActivityVO {
  icon: string
  time: string
  content: string
}

// 行程天
export interface ItineraryDayVO {
  day: number
  title: string
  activities: ActivityVO[]
  meals: string
  hotel: string
}

// 线路创建请求
export interface RouteCreateRequest {
  name: string
  subtitle?: string
  coverImage: string
  images?: string[]
  tags?: string[]
  category: string
  cityCode?: string
  cityName?: string
  departureCity?: string
  destination?: string
  originalPrice?: number
  minPrice?: number
  costExclude?: string
  bookingNotice?: string
  tips?: string
  costInclude?: string
  itinerary?: ItineraryDayVO[]
  sortOrder?: number
  isRecommend?: number
  supplierId?: ID
}

// 线路更新请求
export interface RouteUpdateRequest {
  id: ID
  name?: string
  subtitle?: string
  coverImage?: string
  images?: string[]
  tags?: string[]
  category?: string
  cityCode?: string
  cityName?: string
  departureCity?: string
  destination?: string
  originalPrice?: number
  minPrice?: number
  costExclude?: string
  bookingNotice?: string
  tips?: string
  costInclude?: string
  itinerary?: ItineraryDayVO[]
  sortOrder?: number
  isRecommend?: number
}

// 线路列表项
export interface RouteListVO {
  id: ID
  name: string
  subtitle: string
  coverImage: string
  tags: string[]
  category: string
  cityName: string
  destination: string
  minPrice: number
  originalPrice: number
  salesCount: number
  viewCount: number
  score: number
  status: number
  auditStatus: number
  auditRemark: string
  sortOrder: number
  isRecommend: number
  supplierId: ID
  supplierName: string
  packageCount: number
  createdAt: string
  updatedAt: string
}

// 线路详情
export interface RouteDetailVO extends RouteListVO {
  images: string[]
  cityCode: string
  departureCity: string
  costExclude: string
  bookingNotice: string
  tips: string
  costInclude: string
  itinerary: ItineraryDayVO[]
  packages: PackageVO[]
}

// 套餐
export interface PackageVO {
  id: ID
  name: string
  tags: string[]
  basePrice: number
  childPrice: number
  status: number
  sortOrder: number
  days: number
  nights: number
}

// 套餐创建/更新请求
export interface PackageRequest {
  id?: ID
  productId: ID
  name: string
  tags?: string[]
  basePrice: number
  childPrice?: number
  status?: number
  sortOrder?: number
  days: number
  nights: number
}

// 审核请求
export interface RouteAuditRequest {
  id: ID
  auditStatus: number // 1=通过, 2=拒绝
  auditRemark?: string
}

// 价格日历
export interface PriceCalendarItem {
  date: string
  price: number
  childPrice: number
  stock: number
  status: number
}

export interface PriceCalendarSaveRequest {
  skuId: ID
  prices: PriceCalendarItem[]
}

// ==================== 订单管理 ====================

// 订单查询参数
export interface OrderQueryRequest extends PageQuery {
  orderNo?: string
  userId?: ID
  supplierId?: ID
  productId?: ID
  status?: number
  contactName?: string
  contactPhone?: string
  startDateBegin?: string
  startDateEnd?: string
  createDateBegin?: string
  createDateEnd?: string
}

// 订单列表项
export interface OrderListVO {
  id: ID
  orderNo: string
  userId: ID
  userNickname: string
  userPhone: string
  productName: string
  productImage: string
  skuName: string
  startDate: string
  endDate: string
  days: number
  adultCount: number
  childCount: number
  totalAmount: number
  discountAmount: number
  payAmount: number
  contactName: string
  contactPhone: string
  status: number
  statusDesc: string
  payTime: string
  supplierId: ID
  supplierName: string
  remark: string
  adminRemark: string
  createdAt: string
}

// 订单详情
export interface OrderDetailVO extends OrderListVO {
  bizType: string
  productId: ID
  skuId: ID
  quantity: number
  adultPrice: number
  childPrice: number
  couponAmount: number
  refundAmount: number
  travelers: TravelerVO[]
  payStatus: number
  payTradeNo: string
  confirmTime: string
  cancelTime: string
  cancelReason: string
  rejectReason: string
  completeTime: string
  promoterId: ID
  extData: Record<string, any>
  updatedAt: string
  refund: RefundVO | null
}

// 出行人
export interface TravelerVO {
  name: string
  idCard: string
  phone: string
  travelerType: number
  travelerTypeDesc: string
}

// 订单确认请求
export interface OrderConfirmRequest {
  orderNo: string
  action: 'confirm' | 'reject'
  rejectReason?: string
  remark?: string
}

// 订单备注请求
export interface OrderRemarkRequest {
  orderNo: string
  remark: string
}

// 订单统计
export interface OrderStatsVO {
  pendingPayCount: number
  pendingConfirmCount: number
  pendingTravelCount: number
  travelingCount: number
  completedCount: number
  refundingCount: number
  todayOrderCount: number
  todaySalesAmount: number
  monthOrderCount: number
  monthSalesAmount: number
}

// ==================== 退款管理 ====================

// 退款查询参数
export interface RefundQueryRequest extends PageQuery {
  refundNo?: string
  orderNo?: string
  userId?: ID
  status?: number
  createDateBegin?: string
  createDateEnd?: string
}

// 退款列表项
export interface RefundListVO {
  id: ID
  refundNo: string
  orderId: ID
  orderNo: string
  userId: ID
  userNickname: string
  productName: string
  refundAmount: number
  actualAmount: number
  refundRatio: number
  reason: string
  status: number
  statusDesc: string
  auditTime: string
  auditRemark: string
  createdAt: string
}

// 退款详情
export interface RefundVO {
  refundNo: string
  refundAmount: number
  actualAmount: number
  refundRatio: number
  reason: string
  status: number
  auditTime: string
  auditRemark: string
  createdAt: string
}

// 退款审核请求
export interface RefundAuditRequest {
  id: ID
  status: number // 1=同意, 2=拒绝
  actualAmount?: number
  auditRemark?: string
}

// ==================== 用户管理 ====================

// 用户查询参数
export interface UserQueryRequest extends PageQuery {
  userId?: ID
  phone?: string
  nickname?: string
  isMember?: number
  isLeader?: number
  isPromoter?: number
  status?: number
  createDateBegin?: string
  createDateEnd?: string
}

// 用户列表项
export interface UserListVO {
  id: ID
  nickname: string
  avatar: string
  phone: string
  gender: number
  status: number
  isMember: number
  isLeader: number
  isPromoter: number
  orderCount: number
  totalAmount: number
  createdAt: string
}

// ==================== 会员管理 ====================

// 会员查询参数
export interface MemberQueryRequest extends PageQuery {
  userId?: ID
  phone?: string
  nickname?: string
  memberStatus?: number
  createDateBegin?: string
  createDateEnd?: string
}

// 会员列表项
export interface MemberListVO {
  userId: ID
  nickname: string
  avatar: string
  phone: string
  memberExpireAt: string
  memberStatus: number
  memberStatusDesc: string
  totalPaidAmount: number
  firstOpenAt: string
  remainDays: number
}

// 会员续期请求
export interface MemberExtendRequest {
  userId: ID
  days: number
  reason: string
}

// ==================== Banner管理 ====================

// Banner创建请求
export interface BannerCreateRequest {
  title?: string
  imageUrl: string
  linkType?: number
  linkValue?: string
  sortOrder?: number
  startTime?: string
  endTime?: string
}

// Banner更新请求
export interface BannerUpdateRequest {
  id: ID
  title?: string
  imageUrl?: string
  linkType?: number
  linkValue?: string
  sortOrder?: number
  startTime?: string
  endTime?: string
}

// Banner列表项
export interface BannerVO {
  id: ID
  title: string
  imageUrl: string
  linkType: number
  linkValue: string
  sortOrder: number
  status: number
  startTime: string
  endTime: string
  createdAt: string
}

// ==================== 分类管理 ====================

// 分类创建请求
export interface CategoryCreateRequest {
  name: string
  icon?: string
  bizType?: string
  sortOrder?: number
}

// 分类更新请求
export interface CategoryUpdateRequest {
  id: ID
  name?: string
  icon?: string
  bizType?: string
  sortOrder?: number
}

// 分类列表项
export interface CategoryVO {
  id: ID
  name: string
  icon: string
  bizType: string
  sortOrder: number
  status: number
  createdAt: string
}

// ==================== 供应商管理 ====================

// 供应商创建请求
export interface SupplierCreateRequest {
  name: string
  logo?: string
  phone: string
  intro?: string
  licenseImages?: string[]
  username: string
  password: string
}

// 供应商更新请求
export interface SupplierUpdateRequest {
  id: ID
  name?: string
  logo?: string
  phone?: string
  intro?: string
  licenseImages?: string[]
}

// 供应商列表项
export interface SupplierVO {
  id: ID
  name: string
  logo: string
  phone: string
  intro: string
  licenseImages: string[]
  status: number
  routeCount: number
  username: string
  createdAt: string
}
