/**
 * 我的优惠券
 */
const couponApi = require('../../../services/coupon');

Page({
  data: {
    navBarHeight: 88,
    activeTab: 0,
    list: [],
    loading: false,
    finished: false,
    page: 1,
    pageSize: 20,
  },

  onLoad(options) {
    try {
      const sys = wx.getSystemInfoSync();
      const sb = sys.statusBarHeight || 44;
      const mb = wx.getMenuButtonBoundingClientRect();
      this.setData({ navBarHeight: sb + mb.height + (mb.top - sb) * 2 });
    } catch (e) {}

    if (options.tab) this.setData({ activeTab: parseInt(options.tab) || 0 });
    this.loadList(true);
  },

  onPullDownRefresh() {
    this.loadList(true).then(() => wx.stopPullDownRefresh());
  },

  onReachBottom() {
    this.loadList();
  },

  handleTabChange(e) {
    const tab = parseInt(e.currentTarget.dataset.tab);
    if (tab === this.data.activeTab) return;
    this.setData({ activeTab: tab, list: [], finished: false, page: 1 });
    this.loadList(true);
  },

  async loadList(refresh = false) {
    if (this.data.loading || (!refresh && this.data.finished)) return;
    this.setData({ loading: true });
    try {
      const { activeTab, page, pageSize } = this.data;
      const res = await couponApi.getMy({ status: activeTab, page, pageSize });
      const list = (res.list || res.records || []).map(item => ({
        ...item,
        expireDate: item.expireDate || item.expiredAt || '',
        minAmount: item.minAmount || item.minOrderAmount || 0,
      }));
      this.setData({
        list: refresh ? list : [...this.data.list, ...list],
        page: page + 1,
        finished: list.length < pageSize,
      });
    } catch (e) {
      console.error('加载优惠券失败', e);
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  onShareAppMessage() {
    return { title: '寻迹千行优惠券', path: '/pages/member/coupon/index' };
  },
});
