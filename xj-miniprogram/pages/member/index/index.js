/**
 * 会员中心
 */
const app = getApp();
const userApi = require('../../../services/user');
const orderApi = require('../../../services/order');
const { go } = require('../../../utils/router');
const { isLogin, checkLogin } = require('../../../utils/auth');
const assets = require('../../../assets/index');
const appConfig = require('../../../config/app.config');

Page({
  data: {
    assets,
    userInfo: null,
    isLogin: false,
    orderCounts: {
      pending: 0,
      confirming: 0,
      travelling: 0,
    },
  },

  onLoad() {
    this.checkLogin();
  },

  onShow() {
    this.checkLogin();

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
      this.loadOrderCounts();
    }
  },

  /**
   * 加载用户信息
   */
  async loadUserInfo() {
    try {
      const userInfo = await userApi.getInfo();
      this.setData({ userInfo });
      app.globalData.userInfo = userInfo;
      wx.setStorageSync('userInfo', userInfo);
    } catch (e) {
      console.error('获取用户信息失败', e);
    }
  },

  /**
   * 加载订单数量
   */
  async loadOrderCounts() {
    try {
      const counts = await orderApi.getCounts();
      this.setData({
        orderCounts: {
          pending: counts.pending || 0,
          confirming: counts.confirming || 0,
          travelling: counts.travelling || 0,
        },
      });
    } catch (e) {
      // 静默失败，不影响页面展示
    }
  },

  /**
   * 头像点击
   */
  handleAvatarTap() {
    if (this.data.isLogin) {
      go.profile();
    } else {
      wx.navigateTo({ url: '/pages/login/index' });
    }
  },

  /**
   * 扫码
   */
  handleScanTap() {
    wx.scanCode({
      success: (res) => {
        console.log('扫码结果', res);
      },
    });
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
   * 开通会员
   */
  handleMemberTap() {
    if (!checkLogin()) return;
    wx.showToast({ title: '会员功能即将上线', icon: 'none' });
  },

  /**
   * 成为领队
   */
  handleLeaderTap() {
    if (!checkLogin()) return;
    wx.showToast({ title: '领队功能即将上线', icon: 'none' });
  },

  /**
   * 推广中心
   */
  handlePromoterTap() {
    if (!checkLogin()) return;
    if (this.data.userInfo?.isPromoter) {
      go.promoterCenter();
    } else {
      go.promoterApply();
    }
  },

  /**
   * 关于我们
   */
  handleAboutTap() {
    go.webview && go.webview('about');
  },

  /**
   * 设置
   */
  handleSettingsTap() {
    go.settings();
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
