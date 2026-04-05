// app.js
const ThemeManager = require('./config/theme');
const { STORAGE_KEYS } = require('./config/constants');
const promoterApi = require('./services/promoter');

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
    // 接管原生 wx.showToast，统一走自定义 xj-toast 组件
    this.overrideShowToast();
  },

  onShow(options) {
    // 捕获扫码 scene（从后台唤起）
    this.handleScene(options);
  },

  handleScene(options) {
    if (!options) return;
    // options.scene 是微信场景值（数字）：1047=扫小程序码
    // 只有扫小程序码时才处理推广绑定
    if (options.scene !== 1047) return;
    // wxacode.getUnlimited 的自定义 scene 字符串在 options.query.scene 里
    const query = options.query || {};
    if (!query.scene) return;
    const promoCode = decodeURIComponent(query.scene);
    wx.setStorageSync('pendingPromoCode', promoCode);
    promoterApi.recordScan(promoCode).catch(() => {});
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
   * 接管 wx.showToast，将所有调用路由到自定义 xj-toast 组件
   * 避免原生 toast 与自定义 toast 同时弹出
   */
  overrideShowToast() {
    const _original = wx.showToast.bind(wx);
    wx.showToast = function (options = {}) {
      const pages = getCurrentPages();
      if (pages.length) {
        const page = pages[pages.length - 1];
        const toast = page.selectComponent && page.selectComponent('#xj-toast');
        if (toast) {
          const type = options.icon === 'success' ? 'success'
                     : options.icon === 'error' ? 'error'
                     : 'info';
          toast.show({
            message: options.title || '',
            type,
            duration: options.duration || 2000,
          });
          return;
        }
      }
      _original(options);
    };
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
