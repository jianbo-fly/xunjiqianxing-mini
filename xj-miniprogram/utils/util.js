/**
 * Util - 通用工具函数
 */

/**
 * 防抖函数
 */
const debounce = (fn, delay = 300) => {
  let timer = null;
  return function (...args) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  };
};

/**
 * 节流函数
 */
const throttle = (fn, delay = 300) => {
  let lastTime = 0;
  return function (...args) {
    const now = Date.now();
    if (now - lastTime >= delay) {
      lastTime = now;
      fn.apply(this, args);
    }
  };
};

/**
 * 深拷贝
 */
const deepClone = (obj) => {
  if (obj === null || typeof obj !== 'object') return obj;
  if (obj instanceof Date) return new Date(obj);
  if (obj instanceof Array) return obj.map(item => deepClone(item));

  const cloned = {};
  for (const key in obj) {
    if (obj.hasOwnProperty(key)) {
      cloned[key] = deepClone(obj[key]);
    }
  }
  return cloned;
};

/**
 * 生成唯一ID
 */
const generateId = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
};

/**
 * 获取数据类型
 */
const getType = (value) => {
  return Object.prototype.toString.call(value).slice(8, -1).toLowerCase();
};

/**
 * 判断是否为空
 */
const isEmpty = (value) => {
  if (value === null || value === undefined) return true;
  if (typeof value === 'string') return value.trim() === '';
  if (Array.isArray(value)) return value.length === 0;
  if (typeof value === 'object') return Object.keys(value).length === 0;
  return false;
};

/**
 * 延迟执行
 */
const sleep = (ms) => {
  return new Promise(resolve => setTimeout(resolve, ms));
};

/**
 * 显示加载提示
 */
const showLoading = (title = '加载中...') => {
  wx.showLoading({ title, mask: true });
};

/**
 * 隐藏加载提示
 */
const hideLoading = () => {
  wx.hideLoading();
};

/**
 * 显示提示
 */
const showToast = (title, icon = 'none', duration = 2000) => {
  wx.showToast({ title, icon, duration });
};

/**
 * 显示成功提示
 */
const showSuccess = (title = '操作成功') => {
  wx.showToast({ title, icon: 'success' });
};

/**
 * 显示错误提示
 */
const showError = (title = '操作失败') => {
  wx.showToast({ title, icon: 'error' });
};

/**
 * 显示确认弹窗
 */
const showConfirm = (content, title = '提示') => {
  return new Promise((resolve) => {
    wx.showModal({
      title,
      content,
      success: (res) => {
        resolve(res.confirm);
      }
    });
  });
};

/**
 * rpx转px
 */
const rpx2px = (rpx) => {
  const systemInfo = wx.getSystemInfoSync();
  return rpx * systemInfo.windowWidth / 750;
};

/**
 * px转rpx
 */
const px2rpx = (px) => {
  const systemInfo = wx.getSystemInfoSync();
  return px * 750 / systemInfo.windowWidth;
};

module.exports = {
  debounce,
  throttle,
  deepClone,
  generateId,
  getType,
  isEmpty,
  sleep,
  showLoading,
  hideLoading,
  showToast,
  showSuccess,
  showError,
  showConfirm,
  rpx2px,
  px2rpx,
};
