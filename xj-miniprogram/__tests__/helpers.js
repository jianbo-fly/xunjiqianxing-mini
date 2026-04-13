/**
 * 测试辅助工具
 */

/**
 * 加载小程序页面模块，返回 Page 配置对象
 * 用法：const page = loadPage('pages/order/confirm/index');
 */
function loadPage(pagePath) {
  const path = require('path');
  const fullPath = path.resolve(__dirname, '..', pagePath);

  // 清除该模块及其依赖的缓存
  Object.keys(require.cache).forEach((key) => {
    if (key.startsWith(path.resolve(__dirname, '..'))) {
      delete require.cache[key];
    }
  });

  global.__lastPageConfig = null;
  require(fullPath);
  return global.__lastPageConfig;
}

/**
 * 创建一个模拟的 Page 实例，支持 setData 和 data 访问
 */
function createPageInstance(pageConfig, initialData = {}) {
  const data = { ...pageConfig.data, ...initialData };

  const instance = {
    data,
    setData(obj, callback) {
      Object.assign(this.data, obj);
      if (callback) callback();
    },
    // 将 pageConfig 上的方法绑定到 instance
  };

  // 绑定所有方法
  Object.keys(pageConfig).forEach((key) => {
    if (typeof pageConfig[key] === 'function' && key !== 'data') {
      instance[key] = pageConfig[key].bind(instance);
    }
  });

  return instance;
}

module.exports = { loadPage, createPageInstance };
