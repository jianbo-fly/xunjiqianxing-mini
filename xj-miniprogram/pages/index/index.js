/**
 * 首页
 */
const homeApi = require('../../services/home');
const promoterApi = require('../../services/promoter');
const messageApi = require('../../services/message');
const { HOT_CITIES, CITY_GROUPS } = require('../../config/cities');
const { go } = require('../../utils/router');
const { isLogin } = require('../../utils/auth');
const appConfig = require('../../config/app.config');

const CITY_HISTORY_KEY = 'departureCityHistory';

Page({
  data: {
    statusBarHeight: 0,
    menuButtonRight: 32, // 胶囊按钮右侧到屏幕右边距（rpx）
    menuButtonLeft: 0,   // 胶囊按钮左边界（px），用于计算右侧 padding
    // 用户数据
    unreadCount: 0,
    // 出发城市
    departureCity: wx.getStorageSync('departureCity') || '上海',
    // 城市选择弹窗
    showCityPicker: false,
    cityScrollTo: '',
    hotCities: HOT_CITIES,
    cityGroups: CITY_GROUPS,
    cityIndexLetters: CITY_GROUPS.map(g => g.letter),
    cityHistory: [],
    // 轮播图
    banners: [],
    currentBanner: 0,
    // 功能入口（8个）
    entries: [
      { id: 'travel',   name: '旅游定制', icon: '/assets/icons/entry/travel.png',   type: 'travel', bgColor: 'linear-gradient(135deg,#fff5f3,#ffe8e3)', shadowColor: 'rgba(236,55,19,0.12)' },
      { id: 'car',      name: '租车自驾', icon: '/assets/icons/entry/car.png',      type: 'coming', bgColor: 'linear-gradient(135deg,#eff6ff,#dbeafe)', shadowColor: 'rgba(59,130,246,0.12)' },
      { id: 'hotel',    name: '酒店民宿', icon: '/assets/icons/entry/hotel.png',    type: 'coming', bgColor: 'linear-gradient(135deg,#f0fdf4,#dcfce7)', shadowColor: 'rgba(34,197,94,0.12)' },
      { id: 'ticket',   name: '景点门票', icon: '/assets/icons/entry/ticket.png',   type: 'coming', bgColor: 'linear-gradient(135deg,#fff7ed,#ffedd5)', shadowColor: 'rgba(249,115,22,0.12)' },
      { id: 'transfer', name: '接送包车', icon: '/assets/icons/entry/transfer.png', type: 'coming', bgColor: 'linear-gradient(135deg,#f5f3ff,#ede9fe)', shadowColor: 'rgba(139,92,246,0.12)' },
      { id: 'food',     name: '美食玩乐', icon: '/assets/icons/entry/food.png',     type: 'coming', bgColor: 'linear-gradient(135deg,#fefce8,#fef9c3)', shadowColor: 'rgba(234,179,8,0.12)' },
      { id: 'rent',     name: '租赁服务', icon: '/assets/icons/entry/rent.png',     type: 'coming', bgColor: 'linear-gradient(135deg,#f0fdfa,#ccfbf1)', shadowColor: 'rgba(20,184,166,0.12)' },
      { id: 'insurance',name: '旅游保险', icon: '/assets/icons/entry/insurance.png',type: 'coming', bgColor: 'linear-gradient(135deg,#ecfeff,#cffafe)', shadowColor: 'rgba(6,182,212,0.12)' },
    ],
    // 热门线路 - 瀑布流双列
    hotRoutes: [],
    leftRoutes: [],
    rightRoutes: [],
    // 加载状态
    loading: true,
  },

  onLoad(options) {
    try {
      const { statusBarHeight, windowWidth } = wx.getSystemInfoSync();
      const menuButton = wx.getMenuButtonBoundingClientRect();
      // 胶囊左边界即右侧需要留出的空间（px 转 rpx: *750/windowWidth）
      const menuButtonLeft = menuButton.left;
      const rightPadding = Math.ceil((windowWidth - menuButton.left) * 750 / windowWidth);
      this.setData({ statusBarHeight, menuButtonLeft: rightPadding });
    } catch (e) {
      this.setData({ statusBarHeight: 20, menuButtonLeft: 120 });
    }
    this.loadHomeData();

    // scene 捕获已移至 app.js handleScene 统一处理
  },

  onShow() {
    // 设置TabBar选中状态
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 0 });
    }
    // 刷新未读消息数
    this.loadUnreadCount();
    // 登录后尝试绑定推广员
    this.tryBindPromoter();
  },

  tryBindPromoter() {
    if (!isLogin()) return;
    const promoCode = wx.getStorageSync('pendingPromoCode');
    if (!promoCode) return;
    wx.removeStorageSync('pendingPromoCode');
    promoterApi.bind({ promoCode }).catch(() => {
      // 静默失败：已绑定或无效码均不提示
    });
  },

  onPullDownRefresh() {
    this.loadHomeData().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 加载首页数据
   */
  async loadHomeData() {
    this.setData({ loading: true });

    try {
      const data = await homeApi.getData();
      const hotRoutes = data.recommendRoutes || [];

      // 瀑布流分列：交替分配到左右列
      const leftRoutes = [];
      const rightRoutes = [];
      hotRoutes.forEach((route, index) => {
        if (index % 2 === 0) {
          leftRoutes.push(route);
        } else {
          rightRoutes.push(route);
        }
      });

      this.setData({
        banners: data.banners || [],
        hotRoutes,
        leftRoutes,
        rightRoutes,
      });
    } catch (e) {
      console.error('加载首页数据失败', e);
    } finally {
      this.setData({ loading: false });
    }
  },

  /**
   * 加载未读消息数
   */
  async loadUnreadCount() {
    if (!isLogin()) return;
    try {
      const count = await messageApi.getUnreadCount();
      this.setData({ unreadCount: count || 0 });
    } catch (e) {
      // 静默失败，不影响页面展示
    }
  },

  /**
   * 轮播切换
   */
  handleBannerChange(e) {
    this.setData({ currentBanner: e.detail.current });
  },

  /**
   * 出发城市选择 - 打开弹窗
   */
  handleCityTap() {
    const history = wx.getStorageSync(CITY_HISTORY_KEY) || [];
    this.setData({ showCityPicker: true, cityScrollTo: '', cityHistory: history });
  },

  /**
   * 关闭城市弹窗
   */
  handleCloseCityPicker() {
    this.setData({ showCityPicker: false });
  },

  /**
   * 选中城市
   */
  handleCitySelect(e) {
    const city = e.currentTarget.dataset.city;
    // 更新历史（最多5条，去重）
    let history = wx.getStorageSync(CITY_HISTORY_KEY) || [];
    history = [city, ...history.filter(c => c !== city)].slice(0, 5);
    wx.setStorageSync(CITY_HISTORY_KEY, history);
    wx.setStorageSync('departureCity', city);
    this.setData({ departureCity: city, showCityPicker: false, cityHistory: history });
  },

  /**
   * 清空历史
   */
  handleClearCityHistory() {
    wx.removeStorageSync(CITY_HISTORY_KEY);
    this.setData({ cityHistory: [] });
  },

  /**
   * 点击右侧字母索引跳转
   */
  handleLetterTap(e) {
    const letter = e.currentTarget.dataset.letter;
    this.setData({ cityScrollTo: `city-letter-${letter}` });
  },

  /**
   * 搜索点击
   */
  handleSearchTap() {
    wx.navigateTo({ url: `/pages/search/index?departureCity=${encodeURIComponent(this.data.departureCity)}` });
  },

  /**
   * 消息入口点击
   */
  handleMessageTap() {
    wx.navigateTo({ url: '/pages/message/list/index' });
  },

  /**
   * Banner点击
   */
  handleBannerTap(e) {
    const { item } = e.currentTarget.dataset;
    if (!item) return;

    if (item.linkType === 1 && item.linkValue) {
      go.routeDetail(item.linkValue);
    } else if (item.linkType === 2 && item.linkValue) {
      go.webview(item.linkValue);
    } else if (item.linkType === 3 && item.linkValue) {
      wx.navigateTo({ url: item.linkValue });
    }
  },

  /**
   * 功能入口点击
   */
  handleEntryTap(e) {
    const { item } = e.currentTarget.dataset;
    if (!item) return;

    switch (item.type) {
      case 'travel':
        wx.navigateTo({ url: '/pages/travel/index/index' });
        break;
      case 'route':
        go.routeList();
        break;
      case 'custom':
        wx.navigateTo({ url: '/pages/custom/index/index' });
        break;
      case 'coming':
        wx.showToast({ title: '敬请期待', icon: 'none' });
        break;
      default:
        break;
    }
  },

  /**
   * 线路点击
   */
  handleRouteTap(e) {
    const { id } = e.currentTarget.dataset;
    if (id) {
      go.routeDetail(id);
    }
  },

  /**
   * 查看更多线路
   */
  handleMoreTap() {
    go.routeList();
  },

  /**
   * 在线客服
   */
  handleServiceTap() {
    if (appConfig.features.customerService.type === 'weixin') {
      wx.openCustomerServiceChat({
        extInfo: { url: appConfig.features.customerService.corpId || '' },
        corpId: appConfig.features.customerService.corpId || '',
        success() {},
        fail() {
          wx.showToast({ title: '客服暂不可用', icon: 'none' });
        }
      });
    } else {
      wx.makePhoneCall({
        phoneNumber: appConfig.features.customerService.phone || '',
        fail() {}
      });
    }
  },

  /**
   * 分享
   */
  onShareAppMessage() {
    return {
      title: appConfig.share.defaultTitle,
      path: '/pages/index/index',
      imageUrl: appConfig.share.defaultImage,
    };
  },
});
