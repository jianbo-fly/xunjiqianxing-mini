/**
 * 搭子列表页 (TabBar页面)
 * TODO: 待开发 - 用户结伴同行功能
 */

Page({
  data: {
    // 页面状态
    loading: false,
    // 搭子列表
    list: [],
  },

  onLoad() {
    // TODO: 加载搭子列表数据
  },

  onShow() {
    // 设置TabBar选中状态
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 1 });
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
   * 分享
   */
  onShareAppMessage() {
    return {
      title: '寻找旅途搭子',
      path: '/pages/companion/list/index',
    };
  },
});
