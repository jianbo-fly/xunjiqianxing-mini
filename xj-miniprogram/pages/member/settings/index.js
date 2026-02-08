/**
 * 设置页
 */
const app = getApp();
const userApi = require('../../../services/user');
const { isLogin } = require('../../../utils/auth');
const { go } = require('../../../utils/router');
const appConfig = require('../../../config/app.config');

Page({
  data: {
    isLogin: false,
    version: '',
    cacheSize: '0KB',
  },

  onLoad() {
    this.setData({
      isLogin: isLogin(),
      version: appConfig.version || '1.0.0',
    });
    this.calcCacheSize();
  },

  /**
   * 计算缓存大小
   */
  calcCacheSize() {
    try {
      const res = wx.getStorageInfoSync();
      const kb = res.currentSize || 0;
      const sizeText = kb > 1024 ? `${(kb / 1024).toFixed(1)}MB` : `${kb}KB`;
      this.setData({ cacheSize: sizeText });
    } catch (e) {
      this.setData({ cacheSize: '0KB' });
    }
  },

  /**
   * 清除缓存
   */
  handleClearCache() {
    wx.showModal({
      title: '提示',
      content: '确定要清除缓存吗？',
      success: (res) => {
        if (res.confirm) {
          // 保留登录信息
          const token = wx.getStorageSync('token');
          const userInfo = wx.getStorageSync('userInfo');
          wx.clearStorageSync();
          if (token) wx.setStorageSync('token', token);
          if (userInfo) wx.setStorageSync('userInfo', userInfo);

          this.calcCacheSize();
          wx.showToast({ title: '清除成功', icon: 'success' });
        }
      },
    });
  },

  /**
   * 关于我们
   */
  handleAbout() {
    go.webview(appConfig.aboutUrl || '', '关于我们');
  },

  /**
   * 隐私协议
   */
  handlePrivacy() {
    go.webview(appConfig.privacyUrl || '', '隐私协议');
  },

  /**
   * 用户协议
   */
  handleUserAgreement() {
    go.webview(appConfig.userAgreementUrl || '', '用户协议');
  },

  /**
   * 退出登录
   */
  handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await userApi.logout();
          } catch (e) {
            // 忽略
          }
          app.clearLoginInfo();
          this.setData({ isLogin: false });
          wx.showToast({ title: '已退出登录', icon: 'success' });
          setTimeout(() => go.home(), 1500);
        }
      },
    });
  },
});
