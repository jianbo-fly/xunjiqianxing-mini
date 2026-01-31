/**
 * 行程列表页 (TabBar页面)
 * TODO: 待开发 - 用户行程管理功能
 */

Page({
  data: {
    // 页面状态
    loading: false,
    // 行程列表
    list: [],
    // 当前tab: upcoming/ongoing/completed
    currentTab: 'upcoming',
  },

  onLoad() {
    // TODO: 加载行程列表数据
  },

  onShow() {
    // 设置TabBar选中状态
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 });
    }
  },

  onPullDownRefresh() {
    // TODO: 下拉刷新
    wx.stopPullDownRefresh();
  },

  onReachBottom() {
    // TODO: 加载更多
  },

  /**
   * Tab切换
   */
  handleTabChange(e) {
    const { tab } = e.currentTarget.dataset;
    this.setData({ currentTab: tab });
    // TODO: 重新加载数据
  },

  /**
   * 分享
   */
  onShareAppMessage() {
    return {
      title: '我的旅行行程',
      path: '/pages/trip/list/index',
    };
  },
});
