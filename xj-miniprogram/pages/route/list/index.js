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
    // 筛选条件
    categoryId: '',
    sortBy: '', // hot/new/price
    priceAsc: true, // 价格升序/降序
  },

  onLoad(options) {
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
      const res = await routeApi.getList({
        page,
        pageSize,
        keyword: keyword || undefined,
        categoryId: categoryId || undefined,
        sortBy: sortBy || undefined,
        priceAsc: sortBy === 'price' ? priceAsc : undefined,
      });

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
      const res = await routeApi.getList({
        page: nextPage,
        pageSize,
        keyword: keyword || undefined,
        categoryId: categoryId || undefined,
        sortBy: sortBy || undefined,
        priceAsc: sortBy === 'price' ? priceAsc : undefined,
      });

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
   * 搜索
   */
  handleSearch(e) {
    const keyword = e.detail.value;
    this.setData({ keyword, page: 1, list: [], hasMore: true });
    this.loadList();
  },

  /**
   * 排序切换
   */
  handleSortChange(e) {
    const { sort } = e.currentTarget.dataset;
    const { sortBy, priceAsc } = this.data;

    // 价格排序支持升降序切换
    if (sort === 'price' && sortBy === 'price') {
      this.setData({ priceAsc: !priceAsc, page: 1, list: [], hasMore: true });
    } else {
      this.setData({ sortBy: sort, priceAsc: true, page: 1, list: [], hasMore: true });
    }

    this.loadList();
  },

  /**
   * 线路点击
   */
  handleRouteTap(e) {
    const route = e.detail?.route;
    const id = route?.id || e.currentTarget.dataset.id;
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
