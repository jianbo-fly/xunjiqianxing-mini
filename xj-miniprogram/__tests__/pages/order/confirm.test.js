/**
 * 订单确认页测试
 * calcAmount: #139 ~ #144
 * handleSubmit: #145 ~ #151
 * doPay: #152 ~ #154
 */

const { loadPage, createPageInstance } = require('../../helpers');

// Mock 所有 service 依赖
jest.mock('../../../services/order', () => ({
  create: jest.fn(),
}));
jest.mock('../../../services/traveler', () => ({}));
jest.mock('../../../services/coupon', () => ({
  getUsable: jest.fn(),
}));
jest.mock('../../../services/pay', () => ({
  createOrderPayment: jest.fn(),
  wxPay: jest.fn(),
}));
jest.mock('../../../services/route', () => ({}));

const orderApi = require('../../../services/order');
const payApi = require('../../../services/pay');

describe('订单确认页', () => {
  let pageConfig;
  let page;

  beforeAll(() => {
    pageConfig = loadPage('pages/order/confirm/index');
  });

  beforeEach(() => {
    jest.clearAllMocks();
    page = createPageInstance(pageConfig);
  });

  // ==================== calcAmount #139-144 ====================

  describe('calcAmount - 金额计算', () => {
    test('#139 仅成人：总价 = 成人价 × 成人数', () => {
      page.setData({
        adultCount: 2, childCount: 0,
        adultPrice: 299, childPrice: 0,
        selectedCoupon: null,
        adultTravelers: [{ name: '张三' }, { name: '李四' }],
        childTravelers: [],
      });

      page.calcAmount();

      expect(page.data.adultAmount).toBe(598);
      expect(page.data.childAmount).toBe(0);
      expect(page.data.totalAmount).toBe(598);
      expect(page.data.payAmount).toBe(598);
    });

    test('#140 成人 + 儿童：分别计算后求和', () => {
      page.setData({
        adultCount: 2, childCount: 1,
        adultPrice: 299, childPrice: 199,
        selectedCoupon: null,
        adultTravelers: [{ name: '张三' }, { name: '李四' }],
        childTravelers: [{ name: '小明' }],
      });

      page.calcAmount();

      expect(page.data.totalAmount).toBe(797);
      expect(page.data.payAmount).toBe(797);
    });

    test('#141 使用满减券（满足门槛）', () => {
      page.setData({
        adultCount: 2, childCount: 0,
        adultPrice: 500, childPrice: 0,
        selectedCoupon: { type: 1, minAmount: 800, discount: 100 },
        adultTravelers: [null, null],
        childTravelers: [],
      });

      page.calcAmount();

      expect(page.data.totalAmount).toBe(1000);
      expect(page.data.couponDiscount).toBe(100);
      expect(page.data.payAmount).toBe(900);
    });

    test('#142 使用满减券（不满足门槛），优惠为 0', () => {
      page.setData({
        adultCount: 1, childCount: 0,
        adultPrice: 200, childPrice: 0,
        selectedCoupon: { type: 1, minAmount: 800, discount: 100 },
        adultTravelers: [null],
        childTravelers: [],
      });

      page.calcAmount();

      expect(page.data.couponDiscount).toBe(0);
      expect(page.data.payAmount).toBe(200);
    });

    test('#143 使用折扣券（8折）', () => {
      page.setData({
        adultCount: 1, childCount: 0,
        adultPrice: 1000, childPrice: 0,
        selectedCoupon: { type: 2, discount: 80 },
        adultTravelers: [null],
        childTravelers: [],
      });

      page.calcAmount();

      expect(page.data.totalAmount).toBe(1000);
      expect(page.data.couponDiscount).toBeCloseTo(200, 2);
      expect(page.data.payAmount).toBeCloseTo(800, 2);
    });

    test('#144 全部参数为 0，总价为 0 不报错', () => {
      page.setData({
        adultCount: 0, childCount: 0,
        adultPrice: 0, childPrice: 0,
        selectedCoupon: null,
        adultTravelers: [], childTravelers: [],
      });

      expect(() => page.calcAmount()).not.toThrow();
      expect(page.data.totalAmount).toBe(0);
      expect(page.data.payAmount).toBe(0);
    });
  });

  // ==================== handleSubmit #145-151 ====================

  describe('handleSubmit - 提交订单', () => {
    const validData = {
      routeId: '1', packageId: '10', date: '2026-05-01',
      adultCount: 1, childCount: 0,
      adultTravelers: [{ name: '张三', idNo: '110101199001011234', phone: '13800138000' }],
      childTravelers: [],
      contact: { name: '张三', phone: '13800138000' },
      selectedCoupon: null,
      submitting: false,
    };

    test('#145 正常提交，创建订单并发起支付', async () => {
      page.setData(validData);
      orderApi.create.mockResolvedValue({ orderId: 100 });
      payApi.createOrderPayment.mockResolvedValue({ timeStamp: '1', nonceStr: 'a' });
      payApi.wxPay.mockResolvedValue({});

      await page.handleSubmit();

      expect(orderApi.create).toHaveBeenCalledWith(
        expect.objectContaining({
          skuId: '10',
          startDate: '2026-05-01',
          adultCount: 1,
          contactName: '张三',
          contactPhone: '13800138000',
        })
      );
    });

    test('#146 防重复提交（submitting=true）', async () => {
      page.setData({ ...validData, submitting: true });

      await page.handleSubmit();

      expect(orderApi.create).not.toHaveBeenCalled();
    });

    test('#147 成人出行人未填满', async () => {
      page.setData({
        ...validData,
        adultCount: 2,
        adultTravelers: [{ name: '张三' }], // 只填了1个
      });

      await page.handleSubmit();

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: expect.stringContaining('成人出行人') })
      );
      expect(orderApi.create).not.toHaveBeenCalled();
    });

    test('#148 儿童出行人未填满', async () => {
      page.setData({
        ...validData,
        childCount: 1,
        childTravelers: [], // 未填
      });

      await page.handleSubmit();

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: expect.stringContaining('儿童出行人') })
      );
    });

    test('#149 联系人姓名为空', async () => {
      page.setData({
        ...validData,
        contact: { name: '', phone: '13800138000' },
      });

      await page.handleSubmit();

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: expect.stringContaining('联系人姓名') })
      );
    });

    test('#150 联系人手机号为空', async () => {
      page.setData({
        ...validData,
        contact: { name: '张三', phone: '' },
      });

      await page.handleSubmit();

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: expect.stringContaining('联系人手机号') })
      );
    });

    test('#151 创建订单 API 返回失败，submitting 重置', async () => {
      page.setData(validData);
      orderApi.create.mockRejectedValue(new Error('服务器繁忙'));

      await page.handleSubmit();

      expect(page.data.submitting).toBe(false);
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '服务器繁忙' })
      );
    });
  });

  // ==================== doPay #152-154 ====================

  describe('doPay - 发起支付', () => {
    test('#152 支付成功跳转结果页', async () => {
      page.setData({
        route: { name: '丽江三日游' },
        packageInfo: { name: '标准套餐' },
        date: '2026-05-01',
        adultCount: 2,
        childCount: 0,
        payAmount: 998,
      });

      payApi.createOrderPayment.mockResolvedValue({ timeStamp: '1' });
      payApi.wxPay.mockResolvedValue({});

      await page.doPay(100);

      expect(wx.redirectTo).toHaveBeenCalledWith(
        expect.objectContaining({
          url: expect.stringContaining('orderId=100'),
        })
      );
    });

    test('#153 用户主动取消支付（errCode=-2），不提示错误', async () => {
      payApi.createOrderPayment.mockResolvedValue({});
      payApi.wxPay.mockRejectedValue({ code: -2 });

      wx.showModal.mockImplementation(({ success }) => {
        if (success) success({ confirm: false });
      });

      await page.doPay(100);

      // 不应该 showToast "支付失败"
      const toastCalls = wx.showToast.mock.calls;
      const hasPayFailToast = toastCalls.some(
        ([arg]) => arg.title && arg.title.includes('支付失败')
      );
      expect(hasPayFailToast).toBe(false);
      expect(page.data.submitting).toBe(false);
    });

    test('#154 支付 API 返回失败', async () => {
      payApi.createOrderPayment.mockResolvedValue({});
      payApi.wxPay.mockRejectedValue({ code: -1, message: '支付失败' });

      await page.doPay(100);

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '支付失败' })
      );
      expect(page.data.submitting).toBe(false);
    });
  });
});
