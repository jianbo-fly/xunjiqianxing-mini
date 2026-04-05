/**
 * 旅游页 - 跟团游 & 定制游
 */
const routeApi = require('../../../services/route');
const { HOT_CITIES, CITY_GROUPS } = require('../../../config/cities');

const CITY_HISTORY_KEY = 'departureCityHistory';

// 筛选面板选项（与路线列表页保持一致）
const GROUP_FILTER_OPTIONS = {
  departure: [
    { label: '北京', value: '北京' },
    { label: '上海', value: '上海' },
    { label: '广州', value: '广州' },
    { label: '深圳', value: '深圳' },
    { label: '成都', value: '成都' },
    { label: '杭州', value: '杭州' },
    { label: '南京', value: '南京' },
    { label: '武汉', value: '武汉' },
  ],
  days: [
    { label: '1-3天', value: '1-3天' },
    { label: '4-6天', value: '4-6天' },
    { label: '7-9天', value: '7-9天' },
    { label: '10天+', value: '10天+' },
  ],
  budget: [
    { label: '3000以下', value: '3000以下' },
    { label: '3000-5000', value: '3000-5000' },
    { label: '5000-8000', value: '5000-8000' },
    { label: '8000以上', value: '8000以上' },
  ],
};
const customApi = require('../../../services/custom');
const { go } = require('../../../utils/router');

Page({
  data: {
    statusBarHeight: 44,
    navBarTotalHeight: 88,
    searchBarHeight: 52,
    contentTop: 140,
    // 搜索框
    departureCity: '',
    keyword: '',
    // 城市选择弹窗
    showCityPicker: false,
    cityScrollTo: '',
    hotCities: HOT_CITIES,
    cityGroups: CITY_GROUPS,
    cityIndexLetters: CITY_GROUPS.map(g => g.letter),
    cityHistory: [],
    // 顶部Tab
    currentTab: 0, // 0=跟团游, 1=定制游
    tabs: ['跟团游', '定制游'],

    // ========== 跟团游相关 ==========
    // 二级分类
    categoryIndex: 0,
    categories: ['国内游', '出境游', '周边游'],

    // 筛选条件
    showFilter: false,
    groupFilterPanel: {
      show: false,
      type: '',
      title: '',
      options: [],
      pendingValue: '',
    },
    groupFilterPanelTop: 0,
    filters: {
      departure: '',
      days: '',
      priceMin: '',
      priceMax: '',
      budgetLabel: '',
    },
    departures: ['不限', '北京', '上海', '广州', '深圳', '成都', '杭州', '南京', '武汉'],
    daysOptions: ['不限', '1-3天', '4-6天', '7-9天', '10天+'],
    filterBudgets: ['不限', '3000以下', '3000-5000', '5000-8000', '8000以上'],

    // 线路列表
    routeList: [],
    routeLoading: false,
    routeFinished: false,
    routePage: 1,
    routePageSize: 10,

    // ========== 定制游相关 ==========
    // 目的地（含图标颜色，对齐 Figma 74-7679）
    destinations: [
      { name: '云南', color: '#DBEAFE' },
      { name: '三亚', color: '#CCFBF1' },
      { name: '日本', color: '#FEE2E2' },
      { name: '新疆', color: '#FEF9C3' },
      { name: '西藏', color: '#E0E7FF' },
      { name: '其他', color: '#E5E7EB' },
    ],
    selectedDest: '',
    customDest: '',

    // 出行时间
    timeOptions: ['本周末', '下周', '本月', '选择日期'],
    selectedTime: '',
    customDate: '',

    // 出行天数
    daysChoices: ['3-5天', '6-8天', '9天+', '待定'],
    selectedDays: '',

    // 出行人数
    adultCount: 2,
    childCount: 0,

    // 预算范围（Figma: 2×2 格局，4 个选项）
    budgetOptions: ['3千以下', '3-5千', '5-8千', '高端定制'],
    selectedBudget: '',

    // 其他需求（Figma 更新标签）
    needTags: [
      { id: 'parent', name: '带父母', selected: false },
      { id: 'noshop', name: '纯玩无购物', selected: false },
      { id: 'child', name: '亲子游', selected: false },
      { id: 'honeymoon', name: '蜜月', selected: false },
      { id: 'photo', name: '摄影采风', selected: false },
    ],
    extraNote: '',
    extraNoteLength: 0,

    // 联系方式
    phone: '',

    // 提交状态
    submitting: false,
    showSuccess: false,
  },

  onLoad(options) {
    const sysInfo = wx.getSystemInfoSync();
    const statusBarHeight = sysInfo.statusBarHeight || 44;
    let navBarHeight = 44;
    try {
      const menuButton = wx.getMenuButtonBoundingClientRect();
      navBarHeight = menuButton.height + (menuButton.top - statusBarHeight) * 2;
    } catch (e) {}
    const navBarTotalHeight = statusBarHeight + navBarHeight;

    const departureCity = wx.getStorageSync('departureCity') || '上海';
    const initialTab = options && options.tab ? parseInt(options.tab) : 0;
    this.setData({
      statusBarHeight,
      navBarTotalHeight,
      departureCity,
      currentTab: initialTab,
    });

    this.loadRouteList(true);
    this.loadUserPhone();

    wx.nextTick(() => {
      wx.createSelectorQuery().select('.tv-search-bar').boundingClientRect(barRect => {
        if (barRect) {
          const searchBarHeight = barRect.height;
          const contentTop = navBarTotalHeight + searchBarHeight;
          this.setData({ searchBarHeight, contentTop });
          wx.nextTick(() => {
            wx.createSelectorQuery().select('.sticky-bar').boundingClientRect(rect => {
              if (rect) this.setData({ groupFilterPanelTop: rect.bottom });
            }).exec();
          });
        }
      }).exec();
    });
  },

  /**
   * 返回
   */
  handleBack() {
    wx.navigateBack({ fail: () => wx.switchTab({ url: '/pages/index/index' }) });
  },

  // 点击搜索框关键词区域：带关键词跳搜索页
  handleSearchBarTap() {
    const { departureCity, keyword } = this.data;
    let url = `/pages/search/index?departureCity=${encodeURIComponent(departureCity)}`;
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
    wx.navigateTo({ url });
  },

  // 点击 X：跳搜索页，不带关键词
  handleClearKeyword() {
    const { departureCity } = this.data;
    wx.navigateTo({ url: `/pages/search/index?departureCity=${encodeURIComponent(departureCity)}` });
  },

  // 保留兼容
  handleSearch() {
    this.handleSearchBarTap();
  },

  // ===== 城市选择弹窗 =====
  handleCityTap() {
    const history = wx.getStorageSync(CITY_HISTORY_KEY) || [];
    this.setData({ showCityPicker: true, cityScrollTo: '', cityHistory: history });
  },

  handleCloseCityPicker() {
    this.setData({ showCityPicker: false });
  },

  handleCitySelect(e) {
    const city = e.currentTarget.dataset.city;
    let history = wx.getStorageSync(CITY_HISTORY_KEY) || [];
    history = [city, ...history.filter(c => c !== city)].slice(0, 5);
    wx.setStorageSync(CITY_HISTORY_KEY, history);
    wx.setStorageSync('departureCity', city);
    this.setData({ departureCity: city, showCityPicker: false, cityHistory: history,
      routePage: 1, routeList: [], routeFinished: false });
    this.loadRouteList(true);
  },

  handleClearCityHistory() {
    wx.removeStorageSync(CITY_HISTORY_KEY);
    this.setData({ cityHistory: [] });
  },

  handleLetterTap(e) {
    const letter = e.currentTarget.dataset.letter;
    this.setData({ cityScrollTo: `city-letter-${letter}` });
  },

  /**
   * 切换顶部Tab
   */
  handleTabChange(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ currentTab: index });
  },

  // ==================== 跟团游 ====================

  /**
   * 切换二级分类
   */
  handleCategoryChange(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ categoryIndex: index, routePage: 1, routeList: [], routeFinished: false });
    this.loadRouteList(true);
  },

  /**
   * 显示筛选
   */
  handleShowFilter() {
    this.setData({ showFilter: true });
  },

  /**
   * 关闭筛选
   */
  handleCloseFilter() {
    this.setData({ showFilter: false });
  },

  /**
   * 选择出发地
   */
  handleDepartureChange(e) {
    const departure = this.data.departures[e.detail.value];
    this.setData({ 'filters.departure': departure === '不限' ? '' : departure });
  },

  /**
   * 选择天数
   */
  handleDaysChange(e) {
    const days = this.data.daysOptions[e.detail.value];
    this.setData({ 'filters.days': days === '不限' ? '' : days });
  },

  /**
   * 重置筛选
   */
  handleResetFilter() {
    this.setData({
      filters: { departure: '', days: '', priceMin: '', priceMax: '', budgetLabel: '' }
    });
  },

  /**
   * 筛选栏 - 预算选择
   */
  handleFilterBudgetChange(e) {
    const idx = parseInt(e.detail.value);
    const budget = this.data.filterBudgets[idx];
    const budgetRanges = {
      '不限': { priceMin: '', priceMax: '' },
      '3000以下': { priceMin: '', priceMax: '3000' },
      '3000-5000': { priceMin: '3000', priceMax: '5000' },
      '5000-8000': { priceMin: '5000', priceMax: '8000' },
      '8000以上': { priceMin: '8000', priceMax: '' },
    };
    const range = budgetRanges[budget] || { priceMin: '', priceMax: '' };
    this.setData({
      'filters.budgetLabel': budget === '不限' ? '' : budget,
      'filters.priceMin': range.priceMin,
      'filters.priceMax': range.priceMax,
    });
    this.setData({ routePage: 1, routeList: [], routeFinished: false });
    this.loadRouteList(true);
  },

  /**
   * 筛选弹窗 - 价格最低输入
   */
  handlePriceMinInput(e) {
    this.setData({ 'filters.priceMin': e.detail.value });
  },

  /**
   * 筛选弹窗 - 价格最高输入
   */
  handlePriceMaxInput(e) {
    this.setData({ 'filters.priceMax': e.detail.value });
  },

  // ==================== 跟团游 自定义筛选面板 ====================

  handleGroupFilterTap(e) {
    const { type } = e.currentTarget.dataset;
    const { groupFilterPanel, filters } = this.data;
    if (groupFilterPanel.show && groupFilterPanel.type === type) {
      this.setData({ 'groupFilterPanel.show': false });
      return;
    }
    const TITLES = { departure: '热门出发城市', days: '行程天数', budget: '价格预算' };
    const options = GROUP_FILTER_OPTIONS[type] || [];
    const currentLabel = type === 'budget' ? filters.budgetLabel : filters[type];
    const selected = (options.find(o => o.label === currentLabel) || {}).value || '';
    this.setData({
      groupFilterPanel: {
        show: true,
        type,
        title: TITLES[type] || '',
        options,
        pendingValue: selected,
      },
    });
  },

  handleGroupFilterChipTap(e) {
    const { value } = e.currentTarget.dataset;
    const current = this.data.groupFilterPanel.pendingValue;
    this.setData({ 'groupFilterPanel.pendingValue': current === value ? '' : value });
  },

  handleGroupFilterReset() {
    this.setData({ 'groupFilterPanel.pendingValue': '' });
  },

  handleGroupFilterConfirm() {
    const { groupFilterPanel, filters } = this.data;
    const { type, pendingValue } = groupFilterPanel;
    const newFilters = { ...filters };
    if (type === 'departure') {
      newFilters.departure = pendingValue;
    } else if (type === 'days') {
      newFilters.days = pendingValue;
    } else if (type === 'budget') {
      newFilters.budgetLabel = pendingValue;
      const budgetRanges = {
        '3000以下': { priceMin: '', priceMax: '3000' },
        '3000-5000': { priceMin: '3000', priceMax: '5000' },
        '5000-8000': { priceMin: '5000', priceMax: '8000' },
        '8000以上': { priceMin: '8000', priceMax: '' },
      };
      const range = budgetRanges[pendingValue] || { priceMin: '', priceMax: '' };
      newFilters.priceMin = range.priceMin;
      newFilters.priceMax = range.priceMax;
    }
    this.setData({
      filters: newFilters,
      'groupFilterPanel.show': false,
      routePage: 1,
      routeList: [],
      routeFinished: false,
    });
    this.loadRouteList(true);
  },

  handleGroupFilterClose() {
    this.setData({ 'groupFilterPanel.show': false });
  },

  /**
   * 确认筛选
   */
  handleConfirmFilter() {
    this.setData({ showFilter: false, routePage: 1, routeList: [], routeFinished: false });
    this.loadRouteList(true);
  },

  /**
   * 加载线路列表
   */
  async loadRouteList(refresh = false) {
    if (this.data.routeLoading || this.data.routeFinished) return;

    this.setData({ routeLoading: true });

    try {
      const categoryMap = ['domestic', 'overseas', 'nearby'];
      const { departureCity, keyword } = this.data;
      const params = {
        page: this.data.routePage,
        pageSize: this.data.routePageSize,
        category: categoryMap[this.data.categoryIndex] || 'domestic',
      };
      if (departureCity) params.departureCity = departureCity;
      if (keyword) params.keyword = keyword;
      // 只追加有实际值的筛选条件，避免发送空字符串
      Object.entries(this.data.filters).forEach(([k, v]) => {
        if (v !== '' && v != null) params[k] = v;
      });

      const res = await routeApi.getList(params);
      const list = res.list || res.records || [];

      this.setData({
        routeList: refresh ? list : [...this.data.routeList, ...list],
        routePage: this.data.routePage + 1,
        routeFinished: list.length < this.data.routePageSize,
      });
    } catch (e) {
      console.error('加载线路失败', e);
    } finally {
      this.setData({ routeLoading: false });
    }
  },

  /**
   * 路线卡片点击（内联卡片）
   */
  handleCardTap(e) {
    const { id } = e.currentTarget.dataset;
    if (id) go.routeDetail(id);
  },

  /**
   * 收藏按钮点击
   */
  handleFavorite(e) {
    const { id } = e.currentTarget.dataset;
    const idx = this.data.routeList.findIndex(r => r.id === id);
    if (idx < 0) return;
    const key = `routeList[${idx}].isFavorite`;
    this.setData({ [key]: !this.data.routeList[idx].isFavorite });
  },

  /**
   * 线路点击（兼容旧 route-card 组件事件，保留以防万一）
   */
  handleRouteTap(e) {
    const { route } = e.detail;
    if (route && route.id) go.routeDetail(route.id);
  },

  /**
   * 触底加载更多
   */
  onReachBottom() {
    if (this.data.currentTab === 0) {
      this.loadRouteList();
    }
  },

  // ==================== 定制游 ====================

  /**
   * 获取用户手机号
   */
  loadUserPhone() {
    const userInfo = wx.getStorageSync('userInfo') || {};
    this.setData({ phone: userInfo.phone || '' });
  },

  /**
   * 选择目的地（destinations 现为对象数组，dataset.dest 取 name 字段）
   */
  handleDestSelect(e) {
    const dest = e.currentTarget.dataset.dest;
    this.setData({ selectedDest: dest, customDest: '' });
  },

  /**
   * 输入自定义目的地
   */
  handleCustomDestInput(e) {
    this.setData({ customDest: e.detail.value });
  },

  /**
   * 选择出行时间
   */
  handleTimeSelect(e) {
    const time = e.currentTarget.dataset.time;
    this.setData({ selectedTime: time, customDate: '' });
  },

  /**
   * 日期选择
   */
  handleDateChange(e) {
    this.setData({ customDate: e.detail.value, selectedTime: '选择日期' });
  },

  /**
   * 选择出行天数
   */
  handleDaysSelect(e) {
    const days = e.currentTarget.dataset.days;
    this.setData({ selectedDays: days });
  },

  /**
   * 成人数量变更
   */
  handleAdultChange(e) {
    const type = e.currentTarget.dataset.type;
    let count = this.data.adultCount;
    if (type === 'minus' && count > 1) count--;
    if (type === 'plus' && count < 99) count++;
    this.setData({ adultCount: count });
  },

  /**
   * 儿童数量变更
   */
  handleChildChange(e) {
    const type = e.currentTarget.dataset.type;
    let count = this.data.childCount;
    if (type === 'minus' && count > 0) count--;
    if (type === 'plus' && count < 99) count++;
    this.setData({ childCount: count });
  },

  /**
   * 选择预算
   */
  handleBudgetSelect(e) {
    const budget = e.currentTarget.dataset.budget;
    this.setData({ selectedBudget: budget });
  },

  /**
   * 切换需求标签
   */
  handleNeedTagTap(e) {
    const index = e.currentTarget.dataset.index;
    const key = `needTags[${index}].selected`;
    this.setData({ [key]: !this.data.needTags[index].selected });
  },

  /**
   * 输入额外备注（同步字数统计）
   */
  handleExtraNoteInput(e) {
    const val = e.detail.value;
    this.setData({ extraNote: val, extraNoteLength: val.length });
  },

  /**
   * 手机号修改（聚焦输入框即可编辑，此方法保留供 UI 调用）
   */
  handlePhoneChangeTap() {
    // 输入框本身可直接编辑，无需额外操作
  },

  /**
   * 输入手机号
   */
  handlePhoneInput(e) {
    this.setData({ phone: e.detail.value });
  },

  /**
   * 提交定制需求
   */
  async handleSubmitCustom() {
    // 表单验证
    if (!this.data.selectedDest) {
      wx.showToast({ title: '请选择目的地', icon: 'none' });
      return;
    }
    if (this.data.selectedDest === '其他' && !this.data.customDest) {
      wx.showToast({ title: '请输入目的地', icon: 'none' });
      return;
    }
    if (!this.data.selectedTime) {
      wx.showToast({ title: '请选择出行时间', icon: 'none' });
      return;
    }
    if (!this.data.selectedDays) {
      wx.showToast({ title: '请选择出行天数', icon: 'none' });
      return;
    }
    if (!this.data.selectedBudget) {
      wx.showToast({ title: '请选择预算范围', icon: 'none' });
      return;
    }
    if (!this.data.phone) {
      wx.showToast({ title: '请输入联系方式', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });

    try {
      const selectedNeeds = this.data.needTags.filter(t => t.selected).map(t => t.name);

      await customApi.submit({
        destination: this.data.selectedDest === '其他' ? this.data.customDest : this.data.selectedDest,
        travelTime: this.data.selectedTime === '选择日期' ? this.data.customDate : this.data.selectedTime,
        travelDays: this.data.selectedDays,
        adultCount: this.data.adultCount,
        childCount: this.data.childCount,
        budget: this.data.selectedBudget,
        needs: selectedNeeds.join(','),
        extraNote: this.data.extraNote,
        phone: this.data.phone,
      });

      this.setData({ showSuccess: true });
    } catch (e) {
      console.error('提交定制需求失败', e);
      wx.showToast({ title: '提交失败，请重试', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  },

  /**
   * 返回首页
   */
  handleBackHome() {
    wx.switchTab({ url: '/pages/index/index' });
  },

  /**
   * 查看我的定制
   */
  handleViewMyCustom() {
    wx.navigateTo({ url: '/pages/custom/list/index' });
  },

  /**
   * 关闭成功弹窗
   */
  handleCloseSuccess() {
    this.setData({ showSuccess: false });
    // 重置表单
    this.resetCustomForm();
  },

  /**
   * 重置定制表单
   */
  resetCustomForm() {
    this.setData({
      selectedDest: '',
      customDest: '',
      selectedTime: '',
      customDate: '',
      selectedDays: '',
      adultCount: 2,
      childCount: 0,
      selectedBudget: '',
      needTags: this.data.needTags.map(t => ({ ...t, selected: false })),
      extraNote: '',
      extraNoteLength: 0,
    });
  },
});
