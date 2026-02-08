/**
 * 退款申请页
 */
const orderApi = require('../../../services/order');
const { navigateBack } = require('../../../utils/router');

Page({
  data: {
    // 订单ID
    orderId: '',
    // 订单信息
    order: null,
    // 退款金额文本
    refundAmountText: '',
    // 退款原因选项
    reasonOptions: [
      '行程有变，无法出行',
      '临时有事，无法出行',
      '身体原因，无法出行',
      '天气原因，无法出行',
      '其他原因',
    ],
    // 选中的原因索引
    selectedReasonIndex: -1,
    // 补充说明（选填）
    customReason: '',
    // 提交状态
    submitting: false,
    // 加载状态
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

      // 格式化出发日期 MM-DD
      let startDateText = '';
      if (order.startDate) {
        const d = new Date(order.startDate);
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        startDateText = `${month}-${day}`;
      }

      // 格式化出行人数
      let peopleText = '';
      const parts = [];
      if (order.adultCount > 0) parts.push(`成人×${order.adultCount}`);
      if (order.childCount > 0) parts.push(`儿童×${order.childCount}`);
      peopleText = parts.join(' ');

      // 退款金额：优先使用 refundRule.refundAmount，否则回退 payAmount
      const refundAmount = (order.refundRule && order.refundRule.refundAmount != null)
        ? order.refundRule.refundAmount
        : (order.payAmount || 0);

      this.setData({
        order: {
          ...order,
          payAmountText: (order.payAmount || 0).toFixed(2),
          startDateText,
          peopleText,
        },
        refundAmountText: refundAmount.toFixed(2),
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
   * 输入补充说明
   */
  handleCustomReasonInput(e) {
    this.setData({ customReason: e.detail.value });
  },

  /**
   * 提交退款申请
   */
  async handleSubmit() {
    const { orderId, selectedReasonIndex, reasonOptions, customReason, submitting } = this.data;

    if (submitting) return;

    // 验证：必须选择退款原因
    if (selectedReasonIndex < 0) {
      wx.showToast({ title: '请选择退款原因', icon: 'none' });
      return;
    }

    // 组装原因：选中的原因 + 补充说明（如有）
    let reason = reasonOptions[selectedReasonIndex];
    const extra = customReason.trim();
    if (extra) {
      reason = `${reason}：${extra}`;
    }

    this.setData({ submitting: true });

    try {
      wx.showLoading({ title: '提交中...' });
      await orderApi.refund(orderId, reason);
      wx.hideLoading();

      wx.showToast({ title: '申请已提交', icon: 'success' });

      // 返回上一页
      setTimeout(() => navigateBack(), 1500);

    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '提交失败', icon: 'none' });
      this.setData({ submitting: false });
    }
  },
});
