/**
 * 会员中心页测试（#379 ~ #388）
 */

const { loadPage, createPageInstance } = require('../../helpers');

jest.mock('../../../services/user', () => ({
  getInfo: jest.fn(),
}));
jest.mock('../../../services/order', () => ({
  getCounts: jest.fn(),
}));
jest.mock('../../../services/custom', () => ({
  getCounts: jest.fn(),
}));
jest.mock('../../../utils/auth', () => ({
  isLogin: jest.fn(() => false),
  checkLogin: jest.fn(() => true),
  clearAll: jest.fn(),
}));
jest.mock('../../../utils/router', () => ({
  go: {
    profile: jest.fn(),
    settings: jest.fn(),
    promoterCenter: jest.fn(),
    promoterApply: jest.fn(),
    orderList: jest.fn(),
  },
  routes: {},
}));
jest.mock('../../../assets/index', () => ({}), { virtual: true });
jest.mock('../../../config/app.config', () => ({
  share: { defaultTitle: '寻迹千行' },
}), { virtual: true });

const userApi = require('../../../services/user');
const orderApi = require('../../../services/order');
const customApi = require('../../../services/custom');
const auth = require('../../../utils/auth');
const { go } = require('../../../utils/router');

// 补齐 wx API
wx.getWindowInfo = jest.fn(() => ({ statusBarHeight: 44 }));
wx.scanCode = jest.fn();
wx.makePhoneCall = jest.fn();

describe('会员中心页', () => {
  let pageConfig;
  let page;

  beforeAll(() => {
    pageConfig = loadPage('pages/member/index/index');
  });

  beforeEach(() => {
    jest.clearAllMocks();
    auth.isLogin.mockReturnValue(false);
    auth.checkLogin.mockReturnValue(true);
    page = createPageInstance(pageConfig);
  });

  // #379 登录状态检查
  describe('#379 登录状态检查', () => {
    test('未登录 → isLogin=false，不加载数据', () => {
      auth.isLogin.mockReturnValue(false);
      page.onLoad();
      expect(page.data.isLogin).toBe(false);
      expect(userApi.getInfo).not.toHaveBeenCalled();
    });

    test('已登录 → 加载用户/订单/定制数据', async () => {
      auth.isLogin.mockReturnValue(true);
      userApi.getInfo.mockResolvedValue({ nickname: '张三' });
      orderApi.getCounts.mockResolvedValue({ pending: 2, confirming: 1, travelling: 0 });
      customApi.getCounts.mockResolvedValue({ pending: 1, following: 0, completed: 3 });

      page.onLoad();
      await new Promise(setImmediate);

      expect(page.data.isLogin).toBe(true);
      expect(userApi.getInfo).toHaveBeenCalled();
      expect(orderApi.getCounts).toHaveBeenCalled();
      expect(customApi.getCounts).toHaveBeenCalled();
    });
  });

  // #380 用户信息展示
  test('#380 loadUserInfo 成功设置 userInfo', async () => {
    userApi.getInfo.mockResolvedValue({ nickname: '李四', avatar: 'a.jpg' });
    await page.loadUserInfo();
    expect(page.data.userInfo.nickname).toBe('李四');
  });

  // #381/382 handlePromoterTap
  describe('handlePromoterTap', () => {
    test('#381 已是推广员 → 跳转推广中心', () => {
      page.setData({ userInfo: { isPromoter: true } });
      page.handlePromoterTap();
      expect(go.promoterCenter).toHaveBeenCalled();
      expect(go.promoterApply).not.toHaveBeenCalled();
    });

    test('#382 非推广员 → 跳转申请页', () => {
      page.setData({ userInfo: { isPromoter: false } });
      page.handlePromoterTap();
      expect(go.promoterApply).toHaveBeenCalled();
    });

    test('未登录 → 被 checkLogin 拦截', () => {
      auth.checkLogin.mockReturnValue(false);
      page.handlePromoterTap();
      expect(go.promoterCenter).not.toHaveBeenCalled();
      expect(go.promoterApply).not.toHaveBeenCalled();
    });
  });

  // #383/384 handleLogout
  describe('handleLogout', () => {
    test('#383 展示确认弹窗', () => {
      page.handleLogout();
      expect(wx.showModal).toHaveBeenCalledWith(
        expect.objectContaining({ title: '退出登录' })
      );
    });

    test('#384 确认后清除所有 auth 数据', () => {
      wx.showModal.mockImplementation(({ success }) => success({ confirm: true }));
      page.setData({ isLogin: true, userInfo: { nickname: 'X' } });

      page.handleLogout();

      expect(auth.clearAll).toHaveBeenCalled();
      expect(page.data.isLogin).toBe(false);
      expect(page.data.userInfo).toBe(null);
      expect(page.data.orderCounts.pending).toBe(0);
    });

    test('点击取消 → 不执行清理', () => {
      wx.showModal.mockImplementation(({ success }) => success({ confirm: false }));
      page.handleLogout();
      expect(auth.clearAll).not.toHaveBeenCalled();
    });
  });

  // #385/386 API 不存在导致静默失败（bug 已修复？至少不应 crash）
  describe('#385/386 getCounts API 异常不崩溃', () => {
    test('orderApi.getCounts 失败 → 静默失败', async () => {
      orderApi.getCounts.mockRejectedValue(new Error('not a function'));
      await page.loadOrderCounts();
      expect(page.data.orderCounts.pending).toBe(0);
    });

    test('customApi.getCounts 失败 → 静默失败', async () => {
      customApi.getCounts.mockRejectedValue(new Error('not a function'));
      await page.loadCustomCounts();
      expect(page.data.customCounts.pending).toBe(0);
    });
  });

  // #387 待上线功能
  describe('#387 待上线功能提示', () => {
    test('handleMemberTap 已登录 → 跳转 VIP 页', () => {
      page.handleMemberTap();
      expect(wx.navigateTo).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/pages/member/vip/index' })
      );
    });

    test('handleLeaderTap → toast 即将上线', () => {
      page.handleLeaderTap();
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '领队功能即将上线' })
      );
    });

    test('handleCouponTap → toast 即将上线', () => {
      page.handleCouponTap();
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '优惠券功能即将上线' })
      );
    });
  });

  // #388 订单入口按状态跳转
  describe('#388 handleOrderTap', () => {
    test('携带 status 调用 go.orderList', () => {
      page.handleOrderTap({ currentTarget: { dataset: { status: 1 } } });
      expect(go.orderList).toHaveBeenCalledWith(1);
    });

    test('未登录 → 被 checkLogin 拦截', () => {
      auth.checkLogin.mockReturnValue(false);
      page.handleOrderTap({ currentTarget: { dataset: { status: 1 } } });
      expect(go.orderList).not.toHaveBeenCalled();
    });
  });

  // 其他辅助
  describe('辅助方法', () => {
    test('handleAvatarTap 已登录 → 跳转 profile', () => {
      page.setData({ isLogin: true });
      page.handleAvatarTap();
      expect(go.profile).toHaveBeenCalled();
    });

    test('handleAvatarTap 未登录 → 跳转登录页', () => {
      page.setData({ isLogin: false });
      page.handleAvatarTap();
      expect(wx.navigateTo).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/pages/login/index' })
      );
    });

    test('onShareAppMessage 返回分享配置', () => {
      const res = page.onShareAppMessage();
      expect(res.title).toBe('寻迹千行');
      expect(res.path).toBe('/pages/index/index');
    });
  });
});
