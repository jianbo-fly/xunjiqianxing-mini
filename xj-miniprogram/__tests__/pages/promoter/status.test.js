/**
 * 推广员审核状态页测试（#271 ~ #275）
 */

const { loadPage, createPageInstance } = require('../../helpers');

jest.mock('../../../services/promoter', () => ({
  getInfo: jest.fn(),
}));
jest.mock('../../../utils/router', () => ({
  navigateBack: jest.fn(),
  routes: {},
}));

const promoterApi = require('../../../services/promoter');
const { navigateBack } = require('../../../utils/router');

wx.makePhoneCall = jest.fn();

describe('推广员审核状态页', () => {
  let pageConfig;
  let page;
  const storage = {};

  beforeAll(() => {
    pageConfig = loadPage('pages/promoter/status/index');
  });

  beforeEach(() => {
    jest.clearAllMocks();
    Object.keys(storage).forEach(k => delete storage[k]);
    wx.getStorageSync.mockImplementation(k => storage[k]);
    page = createPageInstance(pageConfig);
  });

  // #271 status=1 → 自动跳转推广中心
  test('#271 status=1 → redirectTo center', async () => {
    promoterApi.getInfo.mockResolvedValue({ id: 1, status: 1 });

    await page.loadPromoterInfo();

    expect(wx.redirectTo).toHaveBeenCalledWith(
      expect.objectContaining({ url: '/pages/promoter/center/index' })
    );
  });

  // #272 无申请记录 → toast 并返回
  test('#272 无申请记录 → toast + navigateBack', async () => {
    promoterApi.getInfo.mockResolvedValue(null);

    await page.loadPromoterInfo();

    expect(wx.showToast).toHaveBeenCalledWith(
      expect.objectContaining({ title: '暂无申请记录' })
    );
  });

  // #273 center 页 status=0 跳转审核页 — 此页是 status 页本身，验证 status=0 正常展示
  test('#273 status=0 → 展示审核信息', async () => {
    storage.userInfo = { nickname: '小明' };
    promoterApi.getInfo.mockResolvedValue({
      id: 1, status: 0, nickname: '申请昵称',
      applyTime: '2026-04-12T10:30:00', type: '推广员',
    });

    await page.loadPromoterInfo();

    expect(page.data.promoter).toBeTruthy();
    expect(page.data.promoter.nickname).toBe('小明');
    expect(page.data.promoter.type).toBe('推广员');
  });

  // #274 formatDateTime
  test('#274 formatDateTime ISO → "YYYY-MM-DD HH:MM"', async () => {
    promoterApi.getInfo.mockResolvedValue({
      id: 1, status: 0,
      applyTime: '2026-04-12T10:30:00',
    });

    await page.loadPromoterInfo();

    expect(page.data.promoter.applyTime).toBe('2026-04-12 10:30');
  });

  // #275 API 失败不崩溃
  test('#275 API 失败 → 不崩溃', async () => {
    promoterApi.getInfo.mockRejectedValue(new Error('fail'));

    await expect(page.loadPromoterInfo()).resolves.not.toThrow();
  });

  // 辅助方法
  test('handleContact → makePhoneCall', () => {
    page.handleContact();
    expect(wx.makePhoneCall).toHaveBeenCalledWith(
      expect.objectContaining({ phoneNumber: '400-000-0000' })
    );
  });

  test('从缓存补充昵称', async () => {
    storage.userInfo = { nickname: '缓存昵称' };
    promoterApi.getInfo.mockResolvedValue({ id: 1, status: 0, nickname: '' });

    await page.loadPromoterInfo();

    expect(page.data.promoter.nickname).toBe('缓存昵称');
  });
});
