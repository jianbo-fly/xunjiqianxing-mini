/**
 * 订单列表页测试（#184 ~ #221）
 */

const { loadPage, createPageInstance } = require('../../helpers');

jest.mock('../../../services/order', () => ({
  getList: jest.fn(),
  cancel: jest.fn(),
}));
jest.mock('../../../services/pay', () => ({
  createOrderPayment: jest.fn(),
  wxPay: jest.fn(),
}));
jest.mock('../../../utils/router', () => ({
  go: {
    orderDetail: jest.fn(),
    orderRefund: jest.fn(),
    routeDetail: jest.fn(),
    home: jest.fn(),
  },
  redirectTo: jest.fn(),
  routes: {},
}));
jest.mock('../../../utils/auth', () => ({
  checkLogin: jest.fn(() => true),
}));

const orderApi = require('../../../services/order');
const payApi = require('../../../services/pay');
const { go } = require('../../../utils/router');
const { checkLogin } = require('../../../utils/auth');

describe('订单列表页', () => {
  let pageConfig;
  let page;

  beforeAll(() => {
    pageConfig = loadPage('pages/order/list/index');
  });

  beforeEach(() => {
    jest.clearAllMocks();
    checkLogin.mockReturnValue(true);
    page = createPageInstance(pageConfig);
  });

  const makeOrder = (overrides = {}) => ({
    id: 1,
    orderNo: 'XJ001',
    status: 0,
    statusText: '待支付',
    productName: '丽江三日游',
    startDate: '2026-05-01',
    createdAt: '2026-04-01T10:30:00',
    payAmount: 598,
    adultCount: 2,
    childCount: 0,
    productId: 100,
    ...overrides,
  });

  // ==================== onLoad #184-186 ====================

  describe('onLoad', () => {
    test('#184 未传 status 参数 → currentStatus 保持空字符串', () => {
      page.onLoad({});
      expect(page.data.currentStatus).toBe('');
    });

    test('#185 传入 status=1 → 切换到对应 tab', () => {
      page.onLoad({ status: '1' });
      expect(page.data.currentStatus).toBe('1');
    });

    test('#186 计算 navBarTotalHeight', () => {
      page.onLoad({});
      expect(page.data.navBarTotalHeight).toBeGreaterThan(0);
    });
  });

  // ==================== onShow #187-188 ====================

  describe('onShow', () => {
    test('#187 未登录时跳过刷新', () => {
      checkLogin.mockReturnValue(false);
      const spy = jest.spyOn(page, 'refreshList');

      page.onShow();

      expect(spy).not.toHaveBeenCalled();
    });

    test('#188 已登录时触发刷新', async () => {
      orderApi.getList.mockResolvedValue({ list: [] });

      await page.onShow();

      expect(orderApi.getList).toHaveBeenCalled();
    });
  });

  // ==================== loadList #189-195 ====================

  describe('loadList - 加载列表', () => {
    test('#189 正常加载并格式化数据', async () => {
      orderApi.getList.mockResolvedValue({ list: [makeOrder()] });

      await page.loadList(true);

      expect(page.data.list).toHaveLength(1);
      expect(page.data.list[0].payAmountText).toBe('598.00');
      expect(page.data.list[0].peopleText).toBe('2成人');
      expect(page.data.list[0].showPayBtn).toBe(true);
      expect(page.data.list[0].showCancelBtn).toBe(true);
    });

    test('#190 追加模式：loadMore 将新数据追加到末尾', async () => {
      page.setData({ list: [makeOrder({ id: 1, orderNo: 'XJ001' })] });
      orderApi.getList.mockResolvedValue({ list: [makeOrder({ id: 2, orderNo: 'XJ002' })] });

      await page.loadList(false);

      expect(page.data.list).toHaveLength(2);
      expect(page.data.list[1].orderNo).toBe('XJ002');
    });

    test('#191 刷新模式：isRefresh=true 会替换列表', async () => {
      page.setData({ list: [makeOrder({ id: 1, orderNo: 'OLD' })] });
      orderApi.getList.mockResolvedValue({ list: [makeOrder({ id: 2, orderNo: 'NEW' })] });

      await page.loadList(true);

      expect(page.data.list).toHaveLength(1);
      expect(page.data.list[0].orderNo).toBe('NEW');
    });

    test('#192 按状态查询时传入 status 参数', async () => {
      page.setData({ currentStatus: '1' });
      orderApi.getList.mockResolvedValue({ list: [] });

      await page.loadList(true);

      expect(orderApi.getList).toHaveBeenCalledWith(
        expect.objectContaining({ status: 1 })
      );
    });

    test('#193 不带状态时不传 status', async () => {
      page.setData({ currentStatus: '' });
      orderApi.getList.mockResolvedValue({ list: [] });

      await page.loadList(true);

      const callArg = orderApi.getList.mock.calls[0][0];
      expect(callArg.status).toBeUndefined();
    });

    test('#194 空列表 → isEmpty=true', async () => {
      orderApi.getList.mockResolvedValue({ list: [] });

      await page.loadList(true);

      expect(page.data.isEmpty).toBe(true);
    });

    test('#195 API 异常 → 显示失败提示', async () => {
      orderApi.getList.mockRejectedValue(new Error('服务器错误'));

      await page.loadList(true);

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '加载失败' })
      );
      expect(page.data.loading).toBe(false);
    });

    test('#195-2 返回数量少于 pageSize → hasMore=false', async () => {
      const orders = Array.from({ length: 3 }, (_, i) => makeOrder({ id: i }));
      orderApi.getList.mockResolvedValue({ list: orders });

      await page.loadList(true);

      expect(page.data.hasMore).toBe(false);
    });
  });

  // ==================== formatOrder #196-204 ====================

  describe('formatOrder - 按钮显示', () => {
    test.each([
      [0, { showPayBtn: true, showCancelBtn: true, showRefundBtn: false, showBuyAgainBtn: false }],
      [1, { showPayBtn: false, showCancelBtn: false, showRefundBtn: true, showBuyAgainBtn: false }],
      [2, { showPayBtn: false, showCancelBtn: false, showRefundBtn: false, showBuyAgainBtn: false }],
      [3, { showPayBtn: false, showCancelBtn: false, showRefundBtn: false, showBuyAgainBtn: true }],
      [4, { showPayBtn: false, showCancelBtn: false, showRefundBtn: false, showBuyAgainBtn: true }],
      [6, { showPayBtn: false, showCancelBtn: false, showRefundBtn: false, showBuyAgainBtn: true }],
      [7, { showPayBtn: false, showCancelBtn: false, showRefundBtn: false, showBuyAgainBtn: true }],
    ])('#196-202 status=%d 按钮显示', (status, expected) => {
      const result = page.formatOrder(makeOrder({ status }));
      expect(result.showPayBtn).toBe(expected.showPayBtn);
      expect(result.showCancelBtn).toBe(expected.showCancelBtn);
      expect(result.showRefundBtn).toBe(expected.showRefundBtn);
      expect(result.showBuyAgainBtn).toBe(expected.showBuyAgainBtn);
    });

    test('#203 人数文本：成人+儿童', () => {
      const r = page.formatOrder(makeOrder({ adultCount: 2, childCount: 1 }));
      expect(r.peopleText).toBe('2成人 1儿童');
    });

    test('#204 人数文本：仅成人', () => {
      const r = page.formatOrder(makeOrder({ adultCount: 3, childCount: 0 }));
      expect(r.peopleText).toBe('3成人');
    });
  });

  // ==================== formatDate / formatDateTime ====================

  describe('日期格式化', () => {
    test('#205 formatDate 标准日期', () => {
      expect(page.formatDate('2026-05-01')).toBe('5月1日');
    });

    test('#206 formatDate 空值', () => {
      expect(page.formatDate(null)).toBe('');
      expect(page.formatDate('')).toBe('');
    });

    test('#207 formatDateTime', () => {
      expect(page.formatDateTime('2026-04-01T10:30:00')).toBe('2026-04-01 10:30');
    });

    test('#208 formatDateTime 空值', () => {
      expect(page.formatDateTime(null)).toBe('');
    });
  });

  // ==================== handleTabChange #209-211 ====================

  describe('handleTabChange', () => {
    test('#209 切换到新 tab → 重置列表并加载', async () => {
      orderApi.getList.mockResolvedValue({ list: [] });
      page.setData({ currentStatus: '', list: [makeOrder()] });

      page.handleTabChange({ currentTarget: { dataset: { status: '1' } } });

      expect(page.data.currentStatus).toBe('1');
      expect(page.data.page).toBe(1);
      expect(page.data.list).toEqual([]);
    });

    test('#210 点击当前 tab → 不重复加载', () => {
      page.setData({ currentStatus: '1' });

      page.handleTabChange({ currentTarget: { dataset: { status: '1' } } });

      expect(orderApi.getList).not.toHaveBeenCalled();
    });

    test('#211 切换 tab 时清空 isEmpty', async () => {
      orderApi.getList.mockResolvedValue({ list: [] });
      page.setData({ currentStatus: '', isEmpty: true });

      page.handleTabChange({ currentTarget: { dataset: { status: '0' } } });

      expect(page.data.isEmpty).toBe(false);
    });
  });

  // ==================== handleOrderTap #212 ====================

  test('#212 handleOrderTap 跳转详情', () => {
    page.handleOrderTap({ currentTarget: { dataset: { id: 1 } } });
    expect(go.orderDetail).toHaveBeenCalledWith(1);
  });

  // ==================== handleCancel #213-215 ====================

  describe('handleCancel', () => {
    test('#213 确认取消 → 调用 cancel API', async () => {
      wx.showModal.mockImplementation(({ success }) =>
        success({ confirm: true })
      );
      orderApi.cancel.mockResolvedValue({});
      orderApi.getList.mockResolvedValue({ list: [] });

      await page.handleCancel({ currentTarget: { dataset: { id: 1 } } });
      // 等待内部异步完成
      await new Promise(setImmediate);

      expect(orderApi.cancel).toHaveBeenCalledWith(1);
    });

    test('#214 点击取消弹窗 → 不调用 API', async () => {
      wx.showModal.mockImplementation(({ success }) =>
        success({ confirm: false })
      );

      await page.handleCancel({ currentTarget: { dataset: { id: 1 } } });

      expect(orderApi.cancel).not.toHaveBeenCalled();
    });

    test('#215 cancel API 失败 → 显示失败提示', async () => {
      wx.showModal.mockImplementation(({ success }) =>
        success({ confirm: true })
      );
      orderApi.cancel.mockRejectedValue(new Error('操作失败'));

      await page.handleCancel({ currentTarget: { dataset: { id: 1 } } });
      await new Promise(setImmediate);

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '操作失败' })
      );
    });
  });

  // ==================== handlePay #216-218 ====================

  describe('handlePay', () => {
    test('#216 支付成功 → 刷新列表', async () => {
      payApi.createOrderPayment.mockResolvedValue({ timeStamp: '1' });
      payApi.wxPay.mockResolvedValue({});
      orderApi.getList.mockResolvedValue({ list: [] });

      await page.handlePay({ currentTarget: { dataset: { id: 1 } } });

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '支付成功' })
      );
    });

    test('#217 用户取消支付（errCode=-2）→ 提示已取消', async () => {
      payApi.createOrderPayment.mockResolvedValue({});
      payApi.wxPay.mockRejectedValue({ code: -2 });

      await page.handlePay({ currentTarget: { dataset: { id: 1 } } });

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '已取消支付' })
      );
    });

    test('#218 支付失败 → 显示错误信息', async () => {
      payApi.createOrderPayment.mockResolvedValue({});
      payApi.wxPay.mockRejectedValue({ code: -1, message: '余额不足' });

      await page.handlePay({ currentTarget: { dataset: { id: 1 } } });

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '余额不足' })
      );
    });
  });

  // ==================== handleRefund #219 ====================

  test('#219 handleRefund 跳转退款页', () => {
    page.handleRefund({ currentTarget: { dataset: { id: 1 } } });
    expect(go.orderRefund).toHaveBeenCalledWith(1);
  });

  // ==================== handleBuyAgain #220 ====================

  describe('handleBuyAgain', () => {
    test('#220 有 productId → 跳转线路详情', () => {
      page.handleBuyAgain({ currentTarget: { dataset: { productId: 100 } } });
      expect(go.routeDetail).toHaveBeenCalledWith(100);
    });

    test('#220-2 无 productId → 不跳转', () => {
      page.handleBuyAgain({ currentTarget: { dataset: {} } });
      expect(go.routeDetail).not.toHaveBeenCalled();
    });
  });

  // ==================== loadMore / goExplore #221 ====================

  describe('loadMore', () => {
    test('#221 没有更多数据时直接返回', async () => {
      page.setData({ hasMore: false });

      await page.loadMore();

      expect(orderApi.getList).not.toHaveBeenCalled();
    });

    test('#221-2 loadingMore 中不重复加载', async () => {
      page.setData({ loadingMore: true, hasMore: true });

      await page.loadMore();

      expect(orderApi.getList).not.toHaveBeenCalled();
    });

    test('#221-3 goExplore 跳首页', () => {
      page.goExplore();
      expect(go.home).toHaveBeenCalled();
    });
  });
});
