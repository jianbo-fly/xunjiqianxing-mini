/**
 * 推广明细页（扫码记录 + 下单记录）
 */
const promoterApi = require('../../../services/promoter');
const { navigateBack } = require('../../../utils/router');

Page({
  data: {
    statusBarHeight: 0,
    navBarHeight: 44,

    // Summary
    totalScan: 0,
    totalOrder: 0,
    totalPoints: 0,

    // Tabs
    activeTab: 0,

    // Scan list
    scanList: [],
    scanPage: 1,
    scanHasMore: true,
    loadingScan: false,

    // Order list
    orderList: [],
    orderPage: 1,
    orderHasMore: true,
    loadingOrder: false,
  },

  onLoad() {
    const windowInfo = wx.getWindowInfo();
    const deviceInfo = wx.getDeviceInfo();
    this.setData({
      statusBarHeight: windowInfo.statusBarHeight || 0,
      navBarHeight: deviceInfo.platform === 'ios' ? 44 : 48,
    });
    this.loadSummary();
    this.loadScanList(true);
  },

  async loadSummary() {
    try {
      const stats = await promoterApi.getStatistics();
      this.setData({
        totalScan: stats.scanCount || 0,
        totalOrder: stats.orderCount || 0,
        totalPoints: stats.points || 0,
      });
    } catch (err) {
      console.error('加载统计数据失败', err);
    }
  },

  handleBack() {
    navigateBack();
  },

  handleTabChange(e) {
    const index = +e.currentTarget.dataset.index;
    if (index === this.data.activeTab) return;
    this.setData({ activeTab: index });
    if (index === 0 && this.data.scanList.length === 0) {
      this.loadScanList(true);
    } else if (index === 1 && this.data.orderList.length === 0) {
      this.loadOrderList(true);
    }
  },

  // ===== Scan records =====

  async loadScanList(reset = false) {
    if (this.data.loadingScan) return;
    const page = reset ? 1 : this.data.scanPage;
    this.setData({ loadingScan: true });
    try {
      const result = await promoterApi.getScanList({ page, pageSize: 20 });
      const list = (result.list || []).map(item => ({
        ...item,
        scanTimeStr: formatTime(item.createdAt || item.scanTime),
        maskedPhone: maskPhone(item.phone),
      }));
      this.setData({
        scanList: reset ? list : [...this.data.scanList, ...list],
        scanPage: page + 1,
        scanHasMore: list.length >= 20,
        loadingScan: false,
      });
    } catch (err) {
      console.error('加载扫码记录失败', err);
      this.setData({ loadingScan: false });
    }
  },

  loadMoreScan() {
    if (!this.data.scanHasMore || this.data.loadingScan) return;
    this.loadScanList(false);
  },

  // ===== Order records =====

  async loadOrderList(reset = false) {
    if (this.data.loadingOrder) return;
    const page = reset ? 1 : this.data.orderPage;
    this.setData({ loadingOrder: true });
    try {
      const result = await promoterApi.getOrderList({ page, pageSize: 20 });
      const list = (result.list || []).map(item => ({
        ...item,
        orderDateStr: formatDate(item.createdAt || item.orderDate),
        maskedPhone: maskPhone(item.buyerPhone || item.phone),
      }));
      this.setData({
        orderList: reset ? list : [...this.data.orderList, ...list],
        orderPage: page + 1,
        orderHasMore: list.length >= 20,
        loadingOrder: false,
      });
    } catch (err) {
      console.error('加载下单记录失败', err);
      this.setData({ loadingOrder: false });
    }
  },

  loadMoreOrder() {
    if (!this.data.orderHasMore || this.data.loadingOrder) return;
    this.loadOrderList(false);
  },
});

// ===== Helpers =====

function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone || '未知';
  return phone.substring(0, 3) + '****' + phone.substring(phone.length - 4);
}

function formatTime(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  const pad = n => String(n).padStart(2, '0');
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  const pad = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
