/**
 * Auth - 登录态管理
 */

const { STORAGE_KEYS } = require('../config/constants');

/**
 * 获取Token
 */
const getToken = () => {
  return wx.getStorageSync(STORAGE_KEYS.TOKEN) || '';
};

/**
 * 设置Token
 */
const setToken = (token) => {
  wx.setStorageSync(STORAGE_KEYS.TOKEN, token);
};

/**
 * 清除Token
 */
const clearToken = () => {
  wx.removeStorageSync(STORAGE_KEYS.TOKEN);
};

/**
 * 获取用户信息
 */
const getUserInfo = () => {
  return wx.getStorageSync(STORAGE_KEYS.USER_INFO) || null;
};

/**
 * 设置用户信息
 */
const setUserInfo = (userInfo) => {
  wx.setStorageSync(STORAGE_KEYS.USER_INFO, userInfo);
};

/**
 * 清除用户信息
 */
const clearUserInfo = () => {
  wx.removeStorageSync(STORAGE_KEYS.USER_INFO);
};

/**
 * 检查是否登录
 */
const isLogin = () => {
  const token = getToken();
  return !!token;
};

/**
 * 清除所有登录信息
 */
const clearAll = () => {
  clearToken();
  clearUserInfo();
};

/**
 * 登录检查（未登录跳转登录页）
 * @param {string} redirectUrl - 登录后跳转的页面
 * @returns {boolean} - 是否已登录
 */
const checkLogin = (redirectUrl = '') => {
  if (isLogin()) {
    return true;
  }

  // 保存当前页面路径
  if (redirectUrl) {
    wx.setStorageSync('redirectUrl', redirectUrl);
  } else {
    const pages = getCurrentPages();
    if (pages.length > 0) {
      const currentPage = pages[pages.length - 1];
      const url = `/${currentPage.route}`;
      wx.setStorageSync('redirectUrl', url);
    }
  }

  // 跳转登录
  wx.navigateTo({
    url: '/pages/login/index'
  });

  return false;
};

/**
 * 登录成功后跳转
 */
const redirectAfterLogin = () => {
  const redirectUrl = wx.getStorageSync('redirectUrl');
  wx.removeStorageSync('redirectUrl');

  if (redirectUrl) {
    // 判断是否是tabBar页面
    const tabBarPages = [
      '/pages/index/index',
      '/pages/companion/list/index',
      '/pages/trip/list/index',
      '/pages/member/index/index'
    ];

    if (tabBarPages.includes(redirectUrl)) {
      wx.switchTab({ url: redirectUrl });
    } else {
      wx.redirectTo({ url: redirectUrl });
    }
  } else {
    wx.switchTab({ url: '/pages/index/index' });
  }
};

/**
 * 需要登录的装饰器（用于页面方法）
 */
const requireLogin = (fn) => {
  return function (...args) {
    if (checkLogin()) {
      return fn.apply(this, args);
    }
  };
};

module.exports = {
  getToken,
  setToken,
  clearToken,
  getUserInfo,
  setUserInfo,
  clearUserInfo,
  isLogin,
  clearAll,
  checkLogin,
  redirectAfterLogin,
  requireLogin,
};
