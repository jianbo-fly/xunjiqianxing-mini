/**
 * 订单确认页测试
 * calcAmount: #139 ~ #144
 * handleSubmit: #145 ~ #151
 * doPay: #152 ~ #154
 * handleSelectTraveler: #155 ~ #157
 * handleToggleContact: #158 ~ #159
 * handleSelectCoupon: #160 ~ #161
 * loadData: #162 ~ #164
 * refreshTravelerList: #165 ~ #167
 * onLoad/onShow: #168 ~ #169
 * handleRemoveTraveler: #170
 * UI toggles: #171 ~ #173
 * handleNoCoupon: #174
 * popup controls: #175 ~ #179
 * loadUsableCoupons: #180 ~ #181
 * handleContactInput: #182 ~ #183
 */

const { loadPage, createPageInstance } = require('../../helpers');

// Mock 所有 service 依赖
jest.mock('../../../services/order', () => ({
  create: jest.fn(),
}));
jest.mock('../../../services/traveler', () => ({
  getList: jest.fn(),
}));
jest.mock('../../../services/coupon', () => ({
  getUsable: jest.fn(),
}));
jest.mock('../../../services/pay', () => ({
  createOrderPayment: jest.fn(),
  wxPay: jest.fn(),
}));
jest.mock('../../../services/route', () => ({
  getDetail: jest.fn(),
  getPackages: jest.fn(),
}));

const orderApi = require('../../../services/order');
const payApi = require('../../../services/pay');
const travelerApi = require('../../../services/traveler');
const couponApi = require('../../../services/coupon');
const routeApi = require('../../../services/route');

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

  // ==================== handleSelectTraveler #155-157 ====================

  describe('handleSelectTraveler - 选择出行人', () => {
    test('#155 选择新出行人填入空位', () => {
      page.setData({
        travelerType: 'adult',
        travelerIndex: 0,
        adultCount: 2, childCount: 0,
        adultPrice: 100, childPrice: 0,
        selectedCoupon: null,
        adultTravelers: [null, null],
        childTravelers: [],
        showTravelerPopup: true,
      });

      const traveler = { id: 1, name: '张三', phone: '13800138000' };
      page.handleSelectTraveler({ currentTarget: { dataset: { traveler } } });

      expect(page.data.adultTravelers[0]).toEqual(traveler);
      expect(page.data.showTravelerPopup).toBe(false);
    });

    test('#156 选择填入儿童位置', () => {
      page.setData({
        travelerType: 'child',
        travelerIndex: 0,
        adultCount: 1, childCount: 1,
        adultPrice: 100, childPrice: 50,
        selectedCoupon: null,
        adultTravelers: [{ id: 1 }],
        childTravelers: [null],
        showTravelerPopup: true,
      });

      const traveler = { id: 2, name: '小明' };
      page.handleSelectTraveler({ currentTarget: { dataset: { traveler } } });

      expect(page.data.childTravelers[0]).toEqual(traveler);
    });

    test('#157 选择已选中的出行人（重复）→ 提示', () => {
      const traveler = { id: 1, name: '张三' };
      page.setData({
        travelerType: 'adult',
        travelerIndex: 1,
        adultCount: 2, childCount: 0,
        adultPrice: 100, childPrice: 0,
        selectedCoupon: null,
        adultTravelers: [traveler, null],
        childTravelers: [],
      });

      page.handleSelectTraveler({ currentTarget: { dataset: { traveler } } });

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: expect.stringContaining('已添加') })
      );
      expect(page.data.adultTravelers[1]).toBe(null);
    });
  });

  // ==================== handleToggleContact #158-159 ====================

  describe('handleToggleContact - 联系人切换', () => {
    test('#158 勾选"使用其他联系人"→ 清空 contact', () => {
      page.setData({
        useOtherContact: false,
        adultTravelers: [{ name: '张三', phone: '13800138000' }],
        contact: { name: '张三', phone: '13800138000' },
      });

      page.handleToggleContact();

      expect(page.data.useOtherContact).toBe(true);
      expect(page.data.contact.name).toBe('');
      expect(page.data.contact.phone).toBe('');
    });

    test('#159 取消勾选 → 恢复第一个成人出行人', () => {
      page.setData({
        useOtherContact: true,
        adultTravelers: [{ name: '张三', phone: '13800138000' }],
        contact: { name: '', phone: '' },
      });

      page.handleToggleContact();

      expect(page.data.useOtherContact).toBe(false);
      expect(page.data.contact.name).toBe('张三');
      expect(page.data.contact.phone).toBe('13800138000');
    });
  });

  // ==================== handleSelectCoupon #160-161 ====================

  describe('handleSelectCoupon - 选择优惠券', () => {
    test('#160 首次选择优惠券', () => {
      page.setData({
        selectedCoupon: null,
        showCouponPopup: true,
        adultCount: 1, childCount: 0,
        adultPrice: 500, childPrice: 0,
        adultTravelers: [null], childTravelers: [],
      });

      const coupon = { id: 10, type: 1, minAmount: 300, discount: 50 };
      page.handleSelectCoupon({ currentTarget: { dataset: { coupon } } });

      expect(page.data.selectedCoupon).toEqual(coupon);
      expect(page.data.showCouponPopup).toBe(false);
    });

    test('#161 再次点击同一优惠券 → 取消选择', () => {
      const coupon = { id: 10, type: 1, minAmount: 300, discount: 50 };
      page.setData({
        selectedCoupon: coupon,
        showCouponPopup: true,
        adultCount: 1, childCount: 0,
        adultPrice: 500, childPrice: 0,
        adultTravelers: [null], childTravelers: [],
      });

      page.handleSelectCoupon({ currentTarget: { dataset: { coupon } } });

      expect(page.data.selectedCoupon).toBe(null);
    });
  });

  // ==================== loadData #162-164 ====================

  describe('loadData - 加载数据', () => {
    test('#162 并行加载路线和出行人', async () => {
      const route = { id: 1, name: '丽江', packages: [{ id: 10, name: '标准' }] };
      const travelers = [{ id: 1, name: '张三', phone: '13800138000', isDefault: true }];
      routeApi.getDetail.mockResolvedValue(route);
      travelerApi.getList.mockResolvedValue(travelers);
      couponApi.getUsable.mockResolvedValue([]);

      page.setData({ routeId: '1', packageId: '10', adultCount: 1, childCount: 0, adultPrice: 100, childPrice: 0 });
      await page.loadData();

      expect(routeApi.getDetail).toHaveBeenCalledWith('1');
      expect(travelerApi.getList).toHaveBeenCalled();
      expect(page.data.loading).toBe(false);
    });

    test('#163 仅有一个成人时自动填充默认出行人', async () => {
      const defaultTraveler = { id: 1, name: '张三', phone: '13800138000', isDefault: true };
      routeApi.getDetail.mockResolvedValue({ id: 1, packages: [{ id: 10 }] });
      travelerApi.getList.mockResolvedValue([defaultTraveler]);
      couponApi.getUsable.mockResolvedValue([]);

      page.setData({ routeId: '1', packageId: '10', adultCount: 1, childCount: 0, adultPrice: 100, childPrice: 0 });
      await page.loadData();

      expect(page.data.adultTravelers[0]).toEqual(defaultTraveler);
      expect(page.data.contact.name).toBe('张三');
    });

    test('#164 路线不存在 → 提示错误', async () => {
      routeApi.getDetail.mockRejectedValue(new Error('路线不存在'));
      travelerApi.getList.mockResolvedValue([]);

      page.setData({ routeId: '999', packageId: '10', adultCount: 1, childCount: 0 });
      await page.loadData();

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '加载失败' })
      );
      expect(page.data.loading).toBe(false);
    });
  });

  // ==================== refreshTravelerList #165-167 ====================

  describe('refreshTravelerList - 刷新出行人列表', () => {
    test('#165 检测到新增出行人 → 自动填充空位', async () => {
      page.setData({
        travelerType: 'adult',
        travelerIndex: 1,
        adultCount: 2, childCount: 0,
        adultPrice: 100, childPrice: 0,
        selectedCoupon: null,
        adultTravelers: [{ id: 1, name: '张三' }, null],
        childTravelers: [],
        travelerList: [{ id: 1, name: '张三' }],
      });

      const newTraveler = { id: 2, name: '李四' };
      travelerApi.getList.mockResolvedValue([{ id: 1, name: '张三' }, newTraveler]);

      await page.refreshTravelerList();

      expect(page.data.adultTravelers[1]).toEqual(newTraveler);
    });

    test('#166 无新增出行人 → 列表不变', async () => {
      const existing = [{ id: 1, name: '张三' }];
      page.setData({
        travelerType: 'adult',
        travelerIndex: 0,
        adultTravelers: [{ id: 1 }],
        childTravelers: [],
        travelerList: existing,
      });

      travelerApi.getList.mockResolvedValue(existing);

      await page.refreshTravelerList();

      expect(page.data.travelerList).toEqual(existing);
    });

    test('#167 API 调用失败 → 不崩溃', async () => {
      page.setData({
        travelerType: 'adult',
        travelerIndex: 0,
        adultTravelers: [null],
        childTravelers: [],
        travelerList: [],
      });

      travelerApi.getList.mockRejectedValue(new Error('网络错误'));

      await expect(page.refreshTravelerList()).resolves.not.toThrow();
    });
  });

  // ==================== onLoad / onShow #168-169 ====================

  describe('onLoad / onShow', () => {
    test('#168 onLoad 正确解析路由参数', () => {
      page.loadData = jest.fn();

      page.onLoad({
        routeId: '1', packageId: '10', date: '2026-05-01',
        adultCount: '2', childCount: '1',
        adultPrice: '299', childPrice: '199',
      });

      expect(page.data.routeId).toBe('1');
      expect(page.data.adultCount).toBe(2);
      expect(page.data.childCount).toBe(1);
      expect(page.data.adultPrice).toBe(299);
      expect(page.data.childPrice).toBe(199);
      expect(page.data.totalPeople).toBe(3);
    });

    test('#169 onShow 返回页面时刷新出行人列表', () => {
      page.refreshTravelerList = jest.fn();
      page.needRefreshTravelers = true;

      page.onShow();

      expect(page.refreshTravelerList).toHaveBeenCalled();
      expect(page.needRefreshTravelers).toBe(false);
    });
  });

  // ==================== handleRemoveTraveler #170 ====================

  describe('handleRemoveTraveler', () => {
    test('#170 清空指定位置', () => {
      page.setData({
        adultCount: 2, childCount: 0,
        adultPrice: 100, childPrice: 0,
        selectedCoupon: null,
        adultTravelers: [{ id: 1, name: '张三' }, { id: 2, name: '李四' }],
        childTravelers: [],
      });

      page.handleRemoveTraveler({ currentTarget: { dataset: { type: 'adult', index: 0 } } });

      expect(page.data.adultTravelers[0]).toBe(null);
      expect(page.data.adultTravelers[1]).toEqual({ id: 2, name: '李四' });
    });
  });

  // ==================== UI toggles #171-173 ====================

  describe('UI 切换操作', () => {
    test('#171 handleTogglePriceDetail 切换价格明细', () => {
      page.setData({ showPriceDetail: false });
      page.handleTogglePriceDetail();
      expect(page.data.showPriceDetail).toBe(true);

      page.handleTogglePriceDetail();
      expect(page.data.showPriceDetail).toBe(false);
    });

    test('#172 handleToggleInsurance 切换意外险', () => {
      page.setData({ insuranceSelected: false });
      page.handleToggleInsurance();
      expect(page.data.insuranceSelected).toBe(true);
    });

    test('#173 handleBack 返回上一页', () => {
      page.handleBack();
      expect(wx.navigateBack).toHaveBeenCalled();
    });
  });

  // ==================== handleNoCoupon #174 ====================

  describe('handleNoCoupon', () => {
    test('#174 不使用优惠券', () => {
      page.setData({
        selectedCoupon: { id: 10 },
        showCouponPopup: true,
        adultCount: 1, childCount: 0,
        adultPrice: 500, childPrice: 0,
        adultTravelers: [null], childTravelers: [],
      });

      page.handleNoCoupon();

      expect(page.data.selectedCoupon).toBe(null);
      expect(page.data.showCouponPopup).toBe(false);
    });
  });

  // ==================== popup controls #175-179 ====================

  describe('弹窗控制', () => {
    test('#175 handleShowTravelerPopup 正确解析 type 和 index', () => {
      page.handleShowTravelerPopup({
        currentTarget: { dataset: { type: 'child', index: 1 } },
      });

      expect(page.data.showTravelerPopup).toBe(true);
      expect(page.data.travelerType).toBe('child');
      expect(page.data.travelerIndex).toBe(1);
    });

    test('#176 handleCloseTravelerPopup 隐藏弹窗', () => {
      page.setData({ showTravelerPopup: true });
      page.handleCloseTravelerPopup();
      expect(page.data.showTravelerPopup).toBe(false);
    });

    test('#177 handleShowCouponPopup 打开弹窗', () => {
      page.handleShowCouponPopup();
      expect(page.data.showCouponPopup).toBe(true);
    });

    test('#178 handleCloseCouponPopup 关闭弹窗', () => {
      page.setData({ showCouponPopup: true });
      page.handleCloseCouponPopup();
      expect(page.data.showCouponPopup).toBe(false);
    });

    test('#179 handleShowAddTraveler 设置 refreshFlag 并跳转', () => {
      page.setData({ showTravelerPopup: true });

      page.handleShowAddTraveler();

      expect(page.data.showTravelerPopup).toBe(false);
      expect(page.needRefreshTravelers).toBe(true);
      expect(wx.navigateTo).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/pages/member/travelers/edit' })
      );
    });
  });

  // ==================== loadUsableCoupons #180-181 ====================

  describe('loadUsableCoupons - 加载可用优惠券', () => {
    test('#180 无可用优惠券', async () => {
      page.setData({ routeId: '1', totalAmount: 500 });
      couponApi.getUsable.mockResolvedValue([]);

      await page.loadUsableCoupons();

      expect(page.data.coupons).toEqual([]);
    });

    test('#181 API 异常 → 不崩溃', async () => {
      page.setData({ routeId: '1', totalAmount: 500 });
      couponApi.getUsable.mockRejectedValue(new Error('网络错误'));

      await expect(page.loadUsableCoupons()).resolves.not.toThrow();
    });
  });

  // ==================== handleContactInput #182-183 ====================

  describe('handleContactInput - 联系人输入', () => {
    test('#182 实时更新联系人字段', () => {
      page.handleContactInput({
        currentTarget: { dataset: { field: 'name' } },
        detail: { value: '李四' },
      });

      expect(page.data['contact.name']).toBe('李四');
    });

    test('#183 输入手机号', () => {
      page.handleContactInput({
        currentTarget: { dataset: { field: 'phone' } },
        detail: { value: '13900139000' },
      });

      expect(page.data['contact.phone']).toBe('13900139000');
    });
  });
});
