/**
 * 推广中心页测试（#222 ~ #235）
 */

const { loadPage, createPageInstance } = require('../../helpers');

jest.mock('../../../services/promoter', () => ({
  getInfo: jest.fn(),
  getStatistics: jest.fn(),
  downloadQrCode: jest.fn(),
}));
jest.mock('../../../utils/router', () => ({
  go: { promoterApply: jest.fn(), promoterBindList: jest.fn() },
  navigateBack: jest.fn(),
  routes: {},
}));

const promoterApi = require('../../../services/promoter');
const { go, navigateBack } = require('../../../utils/router');

wx.getWindowInfo = jest.fn(() => ({ statusBarHeight: 44 }));
wx.getDeviceInfo = jest.fn(() => ({ platform: 'ios' }));
wx.saveImageToPhotosAlbum = jest.fn();
wx.openSetting = jest.fn();
wx.showShareMenu = jest.fn();

describe('推广中心页', () => {
  let pageConfig;
  let page;
  const storage = {};

  beforeAll(() => {
    pageConfig = loadPage('pages/promoter/center/index');
  });

  beforeEach(() => {
    jest.clearAllMocks();
    Object.keys(storage).forEach(k => delete storage[k]);
    wx.getStorageSync.mockImplementation(k => storage[k]);
    page = createPageInstance(pageConfig);
  });

  // #222 onLoad 计算导航栏高度
  test('#222 onLoad 计算 statusBarHeight / navBarHeight', () => {
    promoterApi.getInfo.mockResolvedValue(null);
    promoterApi.getStatistics.mockResolvedValue({});
    page.onLoad();
    expect(page.data.statusBarHeight).toBe(44);
    expect(page.data.navBarHeight).toBe(44); // ios
  });

  test('#222-2 android 平台 navBarHeight=48', () => {
    wx.getDeviceInfo.mockReturnValue({ platform: 'android' });
    promoterApi.getInfo.mockResolvedValue(null);
    promoterApi.getStatistics.mockResolvedValue({});
    page.onLoad();
    expect(page.data.navBarHeight).toBe(48);
  });

  // #223 已申请推广员
  test('#223 loadPromoterInfo 已审核通过加载完整数据', async () => {
    promoterApi.getInfo.mockResolvedValue({ id: 1, status: 1, promoCode: 'ABC' });
    promoterApi.getStatistics.mockResolvedValue({
      scanCount: 10, orderCount: 3, availableCommission: 100,
    });
    promoterApi.downloadQrCode.mockResolvedValue({ dataUri: 'data:image/png;base64,xxx', filePath: '/tmp/a.png' });
    storage.userInfo = { nickname: '小明', avatar: 'a.jpg' };

    await page.loadPromoterInfo();

    expect(page.data.promoter.scanCount).toBe(10);
    expect(page.data.promoter.orderCount).toBe(3);
    expect(page.data.promoter.points).toBe(100);
    expect(page.data.promoter.nickname).toBe('小明');
    expect(page.data.loading).toBe(false);
  });

  // #224 未申请 → 跳转申请页
  test('#224 未申请推广员 → redirectTo 申请页', async () => {
    promoterApi.getInfo.mockResolvedValue(null);
    promoterApi.getStatistics.mockResolvedValue({});

    await page.loadPromoterInfo();

    expect(wx.redirectTo).toHaveBeenCalledWith(
      expect.objectContaining({ url: '/pages/promoter/apply/index' })
    );
  });

  // #225 从缓存补充昵称/头像
  test('#225 从缓存补充 nickname/avatar', async () => {
    promoterApi.getInfo.mockResolvedValue({ id: 1, status: 1 });
    promoterApi.getStatistics.mockResolvedValue({});
    promoterApi.downloadQrCode.mockResolvedValue({ dataUri: 'x', filePath: '' });
    storage.userInfo = { nickname: '缓存昵称', avatar: 'cache.jpg' };

    await page.loadPromoterInfo();

    expect(page.data.promoter.nickname).toBe('缓存昵称');
    expect(page.data.promoter.avatar).toBe('cache.jpg');
  });

  // #226 API 失败
  test('#226 API 失败 → toast 并关闭 loading', async () => {
    promoterApi.getInfo.mockRejectedValue(new Error('fail'));
    promoterApi.getStatistics.mockRejectedValue(new Error('fail'));

    await page.loadPromoterInfo();

    expect(wx.showToast).toHaveBeenCalledWith(
      expect.objectContaining({ title: '加载失败' })
    );
    expect(page.data.loading).toBe(false);
  });

  // #227 status=1 自动加载二维码
  test('#227 status=1 → 调用 loadQrCode', async () => {
    promoterApi.getInfo.mockResolvedValue({ id: 1, status: 1 });
    promoterApi.getStatistics.mockResolvedValue({});
    promoterApi.downloadQrCode.mockResolvedValue({ dataUri: 'data:xxx', filePath: '/p' });

    await page.loadPromoterInfo();
    // Wait microtasks
    await new Promise(setImmediate);

    expect(promoterApi.downloadQrCode).toHaveBeenCalled();
  });

  // #228 status=0 → 跳转状态页
  test('#228 status=0 → redirectTo status 页', async () => {
    promoterApi.getInfo.mockResolvedValue({ id: 1, status: 0 });
    promoterApi.getStatistics.mockResolvedValue({});

    await page.loadPromoterInfo();

    expect(wx.redirectTo).toHaveBeenCalledWith(
      expect.objectContaining({ url: '/pages/promoter/status/index' })
    );
    expect(promoterApi.downloadQrCode).not.toHaveBeenCalled();
  });

  // #229 loadQrCode 成功
  test('#229 loadQrCode 成功设置 qrCodeUrl', async () => {
    page.setData({ promoter: {} });
    promoterApi.downloadQrCode.mockResolvedValue({
      dataUri: 'data:image/png;base64,X', filePath: '/tmp/a.png',
    });

    await page.loadQrCode();

    // helpers.setData 使用 Object.assign，对 'a.b' 字符串 key 原样存入，这里检查是否被设置
    expect(page.data['promoter.qrCodeUrl']).toContain('data:image');
    expect(page.data['promoter.qrFilePath']).toBe('/tmp/a.png');
  });

  // #230 loadQrCode 失败
  test('#230 loadQrCode 失败 → 不崩溃', async () => {
    page.setData({ promoter: {} });
    promoterApi.downloadQrCode.mockRejectedValue(new Error('fail'));

    await expect(page.loadQrCode()).resolves.not.toThrow();
    expect(page.data.promoter.qrCodeUrl).toBeUndefined();
  });

  // #231 handleSaveImage 成功
  test('#231 handleSaveImage 成功', () => {
    wx.saveImageToPhotosAlbum.mockImplementation(({ success }) => success && success());
    page.setData({ promoter: { qrCodeUrl: 'data:x', qrFilePath: '/tmp/a.png' } });

    page.handleSaveImage();

    expect(wx.saveImageToPhotosAlbum).toHaveBeenCalled();
    expect(wx.showToast).toHaveBeenCalledWith(
      expect.objectContaining({ title: '已保存到相册' })
    );
  });

  // #232 二维码未生成
  test('#232 未生成二维码 → toast', () => {
    page.setData({ promoter: {} });
    page.handleSaveImage();
    expect(wx.showToast).toHaveBeenCalledWith(
      expect.objectContaining({ title: '推广码未生成' })
    );
  });

  // #233 权限被拒
  test('#233 权限被拒 → 弹窗引导授权', () => {
    wx.saveImageToPhotosAlbum.mockImplementation(({ fail }) =>
      fail && fail({ errMsg: 'saveImageToPhotosAlbum:fail auth deny' })
    );
    wx.showModal.mockImplementation(({ success }) => success({ confirm: true }));
    page.setData({ promoter: { qrCodeUrl: 'x', qrFilePath: 'p' } });

    page.handleSaveImage();

    expect(wx.showModal).toHaveBeenCalled();
    expect(wx.openSetting).toHaveBeenCalled();
  });

  // #234 onShareAppMessage 含 promoterCode
  test('#234 onShareAppMessage 含 promoCode', () => {
    page.setData({ promoter: { nickname: '小明', promoCode: 'ABC123' } });
    const res = page.onShareAppMessage();
    expect(res.title).toContain('小明');
    expect(res.path).toContain('promoterCode=ABC123');
  });

  // #235 promoter 为 null
  test('#235 promoter 为 null → 使用默认值', () => {
    page.setData({ promoter: null });
    const res = page.onShareAppMessage();
    expect(res.title).toContain('好友');
    expect(res.path).toContain('promoterCode=');
  });

  // 辅助方法
  test('handleBack → navigateBack', () => {
    page.handleBack();
    expect(navigateBack).toHaveBeenCalled();
  });

  test('handleEditProfile → go.promoterApply', () => {
    page.handleEditProfile();
    expect(go.promoterApply).toHaveBeenCalled();
  });

  test('handlePromoterDetail → go.promoterBindList', () => {
    page.handlePromoterDetail();
    expect(go.promoterBindList).toHaveBeenCalled();
  });
});
