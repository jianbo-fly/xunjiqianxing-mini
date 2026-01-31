/**
 * App Configuration - 应用配置
 *
 * 功能开关和业务配置
 */

module.exports = {
  // ========================
  // 应用信息
  // ========================
  app: {
    name: '寻迹千行',
    version: '1.0.0',
    description: '户外旅行线路预订平台',
  },

  // ========================
  // 功能开关
  // ========================
  features: {
    // 推广员功能
    promoter: {
      enabled: true,
      applyEnabled: true,  // 是否开放申请
    },
    // 搭子（同行）功能
    companion: {
      enabled: true,
    },
    // 会员等级功能
    memberLevel: {
      enabled: true,
    },
    // 优惠券功能
    coupon: {
      enabled: true,
    },
    // 评价功能
    review: {
      enabled: true,
    },
    // 客服功能
    customerService: {
      enabled: true,
      type: 'weixin',  // weixin | phone | both
      phone: '400-xxx-xxxx',
    },
  },

  // ========================
  // 首页模块配置
  // ========================
  homeModules: [
    {
      type: 'banner',
      visible: true,
    },
    {
      type: 'search',
      visible: true,
      placeholder: '搜索目的地、线路名称',
    },
    {
      type: 'navigation',
      visible: true,
    },
    {
      type: 'hotRoutes',
      visible: true,
      title: '热门线路',
      showMore: true,
    },
  ],

  // ========================
  // 首页导航入口配置
  // ========================
  navigations: [
    {
      id: 'travel',
      name: '旅行',
      icon: '/assets/icons/nav/travel.png',
      type: 'route',
      params: { categoryId: 1 },
    },
    {
      id: 'carRental',
      name: '租车',
      icon: '/assets/icons/nav/car-rental.png',
      type: 'route',
      params: { categoryId: 2 },
    },
    {
      id: 'custom',
      name: '定制游',
      icon: '/assets/icons/nav/custom.png',
      type: 'page',
      path: '/pages/custom/index/index',
    },
    {
      id: 'companion',
      name: '找搭子',
      icon: '/assets/icons/nav/companion.png',
      type: 'tab',
      path: '/pages/companion/list/index',
    },
  ],

  // ========================
  // 分享配置
  // ========================
  share: {
    defaultTitle: '寻迹千行 - 发现不一样的旅行',
    defaultImage: '/assets/images/share-default.png',
  },

  // ========================
  // 订单配置
  // ========================
  order: {
    // 待支付订单超时时间（分钟）
    paymentTimeout: 30,
    // 退款原因选项
    refundReasons: [
      '行程有变，无法出行',
      '预订信息填写错误',
      '找到更优惠的价格',
      '其他原因',
    ],
  },

  // ========================
  // 表单配置
  // ========================
  forms: {
    // 证件类型
    idTypes: [
      { value: 1, label: '身份证' },
      { value: 2, label: '护照' },
      { value: 3, label: '港澳通行证' },
      { value: 4, label: '台湾通行证' },
    ],
    // 性别
    genders: [
      { value: 1, label: '男' },
      { value: 2, label: '女' },
    ],
  },

  // ========================
  // 列表配置
  // ========================
  list: {
    // 默认每页条数
    defaultPageSize: 10,
    // 空状态文案
    emptyText: {
      default: '暂无数据',
      order: '暂无订单',
      favorite: '暂无收藏',
      coupon: '暂无优惠券',
      message: '暂无消息',
      search: '未找到相关内容',
    },
  },
};
