/**
 * Request - 请求封装
 */

const apiConfig = require('../config/api');
const { getToken, clearToken } = require('../utils/auth');

/**
 * 请求封装
 */
const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = getToken();

    wx.request({
      url: apiConfig.baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `${token}` : '',
        ...options.header
      },
      timeout: options.timeout || 30000,

      success: (res) => {
        const { statusCode, data } = res;

        // HTTP状态码处理
        if (statusCode === 200) {
          // 业务状态码处理
          if (data.code === 200 || data.code === 0) {
            resolve(data.data);
          } else if (data.code === 401) {
            // 登录过期
            clearToken();
            wx.showToast({ title: '登录已过期', icon: 'none' });
            // 延迟跳转，让用户看到提示
            setTimeout(() => {
              const pages = getCurrentPages();
              if (pages.length > 0) {
                const currentPage = pages[pages.length - 1];
                wx.setStorageSync('redirectUrl', '/' + currentPage.route);
              }
              wx.navigateTo({ url: '/pages/login/index' });
            }, 1500);
            reject(data);
          } else {
            // 业务错误
            if (options.showError !== false) {
              wx.showToast({
                title: data.message || '操作失败',
                icon: 'none'
              });
            }
            reject(data);
          }
        } else {
          const errorMsg = `HTTP Error: ${statusCode}`;
          if (options.showError !== false) {
            wx.showToast({ title: '服务器异常', icon: 'none' });
          }
          reject({ code: statusCode, message: errorMsg });
        }
      },

      fail: (err) => {
        if (options.showError !== false) {
          wx.showToast({ title: '网络异常', icon: 'none' });
        }
        reject({ code: -1, message: err.errMsg || '网络异常' });
      }
    });
  });
};

/**
 * GET请求
 */
const get = (url, data, options = {}) => {
  return request({ url, method: 'GET', data, ...options });
};

/**
 * POST请求
 */
const post = (url, data, options = {}) => {
  return request({ url, method: 'POST', data, ...options });
};

/**
 * PUT请求
 */
const put = (url, data, options = {}) => {
  return request({ url, method: 'PUT', data, ...options });
};

/**
 * DELETE请求
 */
const del = (url, data, options = {}) => {
  return request({ url, method: 'DELETE', data, ...options });
};

/**
 * 上传文件
 */
const upload = (filePath, options = {}) => {
  return new Promise((resolve, reject) => {
    const token = getToken();

    wx.uploadFile({
      url: apiConfig.baseUrl + (options.url || apiConfig.paths.common.upload),
      filePath,
      name: options.name || 'file',
      formData: options.formData || {},
      header: {
        'Authorization': token ? `${token}` : '',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          try {
            const data = JSON.parse(res.data);
            if (data.code === 200 || data.code === 0) {
              resolve(data.data);
            } else {
              wx.showToast({ title: data.message || '上传失败', icon: 'none' });
              reject(data);
            }
          } catch (e) {
            reject({ code: -1, message: '解析响应失败' });
          }
        } else {
          reject({ code: res.statusCode, message: 'HTTP Error' });
        }
      },
      fail: (err) => {
        wx.showToast({ title: '上传失败', icon: 'none' });
        reject(err);
      }
    });
  });
};

module.exports = {
  request,
  get,
  post,
  put,
  del,
  upload,
};
