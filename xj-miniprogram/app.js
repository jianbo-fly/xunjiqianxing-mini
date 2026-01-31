// app.js
const ThemeManager = require('./config/theme');
const { STORAGE_KEYS } = require('./config/constants');

App({
  globalData: {
    userInfo: null,
    isLogin: false,
    systemInfo: null,
    themeManager: ThemeManager,
  },

  onLaunch() {
    // 初始化系统信息
    this.initSystemInfo();
    // 初始化主题
    this.initTheme();
    // 检查登录态
    this.checkLoginStatus();
    // 更新检查
    this.checkUpdate();
  },

  /**
   * 初始化系统信息
   */
  initSystemInfo() {
    try {
      const systemInfo = wx.getSystemInfoSync();
      const menuButton = wx.getMenuButtonBoundingClientRect();

      this.globalData.systemInfo = {
        ...systemInfo,
        menuButton,
        statusBarHeight: systemInfo.statusBarHeight,
        navBarHeight: menuButton.height + (menuButton.top - systemInfo.statusBarHeight) * 2,
        isIPhoneX: this.checkIsIPhoneX(systemInfo),
      };
    } catch (e) {
      console.error('获取系统信息失败', e);
    }
  },

  /**
   * 检查是否为iPhone X系列
   */
  checkIsIPhoneX(systemInfo) {
    const model = systemInfo.model || '';
    return /iPhone\s?(X|1[1-9]|[2-9]\d)/i.test(model) ||
           systemInfo.safeArea?.bottom < systemInfo.screenHeight;
  },

  /**
   * 初始化主题
   */
  initTheme() {
    ThemeManager.init();
  },

  /**
   * 检查登录状态
   */
  checkLoginStatus() {
    const token = wx.getStorageSync(STORAGE_KEYS.TOKEN);
    const userInfo = wx.getStorageSync(STORAGE_KEYS.USER_INFO);

    if (token && userInfo) {
      this.globalData.isLogin = true;
      this.globalData.userInfo = userInfo;
    }
  },

  /**
   * 设置登录信息
   */
  setLoginInfo(token, userInfo = null) {
    wx.setStorageSync(STORAGE_KEYS.TOKEN, token);
    if (userInfo) {
      wx.setStorageSync(STORAGE_KEYS.USER_INFO, userInfo);
      this.globalData.userInfo = userInfo;
    }
    this.globalData.isLogin = true;
  },

  /**
   * 清除登录信息
   */
  clearLoginInfo() {
    wx.removeStorageSync(STORAGE_KEYS.TOKEN);
    wx.removeStorageSync(STORAGE_KEYS.USER_INFO);
    this.globalData.isLogin = false;
    this.globalData.userInfo = null;
  },

  /**
   * 检查更新
   */
  checkUpdate() {
    if (!wx.canIUse('getUpdateManager')) return;

    const updateManager = wx.getUpdateManager();

    updateManager.onCheckForUpdate((res) => {
      if (res.hasUpdate) {
        console.log('发现新版本');
      }
    });

    updateManager.onUpdateReady(() => {
      wx.showModal({
        title: '更新提示',
        content: '新版本已经准备好，是否重启应用？',
        success: (res) => {
          if (res.confirm) {
            updateManager.applyUpdate();
          }
        }
      });
    });

    updateManager.onUpdateFailed(() => {
      console.log('新版本下载失败');
    });
  },
});
