/**
 * 线路列表页
 */
const routeApi = require('../../../services/route');
const { go } = require('../../../utils/router');

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
    // 主 Tab 列表
    tabs: [
      { key: 'group', label: '跟团游' },
      { key: 'custom', label: '定制游' },
    ],
    // Tab 切换
    activeTab: 'group', // group/custom
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
    // 筛选条件
    categoryId: '',
    sortBy: '', // hot/new/price
    priceAsc: true,
    // 筛选值（传给组件）
    filterValues: {
      departure: '',
      days: '',
      budget: '',
      hasFilter: false,
    },
    // nav-bar 高度（px），用于 route-list-header sticky top
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
    this.setData({ navBarTotalHeight: statusBarHeight + navBarHeight });
    if (options.categoryId) {
      this.setData({ categoryId: options.categoryId });
    }
    if (options.sortBy) {
      this.setData({ sortBy: options.sortBy });
    }
    if (options.keyword) {
      this.setData({ keyword: options.keyword });
    }
    this.loadList();
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
   * 加载线路列表
   */
  async loadList() {
    const { page, pageSize, keyword, categoryId, sortBy, priceAsc } = this.data;

    this.setData({ loading: true });

    try {
      const params = { page, pageSize };
      if (keyword) params.keyword = keyword;
      if (categoryId) params.categoryId = categoryId;
      if (sortBy) params.sortBy = sortBy;
      if (sortBy === 'price') params.priceAsc = priceAsc;
      const res = await routeApi.getList(params);

      const records = res.records || res.list || res || [];
      const list = records.map(this.formatRoute);

      this.setData({
        list,
        hasMore: list.length >= pageSize,
        loading: false,
      });
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
    const { page, pageSize, keyword, categoryId, sortBy, priceAsc } = this.data;
    const nextPage = page + 1;

    this.setData({ loadingMore: true });

    try {
      const params = { page: nextPage, pageSize };
      if (keyword) params.keyword = keyword;
      if (categoryId) params.categoryId = categoryId;
      if (sortBy) params.sortBy = sortBy;
      if (sortBy === 'price') params.priceAsc = priceAsc;
      const res = await routeApi.getList(params);

      const records = res.records || res.list || res || [];
      const newItems = records.map(this.formatRoute);

      this.setData({
        list: [...this.data.list, ...newItems],
        page: nextPage,
        hasMore: newItems.length >= pageSize,
        loadingMore: false,
      });
    } catch (err) {
      console.error('加载更多失败', err);
      this.setData({ loadingMore: false });
    }
  },

  /**
   * 格式化线路数据
   */
  formatRoute(item) {
    return {
      ...item,
      minPriceText: (item.minPrice || 0).toFixed(0),
    };
  },

  /**
   * Tab 切换（组件事件：detail.key）
   */
  handleTabChange(e) {
    const { key } = e.detail;
    if (key === this.data.activeTab) return;
    this.setData({ activeTab: key, page: 1, list: [], hasMore: true });
    this.loadList();
  },

  /**
   * 分类切换（组件事件：detail.id）
   */
  handleCategoryChange(e) {
    const { id } = e.detail;
    if (id === this.data.activeCategory) return;
    this.setData({ activeCategory: id, categoryId: id, page: 1, list: [], hasMore: true });
    this.loadList();
  },

  /**
   * 点击搜索图标（组件事件）
   */
  handleSearchTap() {
    wx.navigateTo({ url: '/pages/route/list/index' });
  },

  /**
   * 搜索
   */
  handleSearch(e) {
    const keyword = e.detail.value;
    this.setData({ keyword, page: 1, list: [], hasMore: true });
    this.loadList();
  },

  /**
   * 筛选按钮点击（组件事件：detail.type）
   */
  handleFilterTap(e) {
    const { type } = e.detail;
    // 暂时弹出 toast 提示，后续接入筛选弹窗
    wx.showToast({ title: `${type} 筛选`, icon: 'none' });
  },

  /**
   * 更多筛选（组件事件）
   */
  handleMoreFilter() {
    wx.showToast({ title: '更多筛选', icon: 'none' });
  },

  /**
   * 排序切换
   */
  handleSortChange(e) {
    const { sort } = e.currentTarget.dataset;
    const { sortBy, priceAsc } = this.data;

    if (sort === 'price' && sortBy === 'price') {
      this.setData({ priceAsc: !priceAsc, page: 1, list: [], hasMore: true });
    } else {
      this.setData({ sortBy: sort, priceAsc: true, page: 1, list: [], hasMore: true });
    }

    this.loadList();
  },

  /**
   * 收藏
   */
  handleFavorite(e) {
    const { id } = e.currentTarget.dataset;
    const list = this.data.list.map(item => {
      if (item.id === id) return { ...item, isFavorite: !item.isFavorite };
      return item;
    });
    this.setData({ list });
  },

  /**
   * 线路点击
   */
  handleRouteTap(e) {
    const id = e.currentTarget.dataset.id;
    if (id) go.routeDetail(id);
  },

  /**
   * 分享
   */
  onShareAppMessage() {
    return {
      title: '发现精彩线路',
      path: '/pages/route/list/index',
    };
  },
});
