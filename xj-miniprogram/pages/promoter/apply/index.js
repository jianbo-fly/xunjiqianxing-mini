/**
 * 推广员申请页
 */
const app = getApp();
const promoterApi = require('../../../services/promoter');
const { navigateBack, redirectTo, routes } = require('../../../utils/router');

Page({
  data: {
    statusBarHeight: 0,
    navBarHeight: 44,
    form: {
      avatarUrl: '',
      nickname: '',
    },
    agreed: false,
    submitting: false,
  },

  onLoad() {
    const windowInfo = wx.getWindowInfo();
    const deviceInfo = wx.getDeviceInfo();
    const statusBarHeight = windowInfo.statusBarHeight || 0;
    const navBarHeight = deviceInfo.platform === 'ios' ? 44 : 48;
    this.setData({ statusBarHeight, navBarHeight });

    this.checkExistingApplication();
  },

  async checkExistingApplication() {
    try {
      const info = await promoterApi.getInfo();
      if (info && info.status === 0) {
        wx.redirectTo({ url: '/pages/promoter/status/index' });
        return;
      }
      if (info && info.status === 1) {
        wx.redirectTo({ url: '/pages/promoter/center/index' });
        return;
      }
    } catch (e) {
      // 未申请过，继续展示申请表单
    }

    // 预填用户信息
    const userInfo = app.globalData.userInfo || wx.getStorageSync('userInfo') || {};
    if (userInfo.nickname && userInfo.nickname !== '微信用户') {
      this.setData({ 'form.nickname': userInfo.nickname });
    }
    if (userInfo.avatar) {
      this.setData({ 'form.avatarUrl': userInfo.avatar });
    }
  },

  handleBack() {
    navigateBack();
  },

  /**
   * 选择头像（微信原生头像选择器）
   */
  handleChooseAvatar(e) {
    const avatarUrl = e.detail.avatarUrl;
    if (!avatarUrl) return;
    this.setData({ 'form.avatarUrl': avatarUrl });
  },

  /**
   * 表单输入
   */
  handleInput(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({ [`form.${field}`]: e.detail.value });
  },

  /**
   * 切换协议勾选
   */
  handleToggleAgree() {
    this.setData({ agreed: !this.data.agreed });
  },

  /**
   * 提交申请
   */
  async handleSubmit() {
    const { form, agreed, submitting } = this.data;
    if (submitting) return;
    if (!agreed) {
      wx.showToast({ title: '请先阅读并同意推广员协议', icon: 'none' });
      return;
    }
    if (!form.nickname.trim()) {
      wx.showToast({ title: '请输入昵称', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });

    try {
      wx.showLoading({ title: '提交中...' });
      await promoterApi.apply({
        nickname: form.nickname.trim(),
        avatarUrl: form.avatarUrl,
      });
      wx.hideLoading();

      wx.showToast({ title: '申请已提交', icon: 'success' });
      setTimeout(() => redirectTo(routes.promoterStatus), 1500);
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '提交失败', icon: 'none' });
      this.setData({ submitting: false });
    }
  },
});
