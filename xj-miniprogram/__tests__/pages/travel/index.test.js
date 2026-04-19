/**
 * 旅游页测试（#361 ~ #378）
 */

const { loadPage, createPageInstance } = require('../../helpers');

jest.mock('../../../services/route', () => ({
  getList: jest.fn(),
}));
jest.mock('../../../services/custom', () => ({
  submit: jest.fn(),
}));
jest.mock('../../../utils/router', () => ({
  go: { routeDetail: jest.fn() },
  routes: {},
}));
jest.mock('../../../config/cities', () => ({
  HOT_CITIES: [],
  CITY_GROUPS: [{ letter: 'A', cities: [] }],
}), { virtual: true });

const routeApi = require('../../../services/route');
const customApi = require('../../../services/custom');
const { go } = require('../../../utils/router');

wx.nextTick = (cb) => cb && cb();

describe('旅游页', () => {
  let pageConfig;
  let page;
  const storage = {};

  beforeAll(() => {
    wx.getSystemInfoSync.mockReturnValue({ statusBarHeight: 44 });
    wx.getMenuButtonBoundingClientRect.mockReturnValue({
      left: 280, right: 355, bottom: 80, top: 48, width: 75, height: 32,
    });
    pageConfig = loadPage('pages/travel/index/index');
  });

  beforeEach(() => {
    jest.clearAllMocks();
    Object.keys(storage).forEach(k => delete storage[k]);
    wx.getStorageSync.mockImplementation(k => storage[k]);
    wx.setStorageSync.mockImplementation((k, v) => { storage[k] = v; });
    wx.removeStorageSync.mockImplementation(k => { delete storage[k]; });
    routeApi.getList.mockResolvedValue({ list: [] });
    page = createPageInstance(pageConfig);
  });

  // ==================== 跟团游 ====================

  // #361 分类筛选（国内/出境/周边）
  test('#361 handleCategoryChange 切换分类并重新加载', async () => {
    routeApi.getList.mockResolvedValue({ list: [{ id: 1 }] });

    page.handleCategoryChange({ currentTarget: { dataset: { index: 1 } } });
    await new Promise(setImmediate);

    expect(page.data.categoryIndex).toBe(1);
    const params = routeApi.getList.mock.calls[0][0];
    expect(params.category).toBe('overseas');
  });

  test('#361-2 周边游 → category=nearby', async () => {
    page.handleCategoryChange({ currentTarget: { dataset: { index: 2 } } });
    await new Promise(setImmediate);

    const params = routeApi.getList.mock.calls[0][0];
    expect(params.category).toBe('nearby');
  });

  // #362 天数筛选
  test('#362 groupFilterConfirm days → minDays/maxDays', () => {
    page.setData({
      groupFilterPanel: { show: true, type: 'days', pendingValue: '4-6天' },
      filters: { departure: '', days: '', budgetLabel: '', minDays: '', maxDays: '', minPrice: '', maxPrice: '' },
    });

    page.handleGroupFilterConfirm();

    expect(page.data.filters.days).toBe('4-6天');
    expect(page.data.filters.minDays).toBe(4);
    expect(page.data.filters.maxDays).toBe(6);
  });

  // #363 价格筛选
  test('#363 groupFilterConfirm budget → minPrice/maxPrice', () => {
    page.setData({
      groupFilterPanel: { show: true, type: 'budget', pendingValue: '5000-8000' },
      filters: { departure: '', days: '', budgetLabel: '', minDays: '', maxDays: '', minPrice: '', maxPrice: '' },
    });

    page.handleGroupFilterConfirm();

    expect(page.data.filters.budgetLabel).toBe('5000-8000');
    expect(page.data.filters.minPrice).toBe(5000);
    expect(page.data.filters.maxPrice).toBe(8000);
  });

  // #364 出发城市筛选（优先筛选面板）
  test('#364 筛选面板出发城市优先于搜索栏', async () => {
    page.setData({
      departureCity: '上海',
      filters: { departure: '北京', days: '', budgetLabel: '', minDays: '', maxDays: '', minPrice: '', maxPrice: '' },
      routeLoading: false,
      routeFinished: false,
    });

    await page.loadRouteList(true);

    const params = routeApi.getList.mock.calls[0][0];
    expect(params.departureCity).toBe('北京');
  });

  test('#364-2 筛选面板无出发城市 → fallback 搜索栏', async () => {
    page.setData({
      departureCity: '上海',
      filters: { departure: '', days: '', budgetLabel: '', minDays: '', maxDays: '', minPrice: '', maxPrice: '' },
      routeLoading: false,
      routeFinished: false,
    });

    await page.loadRouteList(true);

    const params = routeApi.getList.mock.calls[0][0];
    expect(params.departureCity).toBe('上海');
  });

  // #365 分页加载
  test('#365 分页追加列表', async () => {
    page.setData({
      routeList: [{ id: 1 }],
      routePage: 2,
      routeLoading: false,
      routeFinished: false,
    });
    routeApi.getList.mockResolvedValue({ list: [{ id: 2 }] });

    await page.loadRouteList(false);

    expect(page.data.routeList).toHaveLength(2);
    expect(page.data.routePage).toBe(3);
  });

  test('#365-2 routeLoading=true → 不重复加载', async () => {
    page.setData({ routeLoading: true });
    await page.loadRouteList(true);
    expect(routeApi.getList).not.toHaveBeenCalled();
  });

  test('#365-3 routeFinished=true → 不加载', async () => {
    page.setData({ routeFinished: true });
    await page.loadRouteList(true);
    expect(routeApi.getList).not.toHaveBeenCalled();
  });

  test('#365-4 少于 pageSize → finished', async () => {
    page.setData({ routeLoading: false, routeFinished: false, routePageSize: 10 });
    routeApi.getList.mockResolvedValue({ list: [{ id: 1 }] });

    await page.loadRouteList(true);

    expect(page.data.routeFinished).toBe(true);
  });

  // #366 筛选面板确认/重置
  test('#366 handleGroupFilterReset 重置 pendingValue', () => {
    page.setData({ groupFilterPanel: { show: true, type: 'days', pendingValue: '4-6天' } });
    page.handleGroupFilterReset();
    expect(page.data['groupFilterPanel.pendingValue']).toBe('');
  });

  test('#366-2 handleGroupFilterConfirm 空 pendingValue → 清除筛选', () => {
    page.setData({
      groupFilterPanel: { show: true, type: 'days', pendingValue: '' },
      filters: { departure: '', days: '4-6天', budgetLabel: '', minDays: 4, maxDays: 6, minPrice: '', maxPrice: '' },
    });

    page.handleGroupFilterConfirm();

    expect(page.data.filters.days).toBe('');
    expect(page.data.filters.minDays).toBe('');
    expect(page.data.filters.maxDays).toBe('');
  });

  // ==================== 定制游 ====================

  // #367 表单必填校验顺序
  describe('#367 handleSubmitCustom 必填校验', () => {
    test('无目的地 → toast 请选择目的地', async () => {
      page.setData({ selectedDest: '' });
      await page.handleSubmitCustom();
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '请选择目的地' })
      );
      expect(customApi.submit).not.toHaveBeenCalled();
    });

    test('无出行时间 → toast', async () => {
      page.setData({ selectedDest: '云南', selectedTime: '' });
      await page.handleSubmitCustom();
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '请选择出行时间' })
      );
    });

    test('无出行天数 → toast', async () => {
      page.setData({ selectedDest: '云南', selectedTime: '本周末', selectedDays: '' });
      await page.handleSubmitCustom();
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '请选择出行天数' })
      );
    });

    test('无预算 → toast', async () => {
      page.setData({ selectedDest: '云南', selectedTime: '本周末', selectedDays: '3-5天', selectedBudget: '' });
      await page.handleSubmitCustom();
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '请选择预算范围' })
      );
    });

    test('无手机号 → toast', async () => {
      page.setData({
        selectedDest: '云南', selectedTime: '本周末', selectedDays: '3-5天',
        selectedBudget: '3-5千', phone: '',
      });
      await page.handleSubmitCustom();
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '请输入联系方式' })
      );
    });
  });

  // #368 目的地选"其他"需手动输入
  test('#368 选"其他" + 无 customDest → toast', async () => {
    page.setData({ selectedDest: '其他', customDest: '' });
    await page.handleSubmitCustom();
    expect(wx.showToast).toHaveBeenCalledWith(
      expect.objectContaining({ title: '请输入目的地' })
    );
  });

  test('#368-2 handleDestSelect 设置 selectedDest', () => {
    page.handleDestSelect({ currentTarget: { dataset: { dest: '三亚' } } });
    expect(page.data.selectedDest).toBe('三亚');
    expect(page.data.customDest).toBe('');
  });

  // #369 选"选择日期"触发日期选择器
  test('#369 handleDateChange 设置 customDate 并标记时间为选择日期', () => {
    page.handleDateChange({ detail: { value: '2026-05-01' } });
    expect(page.data.customDate).toBe('2026-05-01');
    expect(page.data.selectedTime).toBe('选择日期');
  });

  test('#369-2 handleTimeSelect 设置 selectedTime', () => {
    page.handleTimeSelect({ currentTarget: { dataset: { time: '下周' } } });
    expect(page.data.selectedTime).toBe('下周');
    expect(page.data.customDate).toBe('');
  });

  // #370 成人数最少 1，儿童最少 0
  test('#370 成人不能低于 1', () => {
    page.setData({ adultCount: 1 });
    page.handleAdultChange({ currentTarget: { dataset: { type: 'minus' } } });
    expect(page.data.adultCount).toBe(1);
  });

  test('#370-2 成人可增加', () => {
    page.setData({ adultCount: 2 });
    page.handleAdultChange({ currentTarget: { dataset: { type: 'plus' } } });
    expect(page.data.adultCount).toBe(3);
  });

  test('#370-3 儿童不能低于 0', () => {
    page.setData({ childCount: 0 });
    page.handleChildChange({ currentTarget: { dataset: { type: 'minus' } } });
    expect(page.data.childCount).toBe(0);
  });

  test('#370-4 儿童可增加', () => {
    page.setData({ childCount: 0 });
    page.handleChildChange({ currentTarget: { dataset: { type: 'plus' } } });
    expect(page.data.childCount).toBe(1);
  });

  // #371 需求标签多选
  test('#371 handleNeedTagTap 切换选中状态', () => {
    page.setData({
      needTags: [
        { id: 'parent', name: '带父母', selected: false },
        { id: 'noshop', name: '纯玩无购物', selected: false },
      ],
    });

    page.handleNeedTagTap({ currentTarget: { dataset: { index: 0 } } });

    expect(page.data['needTags[0].selected']).toBe(true);
  });

  test('#371-2 提交时传递选中的标签', async () => {
    customApi.submit.mockResolvedValue({});
    page.setData({
      selectedDest: '云南', selectedTime: '本周末', selectedDays: '3-5天',
      selectedBudget: '3-5千', phone: '13800138000', adultCount: 2, childCount: 0,
      needTags: [
        { id: 'parent', name: '带父母', selected: true },
        { id: 'noshop', name: '纯玩无购物', selected: true },
        { id: 'child', name: '亲子游', selected: false },
      ],
      extraNote: '',
    });

    await page.handleSubmitCustom();

    expect(customApi.submit).toHaveBeenCalledWith(
      expect.objectContaining({ needs: '带父母,纯玩无购物' })
    );
  });

  // #372 提交成功后重置表单
  test('#372 提交成功 → showSuccess + resetCustomForm', async () => {
    customApi.submit.mockResolvedValue({});
    page.setData({
      selectedDest: '云南', selectedTime: '本周末', selectedDays: '3-5天',
      selectedBudget: '3-5千', phone: '13800138000', adultCount: 3, childCount: 1,
      needTags: [{ id: 'parent', name: '带父母', selected: true }],
      extraNote: '备注',
    });

    await page.handleSubmitCustom();

    expect(page.data.showSuccess).toBe(true);
    expect(page.data.submitting).toBe(false);

    page.handleCloseSuccess();

    expect(page.data.showSuccess).toBe(false);
    expect(page.data.selectedDest).toBe('');
    expect(page.data.adultCount).toBe(2);
    expect(page.data.childCount).toBe(0);
    expect(page.data.extraNote).toBe('');
  });

  // #373 提交失败
  test('#373 提交失败 → toast', async () => {
    customApi.submit.mockRejectedValue(new Error('fail'));
    page.setData({
      selectedDest: '云南', selectedTime: '本周末', selectedDays: '3-5天',
      selectedBudget: '3-5千', phone: '13800138000',
      needTags: [],
    });

    await page.handleSubmitCustom();

    expect(wx.showToast).toHaveBeenCalledWith(
      expect.objectContaining({ title: '提交失败，请重试' })
    );
    expect(page.data.submitting).toBe(false);
  });

  // #375 Tab 切换
  test('#375 handleTabChange 切换 currentTab', () => {
    page.handleTabChange({ currentTarget: { dataset: { index: 1 } } });
    expect(page.data.currentTab).toBe(1);
  });

  // #378 定制需求：创建新需求跳转（tab=1）
  test('#378 onLoad tab=1 → currentTab=1', () => {
    page.setData({ routeLoading: false, routeFinished: false });
    page.onLoad({ tab: '1' });
    expect(page.data.currentTab).toBe(1);
  });

  // 辅助方法
  describe('辅助方法', () => {
    test('handleCardTap 跳转路线详情', () => {
      page.handleCardTap({ currentTarget: { dataset: { id: 10 } } });
      expect(go.routeDetail).toHaveBeenCalledWith(10);
    });

    test('handleCardTap 无 id → 不跳转', () => {
      page.handleCardTap({ currentTarget: { dataset: {} } });
      expect(go.routeDetail).not.toHaveBeenCalled();
    });

    test('handleFavorite 切换收藏状态', () => {
      page.setData({ routeList: [{ id: 1, isFavorite: false }] });
      page.handleFavorite({ currentTarget: { dataset: { id: 1 } } });
      expect(page.data['routeList[0].isFavorite']).toBe(true);
    });

    test('handleSearchBarTap 跳转搜索页', () => {
      page.setData({ departureCity: '上海', keyword: '丽江' });
      page.handleSearchBarTap();
      expect(wx.navigateTo).toHaveBeenCalledWith(
        expect.objectContaining({
          url: expect.stringContaining('keyword=' + encodeURIComponent('丽江')),
        })
      );
    });

    test('handleClearKeyword 跳转搜索页不带 keyword', () => {
      page.setData({ departureCity: '上海' });
      page.handleClearKeyword();
      const url = wx.navigateTo.mock.calls[0][0].url;
      expect(url).not.toContain('keyword=');
    });

    test('handleBack → navigateBack', () => {
      page.handleBack();
      expect(wx.navigateBack).toHaveBeenCalled();
    });

    test('handleBackHome → switchTab 首页', () => {
      page.handleBackHome();
      expect(wx.switchTab).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/pages/index/index' })
      );
    });

    test('handleViewMyCustom → navigateTo 定制列表', () => {
      page.handleViewMyCustom();
      expect(wx.navigateTo).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/pages/custom/list/index' })
      );
    });

    test('handleExtraNoteInput 同步字数', () => {
      page.handleExtraNoteInput({ detail: { value: '你好世界' } });
      expect(page.data.extraNote).toBe('你好世界');
      expect(page.data.extraNoteLength).toBe(4);
    });

    test('handlePhoneInput 更新手机号', () => {
      page.handlePhoneInput({ detail: { value: '13800138000' } });
      expect(page.data.phone).toBe('13800138000');
    });

    test('handleBudgetSelect 设置预算', () => {
      page.handleBudgetSelect({ currentTarget: { dataset: { budget: '3-5千' } } });
      expect(page.data.selectedBudget).toBe('3-5千');
    });

    test('handleDaysSelect 设置出行天数', () => {
      page.handleDaysSelect({ currentTarget: { dataset: { days: '6-8天' } } });
      expect(page.data.selectedDays).toBe('6-8天');
    });

    test('loadUserPhone 从缓存读取手机号', () => {
      storage.userInfo = { phone: '13900139000' };
      page.loadUserPhone();
      expect(page.data.phone).toBe('13900139000');
    });

    test('onReachBottom currentTab=0 → 加载更多', () => {
      page.setData({ currentTab: 0, routeLoading: false, routeFinished: false });
      page.onReachBottom();
      expect(routeApi.getList).toHaveBeenCalled();
    });

    test('onReachBottom currentTab=1 → 不加载', () => {
      page.setData({ currentTab: 1 });
      page.onReachBottom();
      expect(routeApi.getList).not.toHaveBeenCalled();
    });
  });
});
