/**
 * 搜索页
 */
const routeApi = require('../../services/route');
const { go } = require('../../utils/router');

const HISTORY_KEY = 'search_history';
const MAX_HISTORY = 10;

// 默认热门搜索词
const DEFAULT_HOT_LIST = [
  '云南', '西藏', '三亚', '桂林', '张家界',
  '黄山', '厦门', '成都', '九寨沟', '丽江',
];

Page({
  data: {
    keyword: '',
    autoFocus: true,
    // 默认态
    historyList: [],
    hotList: DEFAULT_HOT_LIST,
    // 结果态
    showResult: false,
    searching: false,
    resultList: [],
    total: 0,
    sortType: 'default',
    // 分页
    page: 1,
    pageSize: 10,
    hasMore: false,
    loadingMore: false,
  },

  // 搜索防抖定时器
  _searchTimer: null,

  onLoad() {
    this._loadHistory();
  },

  /**
   * 从本地读取搜索历史
   */
  _loadHistory() {
    try {
      const raw = wx.getStorageSync(HISTORY_KEY);
      this.setData({ historyList: raw ? JSON.parse(raw) : [] });
    } catch (e) {
      this.setData({ historyList: [] });
    }
  },

  /**
   * 保存关键词到历史记录
   */
  _saveHistory(keyword) {
    if (!keyword) return;
    let list = this.data.historyList.filter(k => k !== keyword);
    list.unshift(keyword);
    if (list.length > MAX_HISTORY) list = list.slice(0, MAX_HISTORY);
    wx.setStorageSync(HISTORY_KEY, JSON.stringify(list));
    this.setData({ historyList: list });
  },

  /**
   * 输入框变化
   */
  handleInput(e) {
    const keyword = e.detail.value;
    this.setData({ keyword });

    // 清空时回到默认态
    if (!keyword.trim()) {
      this.setData({ showResult: false, resultList: [], total: 0 });
    }
  },

  /**
   * 点击搜索（键盘确认）
   */
  handleSearch() {
    const keyword = this.data.keyword.trim();
    if (!keyword) return;
    this._doSearch(keyword, true);
  },

  /**
   * 点击标签（历史/热门）
   */
  handleTagTap(e) {
    const keyword = e.currentTarget.dataset.keyword;
    this.setData({ keyword });
    this._doSearch(keyword, true);
  },

  /**
   * 清空输入
   */
  handleClear() {
    this.setData({ keyword: '', showResult: false, resultList: [], total: 0 });
  },

  /**
   * 取消，返回上一页
   */
  handleCancel() {
    wx.navigateBack({ delta: 1 });
  },

  /**
   * 清空历史记录
   */
  handleClearHistory() {
    wx.showModal({
      title: '提示',
      content: '确认清空搜索历史？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync(HISTORY_KEY);
          this.setData({ historyList: [] });
        }
      },
    });
  },

  /**
   * 切换排序
   */
  handleSort(e) {
    const { type } = e.currentTarget.dataset;
    if (type === this.data.sortType) return;
    this.setData({ sortType: type });
    if (this.data.keyword.trim()) {
      this._doSearch(this.data.keyword, true);
    }
  },

  /**
   * 筛选（预留）
   */
  handleFilter() {
    wx.showToast({ title: '筛选功能即将上线', icon: 'none' });
  },

  /**
   * 线路点击
   */
  handleRouteTap(e) {
    const { id } = e.currentTarget.dataset;
    if (id) go.routeDetail(id);
  },

  /**
   * 加载更多
   */
  async handleLoadMore() {
    if (!this.data.hasMore || this.data.loadingMore) return;
    const nextPage = this.data.page + 1;
    this.setData({ loadingMore: true, page: nextPage });
    await this._fetchResult(this.data.keyword, nextPage, false);
    this.setData({ loadingMore: false });
  },

  /**
   * 执行搜索
   */
  async _doSearch(keyword, reset) {
    this._saveHistory(keyword);
    this.setData({ showResult: true, searching: true, page: 1, resultList: [], total: 0, hasMore: false });
    await this._fetchResult(keyword, 1, reset);
    this.setData({ searching: false });
  },



  /**
   * 请求搜索结果
   */
  async _fetchResult(keyword, page, reset) {
    try {
      const res = await routeApi.getList({
        keyword,
        page,
        pageSize: this.data.pageSize,
        sortType: this.data.sortType,
      });
      const list = res.records || res.list || [];
      const total = res.total || 0;
      const hasMore = (page * this.data.pageSize) < total;
      this.setData({
        resultList: reset ? list : [...this.data.resultList, ...list],
        total,
        hasMore,
      });
    } catch (e) {
      console.error('搜索失败', e);
      wx.showToast({ title: '搜索失败，请重试', icon: 'none' });
    }
  },
});
