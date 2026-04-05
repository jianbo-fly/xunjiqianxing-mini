/**
 * 关于我们页
 */
const { go } = require('../../../utils/router');

Page({
  data: {
    version: '1.0.0',
  },

  onLoad() {
    // 读取小程序版本号
    try {
      const accountInfo = wx.getAccountInfoSync();
      const version = accountInfo.miniProgram.version || '1.0.0';
      this.setData({ version });
    } catch (e) {
      // 开发版无法获取版本号，保持默认值
    }
  },

  /** 拨打电话 */
  handleCallPhone() {
    wx.makePhoneCall({ phoneNumber: '4001234567' });
  },

  /** 在线客服 */
  handleCustomerService() {
    wx.openCustomerServiceChat({
      extInfo: { url: '' },
      corpId: '',
      fail() {
        wx.showToast({ title: '客服暂时不可用', icon: 'none' });
      },
    });
  },

  /** 复制邮箱 */
  handleCopyEmail() {
    wx.setClipboardData({
      data: 'business@xunji.com',
      success() {
        wx.showToast({ title: '邮箱已复制', icon: 'success' });
      },
    });
  },

  /** 用户协议 */
  handleAgreement() {
    go.webview && go.webview('agreement');
  },

  /** 隐私政策 */
  handlePrivacy() {
    go.webview && go.webview('privacy');
  },
});
