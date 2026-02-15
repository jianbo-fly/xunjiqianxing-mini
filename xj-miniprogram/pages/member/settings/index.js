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
      version: appConfig.version || '1.0.2',
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
      const sizeText = kb > 1024 ? `${(kb / 1024).toFixed(1)} MB` : `${kb} KB`;
      this.setData({ cacheSize: sizeText });
    } catch (e) {
      this.setData({ cacheSize: '0 KB' });
    }
  },

  /**
   * 个人资料
   */
  handleProfile() {
    go.profile();
  },

  /**
   * 账号安全
   */
  handleAccountSecurity() {
    wx.showToast({ title: '账号安全即将上线', icon: 'none' });
  },

  /**
   * 消息推送设置
   */
  handleNotification() {
    wx.openSetting({
      success: () => {},
    });
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
   * 注销账号
   */
  handleDeleteAccount() {
    wx.showModal({
      title: '注销账号',
      content: '注销后账号数据将无法恢复，确定要注销吗？',
      confirmColor: '#E74C3C',
      success: (res) => {
        if (res.confirm) {
          wx.showToast({ title: '注销功能即将上线', icon: 'none' });
        }
      },
    });
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
