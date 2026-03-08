/**
 * 我的定制列表
 * 对齐 Figma Node: 76-9523
 */
const customApi = require('../../../services/custom');
const { go } = require('../../../utils/router');

// 状态配置（对齐 Figma 设计）
const STATUS_CONFIG = {
  0: {
    text: '待处理',
    class: 'pending',
    iconBg: '#EFF6FF',
    iconPath: '/assets/icons/custom-list/cl-dest-blue.png',
    ctaText: '查看详情',
    ctaClass: 'outline-primary',
  },
  1: {
    text: '跟进中',
    class: 'following',
    iconBg: '#FFF7ED',
    iconPath: '/assets/icons/custom-list/cl-dest-warm.png',
    ctaText: '查看详情',
    ctaClass: 'solid',
  },
  2: {
    text: '已完成',
    class: 'completed',
    iconBg: '#F0FDF4',
    iconPath: '/assets/icons/custom-list/cl-dest-green.png',
    ctaText: '查看回顾',
    ctaClass: 'outline-gray',
  },
};

// Tab 对应的 status 过滤值（0=全部，无过滤）
const TAB_STATUS = [null, 0, 1, 2];

Page({
  data: {
    statusBarHeight: 44,
    navBarTotalHeight: 88,
    tabsHeight: 44,

    // Tabs
    tabs: ['全部', '待处理', '跟进中', '已完成'],
    currentTab: 0,

    // 列表
    list: [],
    loading: false,
    finished: false,
    page: 1,
    pageSize: 10,
  },

  onLoad(options) {
    // 支持从外部页面带 tab 参数跳转（如会员页点击状态按钮）
    const initialTab = options && options.tab ? parseInt(options.tab) : 0;
    if (initialTab > 0) {
      this.setData({ currentTab: initialTab });
    }

    // 计算导航栏高度
    try {
      const sysInfo = wx.getSystemInfoSync();
      const statusBarHeight = sysInfo.statusBarHeight || 44;
      const menuButton = wx.getMenuButtonBoundingClientRect();
      const navBarHeight = menuButton.height + (menuButton.top - statusBarHeight) * 2;
      this.setData({
        statusBarHeight,
        navBarTotalHeight: statusBarHeight + navBarHeight,
      });
    } catch (e) {}

    this.loadList(true);
  },

  onPullDownRefresh() {
    this.loadList(true).then(() => {
      wx.stopPullDownRefresh();
    });
  },

  onReachBottom() {
    this.loadList();
  },

  /**
   * 切换 Tab
   */
  handleTabChange(e) {
    const index = e.currentTarget.dataset.index;
    if (index === this.data.currentTab) return;
    this.setData({ currentTab: index, page: 1, list: [], finished: false });
    this.loadList(true);
  },

  /**
   * 加载列表
   */
  async loadList(refresh = false) {
    if (this.data.loading || this.data.finished) return;

    if (refresh) {
      this.setData({ page: 1, list: [], finished: false });
    }

    this.setData({ loading: true });

    try {
      const statusFilter = TAB_STATUS[this.data.currentTab];
      const params = {
        page: this.data.page,
        pageSize: this.data.pageSize,
      };
      if (statusFilter !== null) {
        params.status = statusFilter;
      }

      const res = await customApi.getList(params);
      const rawList = res.list || res.records || [];

      // 预计算展示字段
      const list = rawList.map(item => this._processItem(item));

      this.setData({
        list: refresh ? list : [...this.data.list, ...list],
        page: this.data.page + 1,
        finished: rawList.length < this.data.pageSize,
      });
    } catch (e) {
      console.error('加载定制列表失败', e);
    } finally {
      this.setData({ loading: false });
    }
  },

  /**
   * 处理单条数据，补充展示字段
   */
  _processItem(item) {
    const status = item.status ?? 0;
    const cfg = STATUS_CONFIG[status] || STATUS_CONFIG[0];

    const totalPeople = (item.adultCount || 0) + (item.childCount || 0);
    const budgetDisplay = item.budget
      ? (item.budget.includes('¥') ? item.budget + '/人' : '¥' + item.budget + '/人')
      : '-';

    // 生成定制编号
    const orderNo = item.orderNo || item.no || ('XJ-' + String(item.id).toUpperCase());

    // 提交时间展示
    const createdAtDisplay = item.createdAt
      ? '提交于 ' + item.createdAt
      : '';

    return {
      ...item,
      status,
      statusText: cfg.text,
      statusClass: cfg.class,
      iconBg: cfg.iconBg,
      iconPath: cfg.iconPath,
      ctaText: cfg.ctaText,
      ctaClass: cfg.ctaClass,
      totalPeople,
      budgetDisplay,
      orderNo,
      createdAtDisplay,
    };
  },

  /**
   * 点击卡片（整体跳转详情）
   */
  handleCardTap(e) {
    const { id } = e.currentTarget.dataset;
    if (id) wx.navigateTo({ url: `/pages/custom/detail/index?id=${id}` });
  },

  /**
   * 点击 CTA 按钮（阻止卡片冒泡）
   */
  handleCtaTap(e) {
    const { id } = e.currentTarget.dataset;
    if (id) wx.navigateTo({ url: `/pages/custom/detail/index?id=${id}` });
  },

  /**
   * FAB：新建定制游
   */
  handleCreateNew() {
    wx.navigateTo({ url: '/pages/travel/index/index?tab=1' });
  },
});
