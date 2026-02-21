/**
 * 首页
 */
const homeApi = require('../../services/home');
const { go } = require('../../utils/router');
const appConfig = require('../../config/app.config');

Page({
  data: {
    // 用户数据
    points: 0,
    unreadCount: 0,
    // 轮播图
    banners: [],
    currentBanner: 0,
    // 功能入口（8个）
    entries: [
      { id: 'travel', name: '旅游', icon: '/assets/icons/entry/travel.png', type: 'travel' },
      { id: 'car', name: '租车', icon: '/assets/icons/entry/car.png', type: 'coming' },
      { id: 'hotel', name: '民宿', icon: '/assets/icons/entry/hotel.png', type: 'coming' },
      { id: 'ticket', name: '门票', icon: '/assets/icons/entry/ticket.png', type: 'coming' },
      { id: 'transfer', name: '接送', icon: '/assets/icons/entry/transfer.png', type: 'coming' },
      { id: 'food', name: '美食', icon: '/assets/icons/entry/food.png', type: 'coming' },
      { id: 'rent', name: '租赁服务', icon: '/assets/icons/entry/rent.png', type: 'coming' },
      { id: 'insurance', name: '保险', icon: '/assets/icons/entry/insurance.png', type: 'coming' },
    ],
    // 热门线路 - 瀑布流双列
    hotRoutes: [],
    leftRoutes: [],
    rightRoutes: [],
    // 加载状态
    loading: true,
  },

  onLoad() {
    this.loadHomeData();
  },

  onShow() {
    // 设置TabBar选中状态
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 0 });
    }
    // 刷新未读消息数
    this.loadUnreadCount();
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
    // TODO: 调用消息接口获取未读数
  },

  /**
   * 轮播切换
   */
  handleBannerChange(e) {
    this.setData({ currentBanner: e.detail.current });
  },

  /**
   * 搜索点击
   */
  handleSearchTap() {
    wx.navigateTo({ url: '/pages/route/list/index' });
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
