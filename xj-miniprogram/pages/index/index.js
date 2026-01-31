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
    // 功能入口（8个）
    entries: [
      { id: 'travel', name: '旅游定制', icon: '/assets/icons/entry/travel.png', type: 'travel', disabled: false },
      // { id: 'custom', name: '', icon: '/assets/icons/entry/custom.png', type: 'custom', disabled: false },
      { id: 'car', name: '租车', icon: '/assets/icons/entry/car.png', type: 'coming', disabled: true, tag: '敬请期待' },
      { id: 'hotel', name: '民宿', icon: '/assets/icons/entry/hotel.png', type: 'coming', disabled: true, tag: '敬请期待' },
      { id: 'ticket', name: '门票', icon: '/assets/icons/entry/ticket.png', type: 'coming', disabled: true, tag: '敬请期待' },
      { id: 'transfer', name: '接送', icon: '/assets/icons/entry/transfer.png', type: 'coming', disabled: true, tag: '敬请期待' },
      { id: 'food', name: '美食', icon: '/assets/icons/entry/food.png', type: 'coming', disabled: true, tag: '敬请期待' },
      { id: 'rent', name: '租赁', icon: '/assets/icons/entry/rent.png', type: 'coming', disabled: true, tag: '敬请期待' },
    ],
    // 热门线路
    hotRoutes: [],
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
      this.setData({
        banners: data.banners || [],
        hotRoutes: data.recommendRoutes || [],
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
    // const res = await messageApi.getUnreadCount();
    // this.setData({ unreadCount: res.count || 0 });
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

    // linkType: 0无跳转 1线路详情 2外部链接 3小程序页面
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
    if (!item || item.disabled) {
      if (item.tag) {
        wx.showToast({ title: item.tag, icon: 'none' });
      }
      return;
    }

    switch (item.type) {
      case 'travel':
        // 旅游定制 - 跳转旅游页面
        wx.navigateTo({ url: '/pages/travel/index/index' });
        break;
      case 'route':
        // 线路列表
        go.routeList();
        break;
      case 'custom':
        // 定制游页面
        wx.navigateTo({ url: '/pages/custom/index/index' });
        break;
      default:
        break;
    }
  },

  /**
   * 线路点击
   */
  handleRouteTap(e) {
    const { route } = e.detail;
    if (route && route.id) {
      go.routeDetail(route.id);
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
    // 跳转企业微信客服
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
      // 拨打电话
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
