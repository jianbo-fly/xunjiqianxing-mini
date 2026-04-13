/**
 * 登录页面测试（#298 ~ #317）
 */

const { loadPage, createPageInstance } = require('../../helpers');

// Mock 依赖
jest.mock('../../../services/user', () => ({
  wxLogin: jest.fn(),
  phoneLogin: jest.fn(),
  bindPhone: jest.fn(),
  sendVerifyCode: jest.fn(),
  getInfo: jest.fn(),
}));

jest.mock('../../../utils/auth', () => ({
  redirectAfterLogin: jest.fn(),
}));

const userApi = require('../../../services/user');
const { redirectAfterLogin } = require('../../../utils/auth');

describe('登录页面', () => {
  let pageConfig;
  let page;
  let mockApp;

  beforeAll(() => {
    // getApp 必须在 loadPage 之前设置，因为页面顶部 const app = getApp()
    mockApp = {
      globalData: { userInfo: null, token: null },
      setLoginInfo: jest.fn(),
    };
    global.getApp = jest.fn().mockReturnValue(mockApp);

    pageConfig = loadPage('pages/login/index');
  });

  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();

    // 重置 mockApp 方法但保持同一引用
    mockApp.setLoginInfo = jest.fn();
    mockApp.globalData = { userInfo: null, token: null };

    page = createPageInstance(pageConfig);
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  // ==================== handleWxLogin #298-300 ====================

  describe('handleWxLogin - 微信一键登录', () => {
    test('#298 新用户（hasPhone=false）→ 进入手机绑定', async () => {
      page.setData({ agreed: true });

      wx.login.mockImplementation(({ success }) => success({ code: 'mock-code' }));
      userApi.wxLogin.mockResolvedValue({ token: 'pending-token', hasPhone: false });

      await page.handleWxLogin();

      expect(page.data.step).toBe(2);
      expect(page.data.pendingToken).toBe('pending-token');
      expect(page.data.fromPhoneEntry).toBe(false);
      // token 不应被持久化
      expect(mockApp.setLoginInfo).not.toHaveBeenCalled();
    });

    test('#299 老用户（hasPhone=true）→ 直接登录', async () => {
      page.setData({ agreed: true });

      wx.login.mockImplementation(({ success }) => success({ code: 'mock-code' }));
      userApi.wxLogin.mockResolvedValue({ token: 'real-token', hasPhone: true });
      userApi.getInfo.mockResolvedValue({ id: 1, nickname: '张三' });

      await page.handleWxLogin();

      expect(mockApp.setLoginInfo).toHaveBeenCalledWith('real-token');
    });

    test('#300 微信 API 失败', async () => {
      page.setData({ agreed: true });

      wx.login.mockImplementation(({ fail }) => fail(new Error('wx error')));

      await page.handleWxLogin();

      expect(page.data.logging).toBe(false);
      expect(mockApp.setLoginInfo).not.toHaveBeenCalled();
    });

    test('#313 未勾选协议时点击登录', async () => {
      page.setData({ agreed: false });

      await page.handleWxLogin();

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: expect.stringContaining('协议') })
      );
      expect(userApi.wxLogin).not.toHaveBeenCalled();
    });

    test('#317 并发点击登录按钮（防重复提交）', async () => {
      page.setData({ agreed: true, logging: true });

      await page.handleWxLogin();

      expect(userApi.wxLogin).not.toHaveBeenCalled();
    });
  });

  // ==================== handlePhoneLogin #301 ====================

  describe('handlePhoneLogin - 切换手机登录', () => {
    test('#301 切换到手机登录入口', () => {
      page.setData({ agreed: true });

      page.handlePhoneLogin();

      expect(page.data.step).toBe(2);
      expect(page.data.fromPhoneEntry).toBe(true);
    });

    test('#301-2 未勾选协议时不能切换', () => {
      page.setData({ agreed: false });

      page.handlePhoneLogin();

      expect(page.data.step).toBe(1);
      expect(wx.showToast).toHaveBeenCalled();
    });
  });

  // ==================== handleBindPhone #302-304 ====================

  describe('handleBindPhone - 手机号绑定/登录', () => {
    test('#303 手机路径：调用 phoneLogin API', async () => {
      page.setData({
        phone: '13800138000',
        verifyCode: '1234',
        fromPhoneEntry: true,
        pendingToken: '',
      });

      userApi.phoneLogin.mockResolvedValue({ token: 'phone-token' });
      userApi.getInfo.mockResolvedValue({ id: 1, nickname: '张三' });

      await page.handleBindPhone();

      expect(userApi.phoneLogin).toHaveBeenCalledWith({
        phone: '13800138000',
        code: '1234',
      });
      expect(mockApp.setLoginInfo).toHaveBeenCalledWith('phone-token');
    });

    test('#302 微信路径：调用 bindPhone + pendingToken', async () => {
      page.setData({
        phone: '13800138000',
        verifyCode: '1234',
        fromPhoneEntry: false,
        pendingToken: 'pending-token-123',
      });

      userApi.bindPhone.mockResolvedValue({});
      userApi.getInfo.mockResolvedValue({ id: 1 });

      await page.handleBindPhone();

      expect(userApi.bindPhone).toHaveBeenCalledWith(
        { phone: '13800138000', code: '1234' },
        { header: { Authorization: 'pending-token-123' } }
      );
      expect(mockApp.setLoginInfo).toHaveBeenCalledWith('pending-token-123');
      expect(page.data.pendingToken).toBe('');
    });

    test('#309 手机号为空时提交', async () => {
      page.setData({ phone: '', verifyCode: '1234' });

      await page.handleBindPhone();

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: expect.stringContaining('手机号') })
      );
    });

    test('#308 验证码为空时提交', async () => {
      page.setData({ phone: '13800138000', verifyCode: '' });

      await page.handleBindPhone();

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: expect.stringContaining('验证码') })
      );
    });

    test('#305 手机号格式校验', async () => {
      page.setData({ phone: '123', verifyCode: '1234' });

      await page.handleBindPhone();

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: expect.stringContaining('手机号') })
      );
      expect(userApi.phoneLogin).not.toHaveBeenCalled();
    });
  });

  // ==================== handleSendCode #305-307 ====================

  describe('handleSendCode - 发送验证码', () => {
    test('#306 60 秒倒计时', async () => {
      page.setData({ phone: '13800138000', countdown: 0 });
      userApi.sendVerifyCode.mockResolvedValue({});

      await page.handleSendCode();

      expect(page.data.countdown).toBe(60);
      expect(userApi.sendVerifyCode).toHaveBeenCalledWith({ phone: '13800138000' });
    });

    test('#307 倒计时期间重复点击被忽略', async () => {
      page.setData({ phone: '13800138000', countdown: 30 });

      await page.handleSendCode();

      expect(userApi.sendVerifyCode).not.toHaveBeenCalled();
    });

    test('#305 手机号不合法时不发送', async () => {
      page.setData({ phone: '12345', countdown: 0 });

      await page.handleSendCode();

      expect(userApi.sendVerifyCode).not.toHaveBeenCalled();
      expect(wx.showToast).toHaveBeenCalled();
    });
  });

  // ==================== 协议 #312-313 ====================

  describe('协议勾选', () => {
    test('#312 协议勾选状态切换', () => {
      expect(page.data.agreed).toBe(false);

      page.handleAgreeChange();
      expect(page.data.agreed).toBe(true);

      page.handleAgreeChange();
      expect(page.data.agreed).toBe(false);
    });
  });

  // ==================== loginSuccess #315-316 ====================

  describe('loginSuccess - 登录成功跳转', () => {
    test('#315 / #316 调用 redirectAfterLogin', () => {
      page.loginSuccess();

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '登录成功' })
      );
    });
  });

  // ==================== onLoad ====================

  describe('onLoad', () => {
    test('保存 redirect 参数', () => {
      page.onLoad({ redirect: encodeURIComponent('/pages/order/list/index') });

      expect(wx.setStorageSync).toHaveBeenCalledWith(
        'redirectUrl',
        '/pages/order/list/index'
      );
    });
  });

  // ==================== onUnload #310-311 ====================

  describe('onUnload - 清理', () => {
    test('#310 清理倒计时 timer', () => {
      page.countdownTimer = setInterval(() => {}, 1000);

      expect(() => page.onUnload()).not.toThrow();
    });

    test('#310-2 无 timer 时不报错', () => {
      expect(() => page.onUnload()).not.toThrow();
    });
  });
});
