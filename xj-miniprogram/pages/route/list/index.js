/**
 * 线路列表页
 * TODO: 待完善 - 线路搜索与筛选功能
 */
const { go } = require('../../../utils/router');

Page({
  data: {
    // 页面状态
    loading: false,
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
  },

  onLoad(options) {
    // 获取传入的筛选参数
    if (options.categoryId) {
      this.setData({ categoryId: options.categoryId });
    }
    if (options.sortBy) {
      this.setData({ sortBy: options.sortBy });
    }
    if (options.keyword) {
      this.setData({ keyword: options.keyword });
    }
    // TODO: 加载线路列表
  },

  onPullDownRefresh() {
    // TODO: 下拉刷新
    wx.stopPullDownRefresh();
  },

  onReachBottom() {
    // TODO: 加载更多
  },

  /**
   * 搜索
   */
  handleSearch(e) {
    const keyword = e.detail.value;
    this.setData({ keyword, page: 1 });
    // TODO: 重新加载数据
  },

  /**
   * 筛选条件改变
   */
  handleFilterChange(e) {
    // TODO: 处理筛选
  },

  /**
   * 线路点击
   */
  handleRouteTap(e) {
    const { id } = e.currentTarget.dataset;
    go.routeDetail(id);
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
