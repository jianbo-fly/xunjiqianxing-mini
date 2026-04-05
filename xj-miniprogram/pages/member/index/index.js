/**
 * 会员中心
 */
const app = getApp();
const userApi = require('../../../services/user');
const orderApi = require('../../../services/order');
const customApi = require('../../../services/custom');
const { go } = require('../../../utils/router');
const { isLogin, checkLogin, clearAll } = require('../../../utils/auth');
const assets = require('../../../assets/index');
const appConfig = require('../../../config/app.config');

Page({
  data: {
    assets,
    statusBarHeight: 0,
    userInfo: null,
    isLogin: false,
    orderCounts: {
      pending: 0,
      confirming: 0,
      travelling: 0,
    },
    customCounts: {
      pending: 0,
      following: 0,
      completed: 0,
    },
  },

  onLoad() {
    const windowInfo = wx.getWindowInfo();
    this.setData({ statusBarHeight: windowInfo.statusBarHeight || 20 });
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
      this.loadCustomCounts();
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
   * 加载定制数量
   */
  async loadCustomCounts() {
    try {
      const counts = await customApi.getCounts();
      this.setData({
        customCounts: {
          pending: counts.pending || 0,
          following: counts.following || 0,
          completed: counts.completed || 0,
        },
      });
    } catch (e) {
      // 静默失败
    }
  },

  /**
   * 我的定制入口
   */
  handleCustomTap(e) {
    if (!checkLogin()) return;
    const status = e.currentTarget.dataset.status;
    const url = status !== undefined
      ? `/pages/custom/list/index?tab=${parseInt(status) + 1}`
      : '/pages/custom/list/index';
    wx.navigateTo({ url });
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
   * 开通会员 / 查看会员权益
   */
  handleMemberTap() {
    if (!checkLogin()) return;
    wx.navigateTo({ url: '/pages/member/vip/index' });
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
   * 优惠券
   */
  handleCouponTap() {
    if (!checkLogin()) return;
    wx.showToast({ title: '优惠券功能即将上线', icon: 'none' });
  },

  /**
   * 联系我们
   */
  handleContactTap() {
    wx.makePhoneCall({
      phoneNumber: '400-000-0000',
      fail: () => {
        wx.showToast({ title: '拨打失败', icon: 'none' });
      },
    });
  },

  /**
   * 关于我们
   */
  handleAboutTap() {
    wx.navigateTo({ url: '/pages/member/about/index' });
  },

  /**
   * 退出登录
   */
  handleLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出登录吗？',
      confirmText: '退出',
      confirmColor: '#EC3713',
      success: (res) => {
        if (res.confirm) {
          clearAll();
          app.globalData.userInfo = null;
          app.globalData.token = '';
          this.setData({
            isLogin: false,
            userInfo: null,
            orderCounts: { pending: 0, confirming: 0, travelling: 0 },
          });
        }
      },
    });
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
