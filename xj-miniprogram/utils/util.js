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
 * 获取当前页面的 xj-toast 组件实例（如页面已挂载）
 */
const _getToast = () => {
  const pages = getCurrentPages();
  if (!pages.length) return null;
  const page = pages[pages.length - 1];
  return page.selectComponent && page.selectComponent('#xj-toast');
};

/**
 * 显示提示（优先使用自定义 xj-toast，降级使用原生 wx.showToast）
 * @param {string} title    提示文字
 * @param {string} [type]   类型: info / success / error / warning
 * @param {number} [duration] 时长(ms)，默认 2000
 */
const showToast = (title, type = 'info', duration = 2000) => {
  const toast = _getToast();
  if (toast) {
    toast.show({ message: title, type, duration });
  } else {
    wx.showToast({ title, icon: 'none', duration });
  }
};

/**
 * 显示成功提示
 */
const showSuccess = (title = '操作成功') => {
  const toast = _getToast();
  if (toast) {
    toast.show({ message: title, type: 'success' });
  } else {
    wx.showToast({ title, icon: 'success' });
  }
};

/**
 * 显示错误提示
 */
const showError = (title = '操作失败') => {
  const toast = _getToast();
  if (toast) {
    toast.show({ message: title, type: 'error' });
  } else {
    wx.showToast({ title, icon: 'error' });
  }
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
 * 获取系统信息（兼容新旧 API）
 * 返回包含 statusBarHeight / platform / windowWidth / windowHeight 的对象
 */
const getSystemInfo = () => {
  try {
    const windowInfo = wx.getWindowInfo();
    const deviceInfo = wx.getDeviceInfo();
    return {
      statusBarHeight: windowInfo.statusBarHeight || 0,
      windowWidth: windowInfo.windowWidth,
      windowHeight: windowInfo.windowHeight,
      pixelRatio: windowInfo.pixelRatio,
      platform: deviceInfo.platform,
      model: deviceInfo.model,
      system: deviceInfo.system,
    };
  } catch (e) {
    // 降级使用旧 API
    return wx.getSystemInfoSync();
  }
};

/**
 * rpx转px
 */
const rpx2px = (rpx) => {
  const { windowWidth } = getSystemInfo();
  return rpx * windowWidth / 750;
};

/**
 * px转rpx
 */
const px2rpx = (px) => {
  const { windowWidth } = getSystemInfo();
  return px * 750 / windowWidth;
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
  getSystemInfo,
  rpx2px,
  px2rpx,
};
