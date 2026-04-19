/**
 * 推广员申请页测试（#268 ~ #270）
 */

const { loadPage, createPageInstance } = require('../../helpers');

jest.mock('../../../services/promoter', () => ({
  getInfo: jest.fn(),
  apply: jest.fn(),
}));
jest.mock('../../../utils/router', () => ({
  navigateBack: jest.fn(),
  redirectTo: jest.fn(),
  routes: { promoterStatus: '/pages/promoter/status/index' },
}));

const promoterApi = require('../../../services/promoter');
const { navigateBack, redirectTo } = require('../../../utils/router');

wx.getWindowInfo = jest.fn(() => ({ statusBarHeight: 44 }));
wx.getDeviceInfo = jest.fn(() => ({ platform: 'ios' }));

const mockApp = {
  globalData: { userInfo: null },
};
global.getApp = jest.fn(() => mockApp);

describe('推广员申请页', () => {
  let pageConfig;
  let page;
  const storage = {};

  beforeAll(() => {
    pageConfig = loadPage('pages/promoter/apply/index');
  });

  beforeEach(() => {
    jest.clearAllMocks();
    Object.keys(storage).forEach(k => delete storage[k]);
    wx.getStorageSync.mockImplementation(k => storage[k]);
    mockApp.globalData.userInfo = null;
    page = createPageInstance(pageConfig);
  });

  // #268 status=0 → 跳转审核页
  test('#268 checkExistingApplication status=0 → redirectTo status', async () => {
    promoterApi.getInfo.mockResolvedValue({ id: 1, status: 0 });

    await page.checkExistingApplication();

    expect(wx.redirectTo).toHaveBeenCalledWith(
      expect.objectContaining({ url: '/pages/promoter/status/index' })
    );
  });

  // #269 status=1 → 跳转推广中心
  test('#269 checkExistingApplication status=1 → redirectTo center', async () => {
    promoterApi.getInfo.mockResolvedValue({ id: 1, status: 1 });

    await page.checkExistingApplication();

    expect(wx.redirectTo).toHaveBeenCalledWith(
      expect.objectContaining({ url: '/pages/promoter/center/index' })
    );
  });

  // #270 无申请记录 → 展示表单，预填用户信息
  test('#270 无申请记录 → 预填用户信息', async () => {
    promoterApi.getInfo.mockRejectedValue(new Error('not found'));
    mockApp.globalData.userInfo = { nickname: '小明', avatar: 'a.jpg' };

    await page.checkExistingApplication();

    expect(page.data['form.nickname']).toBe('小明');
    expect(page.data['form.avatarUrl']).toBe('a.jpg');
  });

  test('#270-2 昵称为"微信用户" → 不预填', async () => {
    promoterApi.getInfo.mockRejectedValue(new Error('not found'));
    mockApp.globalData.userInfo = { nickname: '微信用户', avatar: 'a.jpg' };

    await page.checkExistingApplication();

    expect(page.data['form.nickname']).toBeUndefined();
  });

  // 提交申请
  describe('handleSubmit', () => {
    test('未勾选协议 → toast', async () => {
      page.setData({ agreed: false });
      await page.handleSubmit();
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '请先阅读并同意推广员协议' })
      );
      expect(promoterApi.apply).not.toHaveBeenCalled();
    });

    test('昵称为空 → toast', async () => {
      page.setData({ agreed: true, form: { nickname: '  ', avatarUrl: '' } });
      await page.handleSubmit();
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '请输入昵称' })
      );
    });

    test('正常提交 → 调用 apply 并跳转', async () => {
      promoterApi.apply.mockResolvedValue({});
      page.setData({ agreed: true, form: { nickname: '小明', avatarUrl: 'a.jpg' } });

      await page.handleSubmit();

      expect(promoterApi.apply).toHaveBeenCalledWith({
        nickname: '小明',
        avatarUrl: 'a.jpg',
      });
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '申请已提交' })
      );
    });

    test('提交失败 → toast 错误', async () => {
      promoterApi.apply.mockRejectedValue(new Error('网络错误'));
      page.setData({ agreed: true, form: { nickname: '小明', avatarUrl: '' } });

      await page.handleSubmit();

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '网络错误' })
      );
      expect(page.data.submitting).toBe(false);
    });

    test('防重复提交', async () => {
      page.setData({ submitting: true, agreed: true, form: { nickname: 'X' } });
      await page.handleSubmit();
      expect(promoterApi.apply).not.toHaveBeenCalled();
    });
  });

  // 辅助方法
  test('handleToggleAgree 切换协议勾选', () => {
    page.setData({ agreed: false });
    page.handleToggleAgree();
    expect(page.data.agreed).toBe(true);
    page.handleToggleAgree();
    expect(page.data.agreed).toBe(false);
  });

  test('handleBack → navigateBack', () => {
    page.handleBack();
    expect(navigateBack).toHaveBeenCalled();
  });

  test('handleChooseAvatar 设置头像', () => {
    page.handleChooseAvatar({ detail: { avatarUrl: 'new.jpg' } });
    expect(page.data['form.avatarUrl']).toBe('new.jpg');
  });

  test('handleChooseAvatar 空 → 不设置', () => {
    page.handleChooseAvatar({ detail: {} });
    expect(page.data['form.avatarUrl']).toBeUndefined();
  });

  test('handleInput 更新表单字段', () => {
    page.handleInput({ currentTarget: { dataset: { field: 'nickname' } }, detail: { value: '新昵称' } });
    expect(page.data['form.nickname']).toBe('新昵称');
  });
});
