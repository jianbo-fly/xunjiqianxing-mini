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
    // 退改规则时间轴当前步骤：0=出发前>7天，1=出发前1-7天，2=出发当天
    currentStep: 1,
    // 规则说明胶囊文字
    refundPolicyDesc: '',
    // 退款原因选项
    reasonOptions: [
      { label: '行程变更',      icon: '日', iconBg: '#ffedd5' },
      { label: '身体不适',      icon: '医', iconBg: '#dbeafe' },
      { label: '天气/交通原因', icon: '天', iconBg: '#ccfbf1' },
      { label: '其他原因',      icon: '他', iconBg: '#f3f4f6' },
    ],
    selectedReasonIndex: -1,
    // 补充说明
    remark: '',
    remarkCount: 0,
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

      // 格式化出发日期
      let startDateText = '';
      let daysToDepart = null;
      if (order.startDate) {
        const d = new Date(order.startDate);
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        startDateText = `${y}年${m}月${day}日`;
        // 计算距出发天数
        const now = new Date();
        now.setHours(0, 0, 0, 0);
        daysToDepart = Math.ceil((d - now) / (24 * 60 * 60 * 1000));
      }

      const payAmount = order.payAmount || 0;
      const refundRule = order.refundRule || {};
      const refundPercent = refundRule.refundPercent != null ? refundRule.refundPercent : 100;
      const refundAmount = refundRule.refundAmount != null
        ? refundRule.refundAmount
        : payAmount * refundPercent / 100;

      // 计算当前退改步骤
      let currentStep = 1;
      if (daysToDepart != null) {
        if (daysToDepart > 7) currentStep = 0;
        else if (daysToDepart >= 1) currentStep = 1;
        else currentStep = 2;
      } else if (refundPercent === 100) {
        currentStep = 0;
      } else if (refundPercent === 0) {
        currentStep = 2;
      }

      // 规则说明文字
      let refundPolicyDesc = refundRule.ruleDescription || '';
      if (!refundPolicyDesc && refundPercent < 100) {
        const deduct = 100 - refundPercent;
        if (daysToDepart != null && daysToDepart > 0) {
          refundPolicyDesc = `距离出发还有${daysToDepart}天，扣除${deduct}%手续费`;
        } else {
          refundPolicyDesc = `扣除${deduct}%手续费`;
        }
      }

      this.setData({
        order: {
          ...order,
          payAmountText: payAmount.toFixed(2),
          startDateText,
        },
        refundAmountText: refundAmount.toFixed(2),
        currentStep,
        refundPolicyDesc,
        loading: false,
      });
    } catch (err) {
      console.error('加载订单信息失败', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
      this.setData({ loading: false });
    }
  },

  /** 选择退款原因 */
  handleSelectReason(e) {
    const { index } = e.currentTarget.dataset;
    this.setData({ selectedReasonIndex: Number(index) });
  },

  /** 补充说明输入 */
  handleRemarkInput(e) {
    const remark = e.detail.value;
    this.setData({ remark, remarkCount: remark.length });
  },

  /** 上传凭证（选填） */
  handleUploadPhoto() {
    wx.chooseImage({
      count: 3,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: () => {
        wx.showToast({ title: '上传功能开发中', icon: 'none' });
      },
    });
  },

  /** 提交退款申请 */
  async handleSubmit() {
    const { orderId, selectedReasonIndex, reasonOptions, remark, submitting } = this.data;

    if (submitting) return;

    if (selectedReasonIndex < 0) {
      wx.showToast({ title: '请选择退款原因', icon: 'none' });
      return;
    }

    const reason = reasonOptions[selectedReasonIndex].label;
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
