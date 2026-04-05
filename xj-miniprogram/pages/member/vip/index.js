/**
 * 会员中心 - 开通会员
 * 对齐 Figma Node: 74-6996
 */
const { checkLogin } = require('../../../utils/auth');
const payApi = require('../../../services/pay');
const userApi = require('../../../services/user');

Page({
  data: {
    statusBarHeight: 44,
    navBarTotalHeight: 88,
    agreed: true,
    activating: false,
    showSuccess: false,
  },

  onLoad() {
    try {
      const sysInfo = wx.getSystemInfoSync();
      const statusBarHeight = sysInfo.statusBarHeight || 44;
      const menuButton = wx.getMenuButtonBoundingClientRect();
      const navBarHeight = menuButton.height + (menuButton.top - statusBarHeight) * 2;
      this.setData({ statusBarHeight, navBarTotalHeight: statusBarHeight + navBarHeight });
    } catch (e) {}
  },

  /**
   * 切换协议勾选
   */
  handleAgreementToggle() {
    this.setData({ agreed: !this.data.agreed });
  },

  /**
   * 查看协议
   */
  handleTermsTap() {
    wx.showToast({ title: '协议内容即将上线', icon: 'none' });
  },

  /**
   * 常见问题
   */
  handleFaqTap() {
    wx.showToast({ title: '常见问题即将上线', icon: 'none' });
  },

  /**
   * 立即开通
   */
  async handleActivate() {
    if (!this.data.agreed) {
      wx.showToast({ title: '请先同意会员服务协议', icon: 'none' });
      return;
    }
    if (!checkLogin()) return;
    if (this.data.activating) return;

    this.setData({ activating: true });
    try {
      wx.showLoading({ title: '处理中...' });
      const res = await userApi.buyMember();
      const orderId = res.orderId || res.id;

      const payParams = await payApi.createMemberPayment(orderId);
      wx.hideLoading();

      await payApi.wxPay(payParams);

      this.setData({ showSuccess: true });
    } catch (err) {
      wx.hideLoading();
      if (err.code === -2) {
        wx.showToast({ title: '已取消，可稍后重新开通', icon: 'none' });
      } else {
        wx.showToast({ title: err.message || '开通失败，请重试', icon: 'none' });
      }
    } finally {
      this.setData({ activating: false });
    }
  },

  /**
   * 支付成功弹窗确认
   */
  handleSuccessConfirm() {
    this.setData({ showSuccess: false });
    wx.navigateBack();
  },
});
