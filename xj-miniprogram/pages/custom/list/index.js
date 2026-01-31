/**
 * 我的定制列表
 */
const customApi = require('../../../services/custom');

Page({
  data: {
    list: [],
    loading: false,
    finished: false,
    page: 1,
    pageSize: 10,
  },

  onLoad() {
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

  async loadList(refresh = false) {
    if (this.data.loading || this.data.finished) return;

    if (refresh) {
      this.setData({ page: 1, list: [], finished: false });
    }

    this.setData({ loading: true });

    try {
      const res = await customApi.getList({
        page: this.data.page,
        pageSize: this.data.pageSize,
      });

      const list = res.list || res.records || [];

      this.setData({
        list: refresh ? list : [...this.data.list, ...list],
        page: this.data.page + 1,
        finished: list.length < this.data.pageSize,
      });
    } catch (e) {
      console.error('加载定制列表失败', e);
    } finally {
      this.setData({ loading: false });
    }
  },

  handleItemTap(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/custom/detail/index?id=${id}` });
  },

  handleCancel(e) {
    const { id } = e.currentTarget.dataset;
    wx.showModal({
      title: '提示',
      content: '确定取消该定制需求吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await customApi.cancel(id);
            wx.showToast({ title: '已取消', icon: 'success' });
            this.loadList(true);
          } catch (e) {
            wx.showToast({ title: '取消失败', icon: 'none' });
          }
        }
      }
    });
  },

  // 状态文案
  getStatusText(status) {
    const map = {
      0: '待处理',
      1: '已联系',
      2: '已完成',
      3: '已取消',
    };
    return map[status] || '未知';
  },
});
