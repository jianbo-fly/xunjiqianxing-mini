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
    // 其他原因（自定义）
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
      this.setData({
        order: {
          ...order,
          payAmountText: (order.payAmount || 0).toFixed(2),
        },
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
    this.setData({
      selectedReasonIndex: index,
      customReason: index === this.data.reasonOptions.length - 1 ? '' : '',
    });
  },

  /**
   * 输入自定义原因
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

    // 验证
    if (selectedReasonIndex < 0) {
      wx.showToast({ title: '请选择退款原因', icon: 'none' });
      return;
    }

    // 如果选择的是"其他原因"，需要填写具体原因
    const isOtherReason = selectedReasonIndex === reasonOptions.length - 1;
    if (isOtherReason && !customReason.trim()) {
      wx.showToast({ title: '请填写具体原因', icon: 'none' });
      return;
    }

    const reason = isOtherReason ? customReason.trim() : reasonOptions[selectedReasonIndex];

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
