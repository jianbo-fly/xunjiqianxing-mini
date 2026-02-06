/**
 * API Configuration - API配置
 *
 * 根据后端实际接口路径配置
 * 后端端口：小程序8080，管理后台8081
 */

// 环境配置
const ENV = {
  dev: {
    baseUrl: 'http://localhost:8080',
  },
  test: {
    baseUrl: 'https://test-api.xunjiqianxing.com',
  },
  prod: {
    baseUrl: 'https://api.xunjiqianxing.com',
  }
};

// 当前环境 - 发布时改为 prod
const currentEnv = 'dev';

module.exports = {
  env: currentEnv,
  baseUrl: ENV[currentEnv].baseUrl,

  // API路径 - 对应后端接口
  paths: {
    // 用户模块 /api/user
    user: {
      wxLogin: '/api/user/loginByCode',
      info: '/api/user/info',
      update: '/api/user/info',
      logout: '/api/user/logout',
      bindPhoneByWx: '/api/user/bindPhoneByWx',
      bindPhone: '/api/user/bindPhone',
      sendCode: '/api/user/sendCode',
    },

    // 首页模块 /api/home
    home: {
      data: '/api/home/data',
      banners: '/api/home/banners',
      recommend: '/api/home/recommend',
    },

    // 线路模块 /api/route
    route: {
      list: '/api/route/list',
      detail: '/api/route',  // /api/route/{id}
      packages: '/api/route',  // /api/route/{id}/packages
      packageDetail: '/api/route/package',  // /api/route/package/{id}
      calendar: '/api/route/package',  // /api/route/package/{id}/calendar
    },

    // 订单模块 /api/order
    order: {
      confirm: '/api/order/confirm',  // 订单确认信息
      create: '/api/order/create',
      list: '/api/order/list',
      detail: '/api/order',  // /api/order/{id}
      cancel: '/api/order',  // /api/order/{id}/cancel
      refund: '/api/order',  // /api/order/{id}/refund
    },

    // 定制游模块 /api/custom
    custom: {
      submit: '/api/custom/submit',
      list: '/api/custom/list',
      detail: '/api/custom',  // /api/custom/{id}
      cancel: '/api/custom',  // /api/custom/{id}/cancel
    },

    // 会员模块 /api/member
    member: {
      info: '/api/member/info',
      buy: '/api/member/buy',
      benefits: '/api/member/benefits',
    },

    // 出行人模块 /api/traveler
    traveler: {
      list: '/api/traveler/list',
      detail: '/api/traveler',  // /api/traveler/{id}
      add: '/api/traveler/add',
      update: '/api/traveler',  // /api/traveler/{id}/update
      delete: '/api/traveler',  // /api/traveler/{id}/delete
      setDefault: '/api/traveler',  // /api/traveler/{id}/setDefault
    },

    // 优惠券模块 /api/coupon
    coupon: {
      available: '/api/coupon/available',
      receive: '/api/coupon/receive',  // /api/coupon/receive/{id}
      my: '/api/coupon/my',
      usable: '/api/coupon/usable',
    },

    // 推广员模块 /api/promoter
    promoter: {
      info: '/api/promoter/info',
      apply: '/api/promoter/apply',
      bind: '/api/promoter/bind',
      commissions: '/api/promoter/commissions',
      withdraw: '/api/promoter/withdraw',
      withdraws: '/api/promoter/withdraws',
      statistics: '/api/promoter/statistics',
    },

    // 支付模块 /api/payment
    payment: {
      orderPay: '/api/payment/order',  // /api/payment/order/{orderId}
      memberPay: '/api/payment/member',  // /api/payment/member/{orderId}
      status: '/api/payment/status',  // /api/payment/status/{paymentNo}
    },

    // 收藏模块（待后端实现）
    favorite: {
      list: '/api/favorite/list',
      add: '/api/favorite/add',
      remove: '/api/favorite/remove',
      check: '/api/favorite/check',
    },

    // 搭子模块（待后端实现）
    companion: {
      list: '/api/companion/list',
      detail: '/api/companion',
      join: '/api/companion/join',
      create: '/api/companion/create',
    },

    // 消息模块（待后端实现）
    message: {
      list: '/api/message/list',
      read: '/api/message/read',
      unreadCount: '/api/message/unreadCount',
    },

    // 通用模块
    common: {
      upload: '/api/common/upload',
      config: '/api/common/config',
      regions: '/api/common/regions',
    },
  },
};
