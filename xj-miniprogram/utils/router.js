/**
 * Router - 路由封装
 */

/**
 * 路由配置
 */
const routes = {
  // TabBar页面
  home: '/pages/index/index',
  companion: '/pages/companion/list/index',
  trip: '/pages/trip/list/index',
  member: '/pages/member/index/index',

  // 线路相关
  routeList: '/pages/route/list/index',
  routeDetail: '/pages/route/detail/index',

  // 订单相关
  orderConfirm: '/pages/order/confirm/index',
  orderList: '/pages/order/list/index',
  orderDetail: '/pages/order/detail/index',
  orderRefund: '/pages/order/refund/index',

  // 搭子相关
  companionDetail: '/pages/companion/detail/index',

  // 会员相关
  profile: '/pages/member/profile/index',
  travelers: '/pages/member/travelers/index',
  favorite: '/pages/member/favorite/index',
  coupon: '/pages/member/coupon/index',
  settings: '/pages/member/settings/index',

  // 推广员相关
  promoterApply: '/pages/promoter/apply/index',
  promoterCenter: '/pages/promoter/center/index',
  promoterBindList: '/pages/promoter/bindList/index',

  // 消息相关
  messageList: '/pages/message/list/index',

  // 其他
  webview: '/pages/webview/index',
};

/**
 * TabBar页面列表
 */
const tabBarPages = [
  routes.home,
  routes.companion,
  routes.trip,
  routes.member,
];

/**
 * 判断是否是TabBar页面
 */
const isTabBarPage = (url) => {
  const path = url.split('?')[0];
  return tabBarPages.includes(path);
};

/**
 * 构建URL
 */
const buildUrl = (path, params = {}) => {
  if (!params || Object.keys(params).length === 0) {
    return path;
  }

  const query = Object.keys(params)
    .filter(key => params[key] !== undefined && params[key] !== null)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&');

  return query ? `${path}?${query}` : path;
};

/**
 * 跳转页面
 */
const navigateTo = (url, params = {}) => {
  const fullUrl = buildUrl(url, params);

  if (isTabBarPage(url)) {
    wx.switchTab({ url: fullUrl.split('?')[0] });
  } else {
    wx.navigateTo({ url: fullUrl });
  }
};

/**
 * 重定向
 */
const redirectTo = (url, params = {}) => {
  const fullUrl = buildUrl(url, params);

  if (isTabBarPage(url)) {
    wx.switchTab({ url: fullUrl.split('?')[0] });
  } else {
    wx.redirectTo({ url: fullUrl });
  }
};

/**
 * 返回上一页
 */
const navigateBack = (delta = 1) => {
  const pages = getCurrentPages();

  if (pages.length > delta) {
    wx.navigateBack({ delta });
  } else {
    wx.switchTab({ url: routes.home });
  }
};

/**
 * 切换Tab
 */
const switchTab = (url) => {
  wx.switchTab({ url: url.split('?')[0] });
};

/**
 * 重新加载当前页（清空所有页面跳转到首页）
 */
const reLaunch = (url = routes.home) => {
  wx.reLaunch({ url });
};

/**
 * 快捷跳转方法
 */
const go = {
  home: () => switchTab(routes.home),
  companion: () => switchTab(routes.companion),
  trip: () => switchTab(routes.trip),
  member: () => switchTab(routes.member),

  routeList: (params) => navigateTo(routes.routeList, params),
  routeDetail: (id) => navigateTo(routes.routeDetail, { id }),

  orderConfirm: (params) => navigateTo(routes.orderConfirm, params),
  orderList: (status) => navigateTo(routes.orderList, { status }),
  orderDetail: (id) => navigateTo(routes.orderDetail, { id }),
  orderRefund: (id) => navigateTo(routes.orderRefund, { orderId: id }),

  companionDetail: (id) => navigateTo(routes.companionDetail, { id }),

  profile: () => navigateTo(routes.profile),
  travelers: () => navigateTo(routes.travelers),
  favorite: () => navigateTo(routes.favorite),
  coupon: (params) => navigateTo(routes.coupon, params),
  settings: () => navigateTo(routes.settings),

  promoterApply: () => navigateTo(routes.promoterApply),
  promoterCenter: () => navigateTo(routes.promoterCenter),
  promoterBindList: () => navigateTo(routes.promoterBindList),

  messageList: () => navigateTo(routes.messageList),

  webview: (url, title) => navigateTo(routes.webview, { url, title }),
};

module.exports = {
  routes,
  isTabBarPage,
  buildUrl,
  navigateTo,
  redirectTo,
  navigateBack,
  switchTab,
  reLaunch,
  go,
};
