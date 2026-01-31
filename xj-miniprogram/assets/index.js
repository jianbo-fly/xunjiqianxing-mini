/**
 * Assets - 静态资源路径配置
 *
 * 集中管理资源路径，便于后期统一替换
 */

const BASE_PATH = '/assets';

const assets = {
  // ==================== TabBar图标 ====================
  tabbar: {
    home: `${BASE_PATH}/icons/tabbar/home.png`,
    homeActive: `${BASE_PATH}/icons/tabbar/home-active.png`,
    companion: `${BASE_PATH}/icons/tabbar/companion.png`,
    companionActive: `${BASE_PATH}/icons/tabbar/companion-active.png`,
    trip: `${BASE_PATH}/icons/tabbar/trip.png`,
    tripActive: `${BASE_PATH}/icons/tabbar/trip-active.png`,
    member: `${BASE_PATH}/icons/tabbar/member.png`,
    memberActive: `${BASE_PATH}/icons/tabbar/member-active.png`,
  },

  // ==================== 通用图标 ====================
  icons: {
    // 箭头
    arrowRight: `${BASE_PATH}/icons/common/arrow-right.png`,
    arrowLeft: `${BASE_PATH}/icons/common/arrow-left.png`,
    arrowUp: `${BASE_PATH}/icons/common/arrow-up.png`,
    arrowDown: `${BASE_PATH}/icons/common/arrow-down.png`,

    // 操作
    close: `${BASE_PATH}/icons/common/close.png`,
    search: `${BASE_PATH}/icons/common/search.png`,
    share: `${BASE_PATH}/icons/common/share.png`,
    more: `${BASE_PATH}/icons/common/more.png`,
    filter: `${BASE_PATH}/icons/common/filter.png`,

    // 收藏
    favorite: `${BASE_PATH}/icons/common/favorite.png`,
    favoriteActive: `${BASE_PATH}/icons/common/favorite-active.png`,

    // 信息
    location: `${BASE_PATH}/icons/common/location.png`,
    phone: `${BASE_PATH}/icons/common/phone.png`,
    time: `${BASE_PATH}/icons/common/time.png`,
    calendar: `${BASE_PATH}/icons/common/calendar.png`,
    user: `${BASE_PATH}/icons/common/user.png`,
    message: `${BASE_PATH}/icons/common/message.png`,

    // 状态
    success: `${BASE_PATH}/icons/common/success.png`,
    warning: `${BASE_PATH}/icons/common/warning.png`,
    error: `${BASE_PATH}/icons/common/error.png`,
    info: `${BASE_PATH}/icons/common/info.png`,

    // 其他
    edit: `${BASE_PATH}/icons/common/edit.png`,
    delete: `${BASE_PATH}/icons/common/delete.png`,
    copy: `${BASE_PATH}/icons/common/copy.png`,
    camera: `${BASE_PATH}/icons/common/camera.png`,
    image: `${BASE_PATH}/icons/common/image.png`,
  },

  // ==================== 订单图标 ====================
  order: {
    pending: `${BASE_PATH}/icons/order/status-pending.png`,
    confirmed: `${BASE_PATH}/icons/order/status-confirmed.png`,
    travelling: `${BASE_PATH}/icons/order/status-travelling.png`,
    completed: `${BASE_PATH}/icons/order/status-completed.png`,
    cancelled: `${BASE_PATH}/icons/order/status-cancelled.png`,
    refunding: `${BASE_PATH}/icons/order/status-refunding.png`,
  },

  // ==================== 空状态图 ====================
  empty: {
    default: `${BASE_PATH}/images/empty/empty-default.png`,
    order: `${BASE_PATH}/images/empty/empty-order.png`,
    favorite: `${BASE_PATH}/images/empty/empty-favorite.png`,
    network: `${BASE_PATH}/images/empty/empty-network.png`,
    search: `${BASE_PATH}/images/empty/empty-search.png`,
    message: `${BASE_PATH}/images/empty/empty-message.png`,
    coupon: `${BASE_PATH}/images/empty/empty-coupon.png`,
  },

  // ==================== 占位图 ====================
  placeholder: {
    route: `${BASE_PATH}/images/placeholder/route.png`,
    avatar: `${BASE_PATH}/images/placeholder/avatar.png`,
    image: `${BASE_PATH}/images/placeholder/image.png`,
  },

  // ==================== 背景图 ====================
  bg: {
    memberHeader: `${BASE_PATH}/images/bg/member-header.png`,
    promoterHeader: `${BASE_PATH}/images/bg/promoter-header.png`,
  },

  // ==================== 其他图片 ====================
  images: {
    logo: `${BASE_PATH}/images/logo.png`,
    shareDefault: `${BASE_PATH}/images/share-default.png`,
    loginBg: `${BASE_PATH}/images/login-bg.png`,
  },
};

module.exports = assets;
