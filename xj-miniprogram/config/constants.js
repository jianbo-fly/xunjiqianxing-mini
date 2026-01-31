/**
 * Constants - 常量定义
 */

module.exports = {
  // ========================
  // 订单状态
  // ========================
  ORDER_STATUS: {
    PENDING_PAYMENT: 0,    // 待支付
    PENDING_CONFIRM: 1,    // 待确认
    CONFIRMED: 2,          // 已确认
    TRAVELLING: 3,         // 出行中
    COMPLETED: 4,          // 已完成
    CANCELLED: 5,          // 已取消
    REFUNDING: 6,          // 退款中
    REFUNDED: 7,           // 已退款
  },

  ORDER_STATUS_TEXT: {
    0: '待支付',
    1: '待确认',
    2: '已确认',
    3: '出行中',
    4: '已完成',
    5: '已取消',
    6: '退款中',
    7: '已退款',
  },

  ORDER_STATUS_COLOR: {
    0: '#FA8C16',  // 待支付 - 橙色
    1: '#1890FF',  // 待确认 - 蓝色
    2: '#52C41A',  // 已确认 - 绿色
    3: '#1890FF',  // 出行中 - 蓝色
    4: '#999999',  // 已完成 - 灰色
    5: '#999999',  // 已取消 - 灰色
    6: '#FA8C16',  // 退款中 - 橙色
    7: '#999999',  // 已退款 - 灰色
  },

  // ========================
  // 推广员状态
  // ========================
  PROMOTER_STATUS: {
    PENDING: 0,      // 待审核
    APPROVED: 1,     // 已通过
    REJECTED: 2,     // 已拒绝
    DISABLED: 3,     // 已禁用
  },

  PROMOTER_STATUS_TEXT: {
    0: '审核中',
    1: '已通过',
    2: '已拒绝',
    3: '已禁用',
  },

  // ========================
  // 优惠券状态
  // ========================
  COUPON_STATUS: {
    UNUSED: 0,       // 未使用
    USED: 1,         // 已使用
    EXPIRED: 2,      // 已过期
  },

  COUPON_STATUS_TEXT: {
    0: '未使用',
    1: '已使用',
    2: '已过期',
  },

  // ========================
  // 证件类型
  // ========================
  ID_TYPE: {
    ID_CARD: 1,           // 身份证
    PASSPORT: 2,          // 护照
    HK_MACAO_PASS: 3,     // 港澳通行证
    TAIWAN_PASS: 4,       // 台湾通行证
  },

  ID_TYPE_TEXT: {
    1: '身份证',
    2: '护照',
    3: '港澳通行证',
    4: '台湾通行证',
  },

  // ========================
  // 存储Key
  // ========================
  STORAGE_KEYS: {
    TOKEN: 'token',
    USER_INFO: 'userInfo',
    THEME: 'theme',
    SEARCH_HISTORY: 'searchHistory',
    RECENT_VIEW: 'recentView',
  },

  // ========================
  // 事件名称
  // ========================
  EVENTS: {
    LOGIN_SUCCESS: 'loginSuccess',
    LOGOUT: 'logout',
    ORDER_STATUS_CHANGE: 'orderStatusChange',
    FAVORITE_CHANGE: 'favoriteChange',
    USER_INFO_UPDATE: 'userInfoUpdate',
  },

  // ========================
  // 正则表达式
  // ========================
  REGEX: {
    PHONE: /^1[3-9]\d{9}$/,
    ID_CARD: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/,
    EMAIL: /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/,
  },
};
