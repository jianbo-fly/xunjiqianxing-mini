/**
 * 首页测试（#318 ~ #332）
 */

const { loadPage, createPageInstance } = require('../helpers');

jest.mock('../../services/home', () => ({
  getData: jest.fn(),
}));
jest.mock('../../services/promoter', () => ({
  bind: jest.fn(),
}));
jest.mock('../../services/message', () => ({
  getUnreadCount: jest.fn(),
}));
jest.mock('../../utils/auth', () => ({
  isLogin: jest.fn(() => false),
}));
jest.mock('../../utils/router', () => ({
  go: {
    routeDetail: jest.fn(),
    routeList: jest.fn(),
    webview: jest.fn(),
  },
  routes: {},
}));
jest.mock('../../config/cities', () => ({
  HOT_CITIES: [],
  CITY_GROUPS: [{ letter: 'A', cities: [] }],
}), { virtual: true });
jest.mock('../../config/app.config', () => ({
  features: {
    customerService: { type: 'phone', phone: '10086', corpId: '' },
  },
  share: { defaultTitle: '寻迹千行', defaultImage: 'share.jpg' },
}), { virtual: true });

const homeApi = require('../../services/home');
const promoterApi = require('../../services/promoter');
const messageApi = require('../../services/message');
const auth = require('../../utils/auth');
const { go } = require('../../utils/router');

describe('首页', () => {
  let pageConfig;
  let page;
  const storage = {};

  beforeAll(() => {
    wx.getSystemInfoSync.mockReturnValue({
      statusBarHeight: 44, windowWidth: 375,
    });
    wx.getMenuButtonBoundingClientRect.mockReturnValue({
      left: 280, right: 355, bottom: 80, top: 48, width: 75,
    });
    pageConfig = loadPage('pages/index/index');
  });

  beforeEach(() => {
    jest.clearAllMocks();
    Object.keys(storage).forEach(k => delete storage[k]);
    wx.getStorageSync.mockImplementation(k => storage[k]);
    wx.setStorageSync.mockImplementation((k, v) => { storage[k] = v; });
    wx.removeStorageSync.mockImplementation(k => { delete storage[k]; });
    auth.isLogin.mockReturnValue(false);
    page = createPageInstance(pageConfig);
  });

  // #318 瀑布流左右分栏
  test('#318 loadHomeData 瀑布流左右分栏', async () => {
    homeApi.getData.mockResolvedValue({
      banners: [{ id: 1 }],
      recommendRoutes: [
        { id: 1, minPrice: 100 },
        { id: 2, minPrice: 200 },
        { id: 3, minPrice: 300 },
        { id: 4, minPrice: 400 },
      ],
    });

    await page.loadHomeData();

    expect(page.data.leftRoutes).toHaveLength(2);
    expect(page.data.rightRoutes).toHaveLength(2);
    expect(page.data.leftRoutes[0].id).toBe(1);
    expect(page.data.rightRoutes[0].id).toBe(2);
  });

  // #319 tryBindPromoter 有 pendingPromoCode
  test('#319 有 pendingPromoCode → 静默绑定', () => {
    auth.isLogin.mockReturnValue(true);
    storage.pendingPromoCode = 'ABC123';
    promoterApi.bind.mockResolvedValue({});

    page.tryBindPromoter();

    expect(promoterApi.bind).toHaveBeenCalledWith({ promoCode: 'ABC123' });
    expect(storage.pendingPromoCode).toBeUndefined();
  });

  // #320 已绑定失败静默忽略
  test('#320 bind 失败 → 静默忽略', async () => {
    auth.isLogin.mockReturnValue(true);
    storage.pendingPromoCode = 'ABC123';
    promoterApi.bind.mockRejectedValue(new Error('已绑定'));

    expect(() => page.tryBindPromoter()).not.toThrow();
  });

  // #321 未登录不绑定
  test('#321 未登录 → 不调用 bind', () => {
    auth.isLogin.mockReturnValue(false);
    storage.pendingPromoCode = 'ABC';

    page.tryBindPromoter();

    expect(promoterApi.bind).not.toHaveBeenCalled();
  });

  // #322 城市历史去重 5 条
  test('#322 handleCitySelect 去重并最多保留 5 条', () => {
    storage.departureCityHistory = ['A', 'B', 'C', 'D', 'E'];

    page.handleCitySelect({ currentTarget: { dataset: { city: 'C' } } });

    expect(page.data.cityHistory).toEqual(['C', 'A', 'B', 'D', 'E']);
  });

  test('#322-2 超过 5 条 → 淘汰最旧', () => {
    storage.departureCityHistory = ['A', 'B', 'C', 'D', 'E'];

    page.handleCitySelect({ currentTarget: { dataset: { city: 'F' } } });

    expect(page.data.cityHistory).toEqual(['F', 'A', 'B', 'C', 'D']);
  });

  // #323 切换城市
  test('#323 handleCitySelect 更新 departureCity', () => {
    page.handleCitySelect({ currentTarget: { dataset: { city: '北京' } } });
    expect(page.data.departureCity).toBe('北京');
    expect(page.data.showCityPicker).toBe(false);
    expect(storage.departureCity).toBe('北京');
  });

  // #324 Banner linkType=1
  test('#324 handleBannerTap linkType=1 → 跳路线详情', () => {
    page.handleBannerTap({
      currentTarget: { dataset: { item: { linkType: 1, linkValue: 10 } } },
    });
    expect(go.routeDetail).toHaveBeenCalledWith(10);
  });

  // #325 Banner linkType=2
  test('#325 handleBannerTap linkType=2 → webview', () => {
    page.handleBannerTap({
      currentTarget: { dataset: { item: { linkType: 2, linkValue: 'https://x' } } },
    });
    expect(go.webview).toHaveBeenCalledWith('https://x');
  });

  // #326 Banner linkType=3
  test('#326 handleBannerTap linkType=3 → navigateTo', () => {
    page.handleBannerTap({
      currentTarget: { dataset: { item: { linkType: 3, linkValue: '/pages/x/x' } } },
    });
    expect(wx.navigateTo).toHaveBeenCalledWith(
      expect.objectContaining({ url: '/pages/x/x' })
    );
  });

  // #327 onPageScroll 视差效果
  test('#327 onPageScroll 滚动超阈值 → bannerVisible=false', () => {
    page.onPageScroll({ scrollTop: 500 });
    expect(page.data.bannerVisible).toBe(false);
    expect(page.data.bannerOpacity).toBeLessThan(1);
  });

  test('#327-2 onPageScroll 顶部 → opacity=1', () => {
    page.onPageScroll({ scrollTop: 0 });
    expect(page.data.bannerOpacity).toBe(1);
    expect(page.data.bannerVisible).toBe(true);
  });

  // #328 未读消息数
  test('#328 onShow 刷新未读数', async () => {
    auth.isLogin.mockReturnValue(true);
    messageApi.getUnreadCount.mockResolvedValue(5);

    await page.loadUnreadCount();

    expect(page.data.unreadCount).toBe(5);
  });

  test('#328-2 未登录 → 不调用', async () => {
    auth.isLogin.mockReturnValue(false);
    await page.loadUnreadCount();
    expect(messageApi.getUnreadCount).not.toHaveBeenCalled();
  });

  // #329 价格展示
  test('#329 originalPrice > minPrice → hasOriginal=true', async () => {
    homeApi.getData.mockResolvedValue({
      recommendRoutes: [{ id: 1, minPrice: 100, originalPrice: 200 }],
    });

    await page.loadHomeData();

    expect(page.data.hotRoutes[0].hasOriginal).toBe(true);
  });

  test('#329-2 无原价 → hasOriginal=false', async () => {
    homeApi.getData.mockResolvedValue({
      recommendRoutes: [{ id: 1, minPrice: 100 }],
    });

    await page.loadHomeData();

    expect(page.data.hotRoutes[0].hasOriginal).toBe(false);
  });

  // #330 loadHomeData 失败
  test('#330 loadHomeData 失败 → 不崩溃且 loading=false', async () => {
    homeApi.getData.mockRejectedValue(new Error('fail'));

    await page.loadHomeData();

    expect(page.data.loading).toBe(false);
  });

  // #331 banner 为空
  test('#331 banners 为空 → 不崩溃', async () => {
    homeApi.getData.mockResolvedValue({ banners: [], recommendRoutes: [] });

    await page.loadHomeData();

    expect(page.data.banners).toEqual([]);
  });

  // #332 推荐路线为空
  test('#332 推荐路线为空 → 双列均为空', async () => {
    homeApi.getData.mockResolvedValue({ recommendRoutes: [] });

    await page.loadHomeData();

    expect(page.data.leftRoutes).toEqual([]);
    expect(page.data.rightRoutes).toEqual([]);
  });

  // 其他辅助
  describe('辅助方法', () => {
    test('handleSearchTap 跳转搜索页', () => {
      page.setData({ departureCity: '北京' });
      page.handleSearchTap();
      expect(wx.navigateTo).toHaveBeenCalledWith(
        expect.objectContaining({
          url: expect.stringContaining('departureCity='),
        })
      );
    });

    test('handleEntryTap type=travel → 跳转', () => {
      page.handleEntryTap({ currentTarget: { dataset: { item: { type: 'travel' } } } });
      expect(wx.navigateTo).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/pages/travel/index/index' })
      );
    });

    test('handleEntryTap type=coming → toast', () => {
      page.handleEntryTap({ currentTarget: { dataset: { item: { type: 'coming' } } } });
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '敬请期待' })
      );
    });

    test('handleRouteTap 跳转详情', () => {
      page.handleRouteTap({ currentTarget: { dataset: { id: 10 } } });
      expect(go.routeDetail).toHaveBeenCalledWith(10);
    });

    test('handleClearCityHistory 清空历史', () => {
      page.setData({ cityHistory: ['A', 'B'] });
      page.handleClearCityHistory();
      expect(page.data.cityHistory).toEqual([]);
    });

    test('onShareAppMessage 返回分享配置', () => {
      const res = page.onShareAppMessage();
      expect(res.title).toBe('寻迹千行');
    });
  });
});
