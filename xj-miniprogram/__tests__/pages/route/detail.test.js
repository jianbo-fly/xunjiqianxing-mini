/**
 * 线路详情页测试（#333 ~ #350）
 */

const { loadPage, createPageInstance } = require('../../helpers');

jest.mock('../../../services/route', () => ({
  getDetail: jest.fn(),
  getPackages: jest.fn(),
  getPriceCalendar: jest.fn(),
}));
jest.mock('../../../services/favorite', () => ({
  check: jest.fn(),
  add: jest.fn(),
  remove: jest.fn(),
}));
jest.mock('../../../utils/router', () => ({
  go: {},
  routes: {},
}));
jest.mock('../../../config/app.config', () => ({
  features: {
    customerService: { type: 'phone', phone: '10086', corpId: '' },
  },
}), { virtual: true });

const routeApi = require('../../../services/route');
const favoriteApi = require('../../../services/favorite');

// 补齐 setup.js 缺失的 wx API
wx.nextTick = (cb) => cb && cb();

describe('线路详情页', () => {
  let pageConfig;
  let page;

  beforeAll(() => {
    pageConfig = loadPage('pages/route/detail/index');
  });

  beforeEach(() => {
    jest.clearAllMocks();
    page = createPageInstance(pageConfig);
    // 屏蔽测量逻辑对 createSelectorQuery 的依赖
    page._measureDayCardPositions = jest.fn();
    page._scrollDateStripToCenter = jest.fn();
  });

  // ==================== loadRouteDetail #333-335 ====================

  describe('loadRouteDetail', () => {
    test('#333 正常加载：详情+套餐+日历', async () => {
      routeApi.getDetail.mockResolvedValue({
        id: 1,
        name: '丽江三日游',
        coverImage: 'cover.jpg',
        images: ['a.jpg', 'b.jpg'],
        packages: [],
      });
      routeApi.getPackages.mockResolvedValue([
        { id: 10, name: '经典套餐', basePrice: 2999, attrs: { days: 3, nights: 2 } },
      ]);
      routeApi.getPriceCalendar.mockResolvedValue([]);
      favoriteApi.check.mockResolvedValue({ isFavorite: false });

      page.setData({ routeId: '1' });
      await page.loadRouteDetail();

      expect(page.data.route.name).toBe('丽江三日游');
      expect(page.data.packages).toHaveLength(1);
      expect(page.data.selectedPackage.id).toBe(10);
      expect(page.data.bannerImages).toEqual(['a.jpg', 'b.jpg']);
      expect(page.data.loading).toBe(false);
    });

    test('#334 getPackages 失败 → 回退 route.packages', async () => {
      routeApi.getDetail.mockResolvedValue({
        id: 1,
        name: 'X',
        coverImage: 'c.jpg',
        packages: [{ id: 20, name: '备用', attrs: {} }],
      });
      routeApi.getPackages.mockRejectedValue(new Error('500'));
      routeApi.getPriceCalendar.mockResolvedValue([]);
      favoriteApi.check.mockResolvedValue({});

      page.setData({ routeId: '1' });
      await page.loadRouteDetail();

      expect(page.data.packages).toHaveLength(1);
      expect(page.data.packages[0].id).toBe(20);
    });

    test('#335 getDetail 失败 → toast 并关闭 loading', async () => {
      routeApi.getDetail.mockRejectedValue(new Error('fail'));

      page.setData({ routeId: '1' });
      await page.loadRouteDetail();

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '加载失败' })
      );
      expect(page.data.loading).toBe(false);
    });

    test('#335-2 images 为空 → 使用 coverImage 作为轮播', async () => {
      routeApi.getDetail.mockResolvedValue({
        id: 1, name: 'X', coverImage: 'only.jpg', images: [],
      });
      routeApi.getPackages.mockResolvedValue([]);
      favoriteApi.check.mockResolvedValue({});

      page.setData({ routeId: '1' });
      await page.loadRouteDetail();

      expect(page.data.bannerImages).toEqual(['only.jpg']);
    });
  });

  // ==================== processPackages #336-337 ====================

  describe('processPackages', () => {
    test('#336 attrs 包含 days/nights → 合并为 "3天2晚"', () => {
      const r = page.processPackages([
        { id: 1, attrs: { days: 3, nights: 2, hotel: '四星' } },
      ]);
      expect(r[0].attrsDisplay).toContain('3天2晚');
      expect(r[0].attrsDisplay).toContain('🏨四星');
    });

    test('#337 无 attrs → attrsDisplay 为空数组', () => {
      const r = page.processPackages([{ id: 1 }]);
      expect(r[0].attrsDisplay).toEqual([]);
    });
  });

  // ==================== calcTotalAmount #338 ====================

  describe('calcTotalAmount', () => {
    test('#338 合计 = 成人价*人数 + 儿童价*人数', () => {
      page.setData({
        selectedPrice: 1000,
        selectedChildPrice: 700,
        adultCount: 2,
        childCount: 1,
      });
      page.calcTotalAmount();
      expect(page.data.totalAmount).toBe(2700);
    });
  });

  // ==================== handleDateSelect #339-341 ====================

  describe('handleDateSelect', () => {
    test('#339 选中正常日期 → 更新价格与默认儿童价（70%）', () => {
      page.handleDateSelect({
        currentTarget: {
          dataset: { item: { date: '2026-05-01', price: 1000, stock: 5, isPast: false } },
        },
      });
      expect(page.data.selectedDate).toBe('2026-05-01');
      expect(page.data.selectedPrice).toBe(1000);
      expect(page.data.selectedChildPrice).toBe(700);
      expect(page.data.selectedStock).toBe(5);
      expect(page.data.adultCount).toBe(1);
      expect(page.data.childCount).toBe(0);
    });

    test('#340 过去的日期 → 跳过', () => {
      page.handleDateSelect({
        currentTarget: { dataset: { item: { date: '2020-01-01', isPast: true } } },
      });
      expect(page.data.selectedDate).toBe('');
    });

    test('#341 售罄 → toast 且不选中', () => {
      page.handleDateSelect({
        currentTarget: {
          dataset: { item: { date: '2026-05-01', price: 1000, stock: 0, isPast: false } },
        },
      });
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '该日期已售罄' })
      );
      expect(page.data.selectedDate).toBe('');
    });
  });

  // ==================== 成人/儿童数量控制 #342-345 ====================

  describe('数量控制', () => {
    test('#342 handleAdultPlus 库存充足 → +1', () => {
      page.setData({
        adultCount: 1, childCount: 0, selectedStock: 10,
        selectedPrice: 100, selectedChildPrice: 70,
      });
      page.handleAdultPlus();
      expect(page.data.adultCount).toBe(2);
      expect(page.data.totalAmount).toBe(200);
    });

    test('#342-2 handleAdultPlus 库存不足 → toast', () => {
      page.setData({ adultCount: 5, childCount: 5, selectedStock: 10 });
      page.handleAdultPlus();
      expect(page.data.adultCount).toBe(5);
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '库存不足' })
      );
    });

    test('#343 handleAdultMinus 儿童超过新成人数 → 同步减少', () => {
      page.setData({
        adultCount: 3, childCount: 3, selectedStock: 10,
        selectedPrice: 100, selectedChildPrice: 70,
      });
      page.handleAdultMinus();
      expect(page.data.adultCount).toBe(2);
      expect(page.data.childCount).toBe(2);
    });

    test('#343-2 handleAdultMinus 成人=1 → 保持', () => {
      page.setData({ adultCount: 1, childCount: 0 });
      page.handleAdultMinus();
      expect(page.data.adultCount).toBe(1);
    });

    test('#344 handleChildPlus 儿童>=成人 → toast', () => {
      page.setData({ adultCount: 1, childCount: 1, selectedStock: 10 });
      page.handleChildPlus();
      expect(page.data.childCount).toBe(1);
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '儿童数量不能超过成人' })
      );
    });

    test('#344-2 handleChildPlus 库存不足 → toast', () => {
      page.setData({ adultCount: 5, childCount: 4, selectedStock: 9 });
      page.handleChildPlus();
      expect(page.data.childCount).toBe(4);
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '库存不足' })
      );
    });

    test('#344-3 handleChildPlus 正常 → +1', () => {
      page.setData({
        adultCount: 2, childCount: 0, selectedStock: 10,
        selectedPrice: 100, selectedChildPrice: 70,
      });
      page.handleChildPlus();
      expect(page.data.childCount).toBe(1);
      expect(page.data.totalAmount).toBe(270);
    });

    test('#345 handleChildMinus 儿童>0 → -1', () => {
      page.setData({
        adultCount: 2, childCount: 2,
        selectedPrice: 100, selectedChildPrice: 70,
      });
      page.handleChildMinus();
      expect(page.data.childCount).toBe(1);
    });
  });

  // ==================== handleBook #346-347 ====================

  describe('handleBook', () => {
    test('#346 未选套餐 → toast 并弹出套餐选择', () => {
      page.setData({ selectedPackage: null });
      page.handleBook();
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '请选择套餐' })
      );
      expect(page.data.showPackagePopup).toBe(true);
      expect(wx.navigateTo).not.toHaveBeenCalled();
    });

    test('#346-2 未选日期 → toast 并弹出日历', () => {
      page.setData({ selectedPackage: { id: 1 }, selectedDate: '' });
      page.handleBook();
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '请选择出行日期' })
      );
      expect(page.data.showCalendarPopup).toBe(true);
    });

    test('#347 参数完整 → 跳转订单确认页', () => {
      page.setData({
        routeId: '1',
        selectedPackage: { id: 10 },
        selectedDate: '2026-05-01',
        adultCount: 2,
        childCount: 1,
        selectedPrice: 1000,
        selectedChildPrice: 700,
      });
      page.handleBook();
      expect(wx.navigateTo).toHaveBeenCalledWith(
        expect.objectContaining({
          url: expect.stringContaining('/pages/order/confirm/index?routeId=1'),
        })
      );
      const url = wx.navigateTo.mock.calls[0][0].url;
      expect(url).toContain('packageId=10');
      expect(url).toContain('date=2026-05-01');
      expect(url).toContain('adultCount=2');
      expect(url).toContain('childCount=1');
      expect(url).toContain('adultPrice=1000');
      expect(url).toContain('childPrice=700');
    });
  });

  // ==================== handleFavorite #348 ====================

  describe('handleFavorite', () => {
    test('#348 未收藏 → 收藏成功', async () => {
      favoriteApi.add.mockResolvedValue({});
      page.setData({ routeId: '1', isFavorite: false });

      await page.handleFavorite();

      expect(favoriteApi.add).toHaveBeenCalledWith({ routeId: '1' });
      expect(page.data.isFavorite).toBe(true);
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '收藏成功' })
      );
    });

    test('#348-2 已收藏 → 取消收藏', async () => {
      favoriteApi.remove.mockResolvedValue({});
      page.setData({ routeId: '1', isFavorite: true });

      await page.handleFavorite();

      expect(favoriteApi.remove).toHaveBeenCalled();
      expect(page.data.isFavorite).toBe(false);
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '已取消收藏' })
      );
    });

    test('#348-3 API 失败 → toast 操作失败', async () => {
      favoriteApi.add.mockRejectedValue(new Error('fail'));
      page.setData({ routeId: '1', isFavorite: false });

      await page.handleFavorite();

      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '操作失败' })
      );
    });
  });

  // ==================== handlePackageSelect #349 ====================

  test('#349 handlePackageSelect 切换套餐 → 重置日期价格并重新加载日历', () => {
    const spy = jest.spyOn(page, 'loadCalendar').mockImplementation(() => {});
    page.setData({
      packages: [{ id: 1 }, { id: 2 }],
      selectedDate: '2026-05-01',
      selectedPrice: 1000,
    });

    page.handlePackageSelect({ currentTarget: { dataset: { index: 1 } } });

    expect(page.data.selectedPackage.id).toBe(2);
    expect(page.data.selectedDate).toBe('');
    expect(page.data.selectedPrice).toBe(0);
    expect(spy).toHaveBeenCalledWith(2);
  });

  // ==================== handleShowCalendar #350 ====================

  describe('handleShowCalendar', () => {
    test('#350 未选套餐 → toast 且不打开弹窗', () => {
      page.setData({ selectedPackage: null });
      page.handleShowCalendar();
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '请先选择套餐' })
      );
      expect(page.data.showCalendarPopup).toBe(false);
    });

    test('#350-2 已选套餐 → 打开弹窗并加载日历', () => {
      const spy = jest.spyOn(page, 'loadCalendarPopup').mockImplementation(() => {});
      page.setData({ selectedPackage: { id: 10 } });

      page.handleShowCalendar();

      expect(page.data.showCalendarPopup).toBe(true);
      expect(spy).toHaveBeenCalledWith(10);
    });
  });

  // ==================== 其他辅助 ====================

  describe('辅助方法', () => {
    test('formatDate 格式化日期', () => {
      expect(page.formatDate(new Date(2026, 4, 1))).toBe('2026-05-01');
    });

    test('handleContentTabChange 切换 tab', () => {
      page.handleContentTabChange({ currentTarget: { dataset: { index: '2' } } });
      expect(page.data.contentTab).toBe(2);
    });

    test('handleSwiperChange 更新索引', () => {
      page.handleSwiperChange({ detail: { current: 3 } });
      expect(page.data.swiperIndex).toBe(3);
    });
  });
});
