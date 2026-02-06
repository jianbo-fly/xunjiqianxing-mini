/**
 * 常用出行人管理页
 */
const travelerApi = require('../../../services/traveler');

Page({
  data: {
    loading: true,
    travelers: [],
  },

  onLoad() {
    this.loadTravelers();
  },

  onShow() {
    // 从编辑页返回时刷新列表
    if (this.needRefresh) {
      this.loadTravelers();
      this.needRefresh = false;
    }
  },

  /**
   * 加载出行人列表
   */
  async loadTravelers() {
    this.setData({ loading: true });

    try {
      const travelers = await travelerApi.getList();
      this.setData({
        travelers: travelers || [],
        loading: false,
      });
    } catch (err) {
      console.error('加载出行人列表失败', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
      this.setData({ loading: false });
    }
  },

  /**
   * 下拉刷新
   */
  async onPullDownRefresh() {
    await this.loadTravelers();
    wx.stopPullDownRefresh();
  },

  /**
   * 添加出行人
   */
  handleAdd() {
    this.needRefresh = true;
    wx.navigateTo({
      url: '/pages/member/travelers/edit',
    });
  },

  /**
   * 编辑出行人
   */
  handleEdit(e) {
    const { id } = e.currentTarget.dataset;
    this.needRefresh = true;
    wx.navigateTo({
      url: `/pages/member/travelers/edit?id=${id}`,
    });
  },

  /**
   * 删除出行人
   */
  handleDelete(e) {
    const { id, name } = e.currentTarget.dataset;

    wx.showModal({
      title: '确认删除',
      content: `确定要删除出行人"${name}"吗？`,
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '删除中...' });
            await travelerApi.delete(id);
            wx.hideLoading();
            wx.showToast({ title: '删除成功', icon: 'success' });
            this.loadTravelers();
          } catch (err) {
            wx.hideLoading();
            wx.showToast({ title: err.message || '删除失败', icon: 'none' });
          }
        }
      },
    });
  },

  /**
   * 设为默认
   */
  async handleSetDefault(e) {
    const { id } = e.currentTarget.dataset;

    try {
      wx.showLoading({ title: '设置中...' });
      await travelerApi.setDefault(id);
      wx.hideLoading();
      wx.showToast({ title: '设置成功', icon: 'success' });
      this.loadTravelers();
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '设置失败', icon: 'none' });
    }
  },
});
