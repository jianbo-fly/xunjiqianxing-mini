/**
 * Jest 全局 setup — 模拟微信小程序运行时环境
 */

// 模拟 wx 全局对象
global.wx = {
  // 网络
  request: jest.fn(),

  // 导航
  navigateTo: jest.fn(),
  navigateBack: jest.fn(),
  redirectTo: jest.fn(),
  switchTab: jest.fn(),
  reLaunch: jest.fn(),

  // UI
  showToast: jest.fn(),
  showModal: jest.fn().mockResolvedValue({ confirm: true }),
  showLoading: jest.fn(),
  hideLoading: jest.fn(),
  stopPullDownRefresh: jest.fn(),

  // 存储
  getStorageSync: jest.fn(),
  setStorageSync: jest.fn(),
  removeStorageSync: jest.fn(),

  // 系统
  getSystemInfoSync: jest.fn().mockReturnValue({
    statusBarHeight: 44,
    windowHeight: 812,
    windowWidth: 375,
    platform: 'devtools',
  }),
  getMenuButtonBoundingClientRect: jest.fn().mockReturnValue({
    top: 48,
    height: 32,
    right: 375,
  }),

  // 登录
  login: jest.fn().mockResolvedValue({ code: 'mock-code' }),
  getUserProfile: jest.fn(),

  // 支付
  requestPayment: jest.fn(),

  // 图片
  saveImageToTempFilePath: jest.fn(),
  previewImage: jest.fn(),
  getFileSystemManager: jest.fn().mockReturnValue({
    writeFile: jest.fn(),
    readFile: jest.fn(),
  }),

  // 分享
  showShareMenu: jest.fn(),

  // 画布
  createSelectorQuery: jest.fn().mockReturnValue({
    select: jest.fn().mockReturnValue({
      boundingClientRect: jest.fn().mockReturnValue({
        exec: jest.fn(),
      }),
    }),
  }),
};

// 模拟 getApp
global.getApp = jest.fn().mockReturnValue({
  globalData: {
    userInfo: null,
    token: null,
  },
});

// Page 辅助：捕获 Page 配置对象，供测试直接调用方法
global.__pageConfigs = {};
global.Page = jest.fn((config) => {
  const name = new Error().stack.match(/at Object\.<anonymous> \((.+?)\)/)?.[1] || 'unknown';
  global.__lastPageConfig = config;
  return config;
});

// App / Component 模拟
global.App = jest.fn((config) => config);
global.Component = jest.fn((config) => config);

// getCurrentPages
global.getCurrentPages = jest.fn().mockReturnValue([]);
