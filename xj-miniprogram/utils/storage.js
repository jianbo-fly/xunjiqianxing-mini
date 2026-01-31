/**
 * Storage - 本地存储封装
 */

/**
 * 设置存储
 */
const set = (key, value) => {
  try {
    wx.setStorageSync(key, value);
    return true;
  } catch (e) {
    console.error('Storage set error:', e);
    return false;
  }
};

/**
 * 获取存储
 */
const get = (key, defaultValue = null) => {
  try {
    const value = wx.getStorageSync(key);
    return value !== '' ? value : defaultValue;
  } catch (e) {
    console.error('Storage get error:', e);
    return defaultValue;
  }
};

/**
 * 移除存储
 */
const remove = (key) => {
  try {
    wx.removeStorageSync(key);
    return true;
  } catch (e) {
    console.error('Storage remove error:', e);
    return false;
  }
};

/**
 * 清空所有存储
 */
const clear = () => {
  try {
    wx.clearStorageSync();
    return true;
  } catch (e) {
    console.error('Storage clear error:', e);
    return false;
  }
};

/**
 * 获取存储信息
 */
const getInfo = () => {
  try {
    return wx.getStorageInfoSync();
  } catch (e) {
    console.error('Storage getInfo error:', e);
    return null;
  }
};

/**
 * 设置带过期时间的存储
 */
const setWithExpire = (key, value, expireSeconds) => {
  const data = {
    value,
    expire: Date.now() + expireSeconds * 1000,
  };
  return set(key, data);
};

/**
 * 获取带过期时间的存储
 */
const getWithExpire = (key, defaultValue = null) => {
  const data = get(key);

  if (!data || !data.expire) {
    return defaultValue;
  }

  if (Date.now() > data.expire) {
    remove(key);
    return defaultValue;
  }

  return data.value;
};

/**
 * 搜索历史管理
 */
const searchHistory = {
  key: 'searchHistory',
  maxLength: 10,

  get() {
    return get(this.key, []);
  },

  add(keyword) {
    if (!keyword || !keyword.trim()) return;

    let history = this.get();

    // 去重
    history = history.filter(item => item !== keyword);
    // 添加到头部
    history.unshift(keyword);
    // 限制长度
    if (history.length > this.maxLength) {
      history = history.slice(0, this.maxLength);
    }

    set(this.key, history);
  },

  remove(keyword) {
    let history = this.get();
    history = history.filter(item => item !== keyword);
    set(this.key, history);
  },

  clear() {
    remove(this.key);
  }
};

/**
 * 最近浏览管理
 */
const recentView = {
  key: 'recentView',
  maxLength: 20,

  get() {
    return get(this.key, []);
  },

  add(item) {
    if (!item || !item.id) return;

    let list = this.get();

    // 去重
    list = list.filter(i => i.id !== item.id);
    // 添加到头部
    list.unshift({
      ...item,
      viewTime: Date.now(),
    });
    // 限制长度
    if (list.length > this.maxLength) {
      list = list.slice(0, this.maxLength);
    }

    set(this.key, list);
  },

  clear() {
    remove(this.key);
  }
};

module.exports = {
  set,
  get,
  remove,
  clear,
  getInfo,
  setWithExpire,
  getWithExpire,
  searchHistory,
  recentView,
};
