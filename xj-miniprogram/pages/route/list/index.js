/**
 * 线路列表页
 */
const routeApi = require('../../../services/route');
const { go } = require('../../../utils/router');
const { HOT_CITIES, CITY_GROUPS } = require('../../../config/cities');

const CITY_HISTORY_KEY = 'departureCityHistory';

const FILTER_OPTIONS = {
  days: [
    { label: '不限', value: '' },
    { label: '1-3天', value: '1-3', min: 1, max: 3 },
    { label: '4-6天', value: '4-6', min: 4, max: 6 },
    { label: '7-10天', value: '7-10', min: 7, max: 10 },
    { label: '10天以上', value: '10+', min: 11, max: 0 },
  ],
  budget: [
    { label: '不限', value: '' },
    { label: '2000以下', value: '0-2000', min: 0, max: 2000 },
    { label: '2000-5000', value: '2000-5000', min: 2000, max: 5000 },
    { label: '5000-10000', value: '5000-10000', min: 5000, max: 10000 },
    { label: '10000以上', value: '10000+', min: 10000, max: 0 },
  ],
};

Page({
  data: {
    // 页面状态
    loading: false,
    loadingMore: false,
    // 线路列表
    list: [],
    // 分页
    page: 1,
    pageSize: 10,
    hasMore: true,
    // 搜索关键词 & 出发城市
    keyword: '',
    departureCity: '',
    statusBarHeight: 0,
    // 主 Tab 列表（首页入口不展示分类 tab）
    tabs: [],
    activeTab: '',
    // 分类（动态从接口加载，name 与 product_route.category 一致）
    categories: [{ name: '全部', value: '' }],
    activeCategory: '',
    // 筛选条件展示值（传给 header 组件显示）
    filterValues: {
      days: '',
      budget: '',
      hasFilter: false,
    },
    // 实际筛选参数（传给 API）
    filterParams: {
      minDays: '',
      maxDays: '',
      minPrice: '',
      maxPrice: '',
    },
    // 筛选下拉面板
    filterPanel: {
      show: false,
      type: '',
      title: '',
      options: [],
      selected: '',
      pendingValue: '',
    },
    filterPanelTop: 220,
    // nav-bar 高度（px），用于定位搜索栏
    navBarTotalHeight: 88,
    // 搜索栏自身高度（px），用于补充占位（nav-bar 已有自己的占位）
    searchBarHeight: 52,
    // nav-bar + 搜索栏合计高度（px），用于 route-list-header sticky top 和 filterPanelTop
    contentTop: 140,
    // 城市选择弹窗
    showCityPicker: false,
    cityScrollTo: '',
    hotCities: HOT_CITIES,
    cityGroups: CITY_GROUPS,
    cityIndexLetters: CITY_GROUPS.map(g => g.letter),
    cityHistory: [],
  },

  onLoad(options) {
    const sysInfo = wx.getSystemInfoSync();
    const statusBarHeight = sysInfo.statusBarHeight || 44;
    // 计算 nav-bar 高度（与首页相同逻辑）
    let navBarHeight = 44;
    try {
      const menuButton = wx.getMenuButtonBoundingClientRect();
      navBarHeight = menuButton.height + (menuButton.top - statusBarHeight) * 2;
    } catch (e) {}
    const navBarTotalHeight = statusBarHeight + navBarHeight;

    const departureCity = options.departureCity
      ? decodeURIComponent(options.departureCity)
      : (wx.getStorageSync('departureCity') || '上海');
    const keyword = options.keyword ? decodeURIComponent(options.keyword) : '';

    this.setData({ statusBarHeight, navBarTotalHeight, departureCity, keyword });

    // 加载分类后再加载列表，确保 activeCategory 已设置
    this.loadCategories(options.category || '').then(() => {
      this.loadList();
    });

    // 渲染完成后测量搜索栏高度 → 更新 searchBarHeight 和 contentTop
    wx.nextTick(() => {
      wx.createSelectorQuery().select('.rl-search-bar').boundingClientRect(barRect => {
        if (barRect) {
          const searchBarHeight = barRect.height;
          const contentTop = navBarTotalHeight + searchBarHeight;
          this.setData({ searchBarHeight, contentTop, filterPanelTop: contentTop + 132 });
          wx.nextTick(() => {
            wx.createSelectorQuery().select('.route-list-header').boundingClientRect(rect => {
              if (rect) this.setData({ filterPanelTop: rect.bottom });
            }).exec();
          });
        }
      }).exec();
    });
  },

  onPullDownRefresh() {
    this.setData({ page: 1, hasMore: true, list: [] });
    this.loadList().then(() => wx.stopPullDownRefresh());
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loadingMore) {
      this.loadMore();
    }
  },

  /**
   * 构建请求参数
   */
  _buildParams(page) {
    const { pageSize, keyword, activeCategory, filterParams, departureCity } = this.data;
    const params = { page, pageSize };
    if (keyword) params.keyword = keyword;
    if (activeCategory) params.category = activeCategory;
    if (departureCity) params.departureCity = departureCity;
    if (filterParams.minDays !== '') params.minDays = filterParams.minDays;
    if (filterParams.maxDays !== '') params.maxDays = filterParams.maxDays;
    if (filterParams.minPrice !== '') params.filterMinPrice = filterParams.minPrice;
    if (filterParams.maxPrice !== '') params.filterMaxPrice = filterParams.maxPrice;
    return params;
  },

  /**
   * 加载线路列表
   */
  /**
   * 加载分类列表（从接口动态获取，与管理端同源）
   * @param {string} initCategory - 页面入口时预选的分类名称
   */
  async loadCategories(initCategory) {
    try {
      const list = await routeApi.getCategories();
      // 第一项固定为"全部"，其余从接口返回
      const categories = [{ name: '全部', value: '' }].concat(
        (list || []).map(c => ({ name: c.name, value: c.name }))
      );
      const activeCategory = initCategory || '';
      this.setData({ categories, activeCategory });
    } catch (err) {
      console.error('加载分类失败', err);
      // 加载失败不影响主流程，保持默认"全部"
    }
  },

  async loadList() {
    this.setData({ loading: true });
    try {
      const res = await routeApi.getList(this._buildParams(1));
      const records = res.records || res.list || res || [];
      const list = records.map(this.formatRoute);
      this.setData({ list, page: 1, hasMore: list.length >= this.data.pageSize, loading: false });
    } catch (err) {
      console.error('加载线路列表失败', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
      this.setData({ loading: false });
    }
  },

  /**
   * 加载更多
   */
  async loadMore() {
    const nextPage = this.data.page + 1;
    this.setData({ loadingMore: true });
    try {
      const res = await routeApi.getList(this._buildParams(nextPage));
      const records = res.records || res.list || res || [];
      const newItems = records.map(this.formatRoute);
      this.setData({
        list: [...this.data.list, ...newItems],
        page: nextPage,
        hasMore: newItems.length >= this.data.pageSize,
        loadingMore: false,
      });
    } catch (err) {
      console.error('加载更多失败', err);
      this.setData({ loadingMore: false });
    }
  },

  formatRoute(item) {
    return { ...item, minPriceText: (item.minPrice || 0).toFixed(0) };
  },

  // ===== 筛选面板 =====

  /**
   * 点击筛选按钮，打开下拉面板
   */
  handleFilterTap(e) {
    const { type } = e.detail;
    const { filterPanel, filterValues } = this.data;

    // 同一个已展开的再点则关闭
    if (filterPanel.show && filterPanel.type === type) {
      this.setData({ 'filterPanel.show': false });
      return;
    }

    const TITLES = { days: '行程天数', budget: '价格预算' };
    const currentLabel = filterValues[type] || '';
    const options = FILTER_OPTIONS[type] || [];
    const selected = (options.find(o => o.label === currentLabel) || {}).value || '';

    this.setData({
      filterPanel: {
        show: true,
        type,
        title: TITLES[type] || '',
        options,
        selected,
        pendingValue: selected,
      },
    });
  },

  /**
   * 点击 chip 选项（暂存，不立即生效）
   */
  handleFilterChipTap(e) {
    const { value } = e.currentTarget.dataset;
    const current = this.data.filterPanel.pendingValue;
    // 再次点击同一项则取消
    this.setData({ 'filterPanel.pendingValue': current === value ? '' : value });
  },

  /**
   * 重置当前筛选
   */
  handleFilterReset() {
    this.setData({ 'filterPanel.pendingValue': '' });
  },

  /**
   * 确定：应用筛选并重新加载
   */
  handleFilterConfirm() {
    const { filterPanel, filterValues, filterParams } = this.data;
    const { type, pendingValue } = filterPanel;
    const options = FILTER_OPTIONS[type] || [];
    const option = options.find(o => o.value === pendingValue) || {};

    const newFilterValues = {
      ...filterValues,
      [type]: option.label && option.label !== '不限' ? option.label : '',
    };
    newFilterValues.hasFilter = !!(newFilterValues.days || newFilterValues.budget);

    const newFilterParams = { ...filterParams };
    if (type === 'days') {
      newFilterParams.minDays = pendingValue ? option.min : '';
      newFilterParams.maxDays = pendingValue ? option.max : '';
    } else if (type === 'budget') {
      newFilterParams.minPrice = pendingValue ? (option.min ?? '') : '';
      newFilterParams.maxPrice = pendingValue ? option.max : '';
    }

    this.setData({
      filterValues: newFilterValues,
      filterParams: newFilterParams,
      'filterPanel.show': false,
      list: [],
      page: 1,
      hasMore: true,
    });

    this.loadList();
  },

  /**
   * 点击遮罩关闭面板
   */
  handleFilterClose() {
    this.setData({ 'filterPanel.show': false });
  },

  /**
   * 更多筛选（暂留）
   */
  handleMoreFilter() {
    this.setData({ 'filterPanel.show': false });
    wx.showToast({ title: '更多筛选即将上线', icon: 'none' });
  },

  // ===== Tab / 分类 =====

  handleTabChange(e) {
    const { key } = e.detail;
    if (key === this.data.activeTab) return;
    this.setData({ activeTab: key, page: 1, list: [], hasMore: true });
    this.loadList();
  },

  handleCategoryChange(e) {
    const { id } = e.detail;
    if (id === this.data.activeCategory) return;
    this.setData({ activeCategory: id, page: 1, list: [], hasMore: true });
    this.loadList();
  },

  handleResetFilter() {
    this.setData({
      activeCategory: '',
      departureCity: '',
      keyword: '',
      filterParams: { minDays: '', maxDays: '', minPrice: '', maxPrice: '' },
      filterValues: { days: '', budget: '', hasFilter: false },
      page: 1,
      list: [],
      hasMore: true,
    });
    this.loadList();
  },

  handleBack() {
    wx.navigateBack({ delta: 1 });
  },

  // 点击搜索框：带关键词跳转搜索页（可回显）
  handleSearchBarTap() {
    const { departureCity, keyword } = this.data;
    let url = `/pages/search/index?departureCity=${encodeURIComponent(departureCity)}`;
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
    wx.navigateTo({ url });
  },

  // 点击 X：清空关键词，跳转搜索页（不回显）
  handleClearKeyword() {
    const { departureCity } = this.data;
    wx.navigateTo({ url: `/pages/search/index?departureCity=${encodeURIComponent(departureCity)}` });
  },

  handleSearchTap() {
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
    this.setData({ departureCity: city, showCityPicker: false, cityHistory: history });
    // 重新加载列表
    this.setData({ page: 1, list: [], hasMore: true });
    this.loadList();
  },

  handleClearCityHistory() {
    wx.removeStorageSync(CITY_HISTORY_KEY);
    this.setData({ cityHistory: [] });
  },

  handleLetterTap(e) {
    const letter = e.currentTarget.dataset.letter;
    this.setData({ cityScrollTo: `city-letter-${letter}` });
  },

  // ===== 卡片操作 =====

  handleFavorite(e) {
    const { id } = e.currentTarget.dataset;
    const list = this.data.list.map(item => {
      if (item.id === id) return { ...item, isFavorite: !item.isFavorite };
      return item;
    });
    this.setData({ list });
  },

  handleRouteTap(e) {
    const id = e.currentTarget.dataset.id;
    if (id) go.routeDetail(id);
  },

  onShareAppMessage() {
    return { title: '发现精彩线路', path: '/pages/route/list/index' };
  },
});
