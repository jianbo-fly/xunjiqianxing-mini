/**
 * 订单列表页
 * TODO: 待完善 - 订单管理功能
 */
const { go } = require('../../../utils/router');
const { checkLogin } = require('../../../utils/auth');

Page({
  data: {
    // 页面状态
    loading: false,
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
      { status: '4', label: '待评价' },
    ],
  },

  onLoad(options) {
    // 获取传入的状态参数
    if (options.status !== undefined) {
      this.setData({ currentStatus: options.status });
    }
    // TODO: 加载订单列表
  },

  onShow() {
    // 检查登录状态
    if (!checkLogin()) return;
    // TODO: 刷新列表
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
    const { status } = e.currentTarget.dataset;
    this.setData({
      currentStatus: status,
      page: 1,
      list: [],
    });
    // TODO: 重新加载数据
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
    // TODO: 取消订单
  },

  /**
   * 去支付
   */
  handlePay(e) {
    const { id } = e.currentTarget.dataset;
    // TODO: 去支付
  },

  /**
   * 申请退款
   */
  handleRefund(e) {
    const { id } = e.currentTarget.dataset;
    // TODO: 申请退款
  },
});
