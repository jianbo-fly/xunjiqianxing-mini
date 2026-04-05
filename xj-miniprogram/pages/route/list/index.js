/**
 * 线路列表页
 */
const routeApi = require('../../../services/route');
const { go } = require('../../../utils/router');

const FILTER_OPTIONS = {
  departure: [
    { label: '不限', value: '' },
    { label: '北京', value: '北京' },
    { label: '上海', value: '上海' },
    { label: '广州', value: '广州' },
    { label: '深圳', value: '深圳' },
    { label: '成都', value: '成都' },
    { label: '杭州', value: '杭州' },
    { label: '武汉', value: '武汉' },
    { label: '西安', value: '西安' },
    { label: '重庆', value: '重庆' },
    { label: '昆明', value: '昆明' },
  ],
  days: [
    { label: '不限', value: '' },
    { label: '1-3天', value: '1-3', min: 1, max: 3 },
    { label: '4-6天', value: '4-6', min: 4, max: 6 },
    { label: '7-10天', value: '7-10', min: 7, max: 10 },
    { label: '10天以上', value: '10+', min: 11, max: 999 },
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
    // 搜索关键词
    keyword: '',
    // 主 Tab 列表（首页入口不展示分类 tab）
    tabs: [],
    activeTab: '',
    // 分类
    categories: [
      { id: '', name: '全部' },
      { id: 'domestic', name: '国内游' },
      { id: 'overseas', name: '出境游' },
      { id: 'nearby', name: '周边游' },
      { id: 'island', name: '海岛游' },
      { id: 'resort', name: '度假游' },
    ],
    activeCategory: '',
    // 筛选条件展示值（传给 header 组件显示）
    filterValues: {
      departure: '',
      days: '',
      budget: '',
      hasFilter: false,
    },
    // 实际筛选参数（传给 API）
    filterParams: {
      departureCity: '',
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
    // nav-bar 高度（px）
    navBarTotalHeight: 88,
  },

  onLoad(options) {
    // 计算 nav-bar 实际高度
    const sysInfo = wx.getSystemInfoSync();
    const statusBarHeight = sysInfo.statusBarHeight || 44;
    let navBarHeight = 44;
    try {
      const menuButton = wx.getMenuButtonBoundingClientRect();
      navBarHeight = menuButton.height + (menuButton.top - statusBarHeight) * 2;
    } catch (e) {}
    const navBarTotalHeight = statusBarHeight + navBarHeight;
    this.setData({ navBarTotalHeight, filterPanelTop: navBarTotalHeight + 132 });

    if (options.categoryId) this.setData({ activeCategory: options.categoryId });
    if (options.keyword) this.setData({ keyword: options.keyword });

    this.loadList();

    // 渲染完成后动态测量 route-list-header 的实际高度
    wx.nextTick(() => {
      const query = wx.createSelectorQuery();
      query.select('.route-list-header').boundingClientRect(rect => {
        if (rect) {
          this.setData({ filterPanelTop: rect.bottom });
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
    const { pageSize, keyword, activeCategory, filterParams } = this.data;
    const params = { page, pageSize };
    if (keyword) params.keyword = keyword;
    if (activeCategory) params.category = activeCategory;
    if (filterParams.departureCity) params.departureCity = filterParams.departureCity;
    if (filterParams.minDays) params.minDays = filterParams.minDays;
    if (filterParams.maxDays) params.maxDays = filterParams.maxDays;
    if (filterParams.minPrice !== '') params.minPrice = filterParams.minPrice;
    if (filterParams.maxPrice) params.maxPrice = filterParams.maxPrice;
    return params;
  },

  /**
   * 加载线路列表
   */
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

    const TITLES = { departure: '热门出发城市', days: '行程天数', budget: '价格预算' };
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
    newFilterValues.hasFilter = !!(newFilterValues.departure || newFilterValues.days || newFilterValues.budget);

    const newFilterParams = { ...filterParams };
    if (type === 'departure') {
      newFilterParams.departureCity = pendingValue;
    } else if (type === 'days') {
      newFilterParams.minDays = pendingValue ? option.min : '';
      newFilterParams.maxDays = pendingValue ? option.max : '';
    } else if (type === 'budget') {
      newFilterParams.minPrice = pendingValue ? option.min : '';
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

  handleSearchTap() {
    go.home(); // 跳首页搜索，或直接跳搜索页
    wx.navigateTo({ url: '/pages/search/index' });
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
