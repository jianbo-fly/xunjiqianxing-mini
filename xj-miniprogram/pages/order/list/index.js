/**
 * 订单列表页
 */
const orderApi = require('../../../services/order');
const payApi = require('../../../services/pay');
const { go, redirectTo, routes } = require('../../../utils/router');
const { checkLogin } = require('../../../utils/auth');

Page({
  data: {
    // 页面状态
    loading: true,
    refreshing: false,
    loadingMore: false,
    // 订单列表
    list: [],
    // 分页
    page: 1,
    pageSize: 10,
    hasMore: true,
    // 当前tab状态
    currentStatus: '',
    // Tab列表
    tabs: [
      { status: '', label: '全部' },
      { status: '0', label: '待支付' },
      { status: '1', label: '待确认' },
      { status: '2', label: '已确认' },
      { status: '4', label: '已完成' },
    ],
    // 空状态
    isEmpty: false,
  },

  onLoad(options) {
    // 获取传入的状态参数
    if (options.status !== undefined) {
      this.setData({ currentStatus: options.status });
    }
  },

  onShow() {
    // 检查登录状态
    if (!checkLogin()) return;
    // 刷新列表
    this.refreshList();
  },

  onPullDownRefresh() {
    this.refreshList();
  },

  onReachBottom() {
    this.loadMore();
  },

  /**
   * 刷新列表
   */
  async refreshList() {
    this.setData({
      page: 1,
      hasMore: true,
      refreshing: true,
    });

    await this.loadList(true);

    this.setData({ refreshing: false });
    wx.stopPullDownRefresh();
  },

  /**
   * 加载更多
   */
  async loadMore() {
    if (this.data.loadingMore || !this.data.hasMore) return;

    this.setData({
      loadingMore: true,
      page: this.data.page + 1,
    });

    await this.loadList(false);

    this.setData({ loadingMore: false });
  },

  /**
   * 加载订单列表
   */
  async loadList(isRefresh = false) {
    try {
      const { currentStatus, page, pageSize } = this.data;

      const params = { page, pageSize };
      if (currentStatus !== '') {
        params.status = parseInt(currentStatus);
      }

      const result = await orderApi.getList(params);
      const newList = (result.list || []).map(item => this.formatOrder(item));

      this.setData({
        list: isRefresh ? newList : [...this.data.list, ...newList],
        hasMore: newList.length >= pageSize,
        loading: false,
        isEmpty: isRefresh && newList.length === 0,
      });
    } catch (err) {
      console.error('加载订单列表失败', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
      this.setData({ loading: false });
    }
  },

  /**
   * 格式化订单数据
   */
  formatOrder(order) {
    return {
      ...order,
      // 格式化日期
      startDateText: this.formatDate(order.startDate),
      createdAtText: this.formatDateTime(order.createdAt),
      // 格式化金额
      payAmountText: (order.payAmount || 0).toFixed(2),
      // 人数文本
      peopleText: this.getPeopleText(order.adultCount, order.childCount),
      // 按钮状态
      showPayBtn: order.status === 0,
      showCancelBtn: order.status === 0,
      showRefundBtn: [1, 2, 3].includes(order.status),
      showBuyAgainBtn: [4, 5, 7, 8].includes(order.status),
    };
  },

  /**
   * 格式化日期
   */
  formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    return `${month}月${day}日`;
  },

  /**
   * 格式化日期时间
   */
  formatDateTime(dateStr) {
    if (!dateStr) return '';
    return dateStr.replace('T', ' ').substring(0, 16);
  },

  /**
   * 获取人数文本
   */
  getPeopleText(adultCount, childCount) {
    const parts = [];
    if (adultCount > 0) parts.push(`${adultCount}成人`);
    if (childCount > 0) parts.push(`${childCount}儿童`);
    return parts.join(' ');
  },

  /**
   * Tab切换
   */
  handleTabChange(e) {
    const { status } = e.currentTarget.dataset;
    if (status === this.data.currentStatus) return;

    this.setData({
      currentStatus: status,
      page: 1,
      list: [],
      loading: true,
      isEmpty: false,
    });

    this.loadList(true);
  },

  /**
   * 订单点击
   */
  handleOrderTap(e) {
    const { id } = e.currentTarget.dataset;
    go.orderDetail(id);
  },

  /**
   * 取消订单
   */
  handleCancel(e) {
    const { id } = e.currentTarget.dataset;

    wx.showModal({
      title: '提示',
      content: '确定要取消此订单吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '取消中...' });
            await orderApi.cancel(id);
            wx.hideLoading();
            wx.showToast({ title: '已取消', icon: 'success' });
            this.refreshList();
          } catch (err) {
            wx.hideLoading();
            wx.showToast({ title: err.message || '取消失败', icon: 'none' });
          }
        }
      },
    });
  },

  /**
   * 去支付
   */
  async handlePay(e) {
    const { id } = e.currentTarget.dataset;

    try {
      wx.showLoading({ title: '正在支付...' });

      // 创建支付
      const payParams = await payApi.createOrderPayment(id);
      wx.hideLoading();

      // 调起微信支付
      await payApi.wxPay(payParams);

      // 支付成功
      wx.showToast({ title: '支付成功', icon: 'success' });
      this.refreshList();

    } catch (err) {
      wx.hideLoading();

      if (err.code === -2) {
        // 用户取消支付
        wx.showToast({ title: '已取消支付', icon: 'none' });
      } else {
        wx.showToast({ title: err.message || '支付失败', icon: 'none' });
      }
    }
  },

  /**
   * 申请退款
   */
  handleRefund(e) {
    const { id } = e.currentTarget.dataset;
    go.orderRefund(id);
  },

  /**
   * 再次购买
   */
  handleBuyAgain(e) {
    const { productId } = e.currentTarget.dataset;
    if (productId) {
      go.routeDetail(productId);
    }
  },

  /**
   * 去逛逛（空状态）
   */
  goExplore() {
    go.home();
  },
});
