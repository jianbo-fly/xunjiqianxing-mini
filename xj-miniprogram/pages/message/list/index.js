/**
 * 消息中心
 */
const messageApi = require('../../../services/message');

// Tab 对应的 category 参数
const TAB_CATEGORY = ['', 'order', 'system'];

// 消息类型 → 图标类型映射
const TYPE_ICON_MAP = {
  order_confirm: 'order',
  order_cancel: 'order',
  travel_reminder: 'travel',
  refund_success: 'refund',
  refund_reject: 'refund',
  payment_success: 'payment',
  promo: 'promo',
  system: 'system',
};

// 图标类型 → 图片路径
const MSG_ICON_PATH = {
  order:   '/assets/icons/message/msg-order.png',
  travel:  '/assets/icons/message/msg-travel.png',
  refund:  '/assets/icons/message/msg-refund.png',
  payment: '/assets/icons/message/msg-payment.png',
  promo:   '/assets/icons/message/msg-payment.png',
  system:  '/assets/icons/message/msg-order.png',
};

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

  onLoad() {
    try {
      const sys = wx.getSystemInfoSync();
      const sb = sys.statusBarHeight || 44;
      const mb = wx.getMenuButtonBoundingClientRect();
      this.setData({ navBarHeight: sb + mb.height + (mb.top - sb) * 2 });
    } catch (e) {}

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
      const category = TAB_CATEGORY[activeTab];
      const params = { page, pageSize };
      if (category) params.category = category;

      const res = await messageApi.getList(params);
      const rawList = res.list || res.records || [];
      const list = rawList.map(item => {
        const iconType = TYPE_ICON_MAP[item.type] || (item.category === 'order' ? 'order' : 'system');
        return {
          ...item,
          iconType,
          iconPath: MSG_ICON_PATH[iconType] || MSG_ICON_PATH.system,
          read: !!item.read,
        };
      });
      this.setData({
        list: refresh ? list : [...this.data.list, ...list],
        page: page + 1,
        finished: rawList.length < pageSize,
      });
    } catch (e) {
      console.error('加载消息失败', e);
    } finally {
      this.setData({ loading: false });
    }
  },

  async handleMsgTap(e) {
    const { id, link } = e.currentTarget.dataset;
    // 标记已读（乐观更新）
    const idx = this.data.list.findIndex(m => m.id === id || m.id === String(id));
    if (idx >= 0 && !this.data.list[idx].read) {
      this.setData({ [`list[${idx}].read`]: true });
      messageApi.markRead(id).catch(() => {});
    }
    if (link) wx.navigateTo({ url: link, fail: () => {} });
  },

  async handleMarkAllRead() {
    try {
      await messageApi.markRead('all');
      const list = this.data.list.map(m => ({ ...m, read: true }));
      this.setData({ list });
      wx.showToast({ title: '已全部已读', icon: 'success' });
    } catch (e) {
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  },
});
