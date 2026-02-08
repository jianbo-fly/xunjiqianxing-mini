/**
 * 推广员申请页
 */
const app = getApp();
const promoterApi = require('../../../services/promoter');
const { navigateBack } = require('../../../utils/router');

Page({
  data: {
    form: {
      realName: '',
      phone: '',
      reason: '',
    },
    submitting: false,
  },

  onLoad() {
    // 自动填充手机号
    const userInfo = app.globalData.userInfo || {};
    if (userInfo.phone) {
      this.setData({ 'form.phone': userInfo.phone });
    }
  },

  /**
   * 表单输入
   */
  handleInput(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({ [`form.${field}`]: e.detail.value });
  },

  /**
   * 提交申请
   */
  async handleSubmit() {
    const { form, submitting } = this.data;
    if (submitting) return;

    // 验证
    if (!form.realName.trim()) {
      wx.showToast({ title: '请输入真实姓名', icon: 'none' });
      return;
    }
    if (!/^1\d{10}$/.test(form.phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });

    try {
      wx.showLoading({ title: '提交中...' });
      await promoterApi.apply({
        realName: form.realName.trim(),
        phone: form.phone,
        reason: form.reason.trim(),
      });
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
