/**
 * 退款申请页
 */
const orderApi = require('../../../services/order');
const { navigateBack } = require('../../../utils/router');

Page({
  data: {
    orderId: '',
    order: null,
    refundAmountText: '',
    deductAmountText: '',
    reasonOptions: [
      '行程变更',
      '身体原因',
      '同行人无法参加',
      '航班/车次取消',
      '其他',
    ],
    selectedReasonIndex: -1,
    submitting: false,
    loading: true,
  },

  onLoad(options) {
    const { orderId } = options;
    if (!orderId) {
      wx.showToast({ title: '订单不存在', icon: 'none' });
      setTimeout(() => navigateBack(), 1500);
      return;
    }

    this.setData({ orderId });
    this.loadOrderInfo();
  },

  /**
   * 加载订单信息
   */
  async loadOrderInfo() {
    try {
      const order = await orderApi.getDetail(this.data.orderId);

      // 格式化出发日期 YYYY-MM-DD
      let startDateText = '';
      if (order.startDate) {
        const d = new Date(order.startDate);
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        startDateText = `${y}-${m}-${day}`;
      }

      const payAmount = order.payAmount || 0;

      // 退款金额计算
      const refundRule = order.refundRule || {};
      const refundAmount = refundRule.refundAmount != null
        ? refundRule.refundAmount
        : payAmount;
      const deductAmount = payAmount - refundAmount;

      this.setData({
        order: {
          ...order,
          payAmountText: payAmount.toFixed(2),
          startDateText,
        },
        refundAmountText: refundAmount.toFixed(2),
        deductAmountText: deductAmount > 0 ? deductAmount.toFixed(2) : '0.00',
        loading: false,
      });
    } catch (err) {
      console.error('加载订单信息失败', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
      this.setData({ loading: false });
    }
  },

  /**
   * 选择退款原因
   */
  handleSelectReason(e) {
    const { index } = e.currentTarget.dataset;
    this.setData({ selectedReasonIndex: index });
  },

  /**
   * 提交退款申请
   */
  async handleSubmit() {
    const { orderId, selectedReasonIndex, reasonOptions, submitting } = this.data;

    if (submitting) return;

    if (selectedReasonIndex < 0) {
      wx.showToast({ title: '请选择退款原因', icon: 'none' });
      return;
    }

    const reason = reasonOptions[selectedReasonIndex];
    this.setData({ submitting: true });

    try {
      wx.showLoading({ title: '提交中...' });
      await orderApi.refund(orderId, reason);
      wx.hideLoading();

      wx.showToast({ title: '申请已提交', icon: 'success' });
      setTimeout(() => navigateBack(), 1500);
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '提交失败', icon: 'none' });
      this.setData({ submitting: false });
    }
  },
});
