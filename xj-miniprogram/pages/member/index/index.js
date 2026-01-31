/**
 * 会员中心
 */
const app = getApp();
const userApi = require('../../../services/user');
const { go } = require('../../../utils/router');
const { isLogin, checkLogin } = require('../../../utils/auth');
const assets = require('../../../assets/index');
const appConfig = require('../../../config/app.config');

Page({
  data: {
    assets,
    userInfo: null,
    isLogin: false,
    // 菜单列表
    menuList: [
      { icon: '📋', title: '我的订单', path: 'orderList' },
      { icon: '❤️', title: '我的收藏', path: 'favorite' },
      { icon: '🎫', title: '优惠券', path: 'coupon' },
      { icon: '👥', title: '出行人管理', path: 'travelers' },
    ],
    // 功能开关
    features: appConfig.features,
  },

  onLoad() {
    this.checkLogin();
  },

  onShow() {
    // 刷新登录状态
    this.checkLogin();

    // 设置TabBar选中状态
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 3 });
    }
  },

  /**
   * 检查登录状态
   */
  checkLogin() {
    const loginStatus = isLogin();
    this.setData({ isLogin: loginStatus });

    if (loginStatus) {
      this.loadUserInfo();
    }
  },

  /**
   * 加载用户信息
   */
  async loadUserInfo() {
    try {
      const userInfo = await userApi.getInfo();
      this.setData({ userInfo });
      // 更新全局状态和存储
      app.globalData.userInfo = userInfo;
      wx.setStorageSync('userInfo', userInfo);
    } catch (e) {
      console.error('获取用户信息失败', e);
    }
  },

  /**
   * 登录
   */
  handleLogin() {
    wx.navigateTo({ url: '/pages/login/index' });
  },

  /**
   * 头像点击
   */
  handleAvatarTap() {
    if (this.data.isLogin) {
      go.profile();
    } else {
      this.handleLogin();
    }
  },

  /**
   * 菜单点击
   */
  handleMenuTap(e) {
    const { path } = e.currentTarget.dataset;
    if (!checkLogin()) return;

    if (go[path]) {
      go[path]();
    }
  },

  /**
   * 订单Tab点击
   */
  handleOrderTap(e) {
    const { status } = e.currentTarget.dataset;
    if (!checkLogin()) return;
    go.orderList(status);
  },

  /**
   * 推广员入口
   */
  handlePromoterTap() {
    if (!checkLogin()) return;
    // 判断是否已是推广员
    if (this.data.userInfo?.isPromoter) {
      go.promoterCenter();
    } else {
      go.promoterApply();
    }
  },

  /**
   * 设置
   */
  handleSettingsTap() {
    go.settings();
  },

  /**
   * 客服
   */
  handleServiceTap() {
    // 使用微信客服
  },

  /**
   * 退出登录
   */
  handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            // 调用退出登录接口
            await userApi.logout();
          } catch (e) {
            // 忽略接口错误，继续清除本地状态
          }
          // 清除登录信息
          app.clearLoginInfo();
          this.setData({
            isLogin: false,
            userInfo: null,
          });
          wx.showToast({ title: '已退出登录', icon: 'success' });
        }
      }
    });
  },

  /**
   * 分享
   */
  onShareAppMessage() {
    return {
      title: appConfig.share.defaultTitle,
      path: '/pages/index/index',
    };
  },
});
